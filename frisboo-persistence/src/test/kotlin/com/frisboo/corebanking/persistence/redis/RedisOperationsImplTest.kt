/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.persistence.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.core.serializers.PersistenceStringSerializerImpl
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDeleteResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisExistsResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisScanPageResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.persistence.redis.testing.RedisTestFixture
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class, ExperimentalKotest::class)
internal class RedisOperationsImplTest : StringSpec() {

    private val redisTestFixture = RedisTestFixture()
    private lateinit var redisOperationsFactory: RedisOperationsFactoryImpl

    override suspend fun beforeSpec(spec: Spec) {
        redisTestFixture.start()
        redisOperationsFactory = RedisOperationsFactoryImpl(connectionPool = redisTestFixture.getPool())
    }

    override suspend fun afterSpec(spec: Spec) {
        redisTestFixture.stop()
    }

    override suspend fun beforeTest(testCase: TestCase) {
        redisTestFixture.flushAll()
    }

    private suspend fun createRedisOperations(prefix: String = "test-${Uuid.random()}") = redisOperationsFactory.create(
        prefix = prefix,
        keySerializer = PersistenceStringSerializerImpl(),
        valueSerializer = PersistenceStringSerializerImpl(),
    )

    init {
        "ping returns PONG" {
            createRedisOperations().ping() shouldBeRight true
        }

        "CRUD round-trip (set, get, exists, del)" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.string(minSize = 1),
                Arb.boolean(),
            ) { key, value, withTtl ->
                val ops = createRedisOperations()

                // Set
                if (withTtl) {
                    ops.set(key, value, 1000) shouldBeRight RedisSetResult.Success(null)
                } else {
                    ops.set(key, value) shouldBeRight RedisSetResult.Success(null)
                }

                // Get & Exists
                ops.get(key) shouldBeRight RedisGetResult.Found(value)
                ops.exists(key) shouldBeRight RedisExistsResult.Exists

                // Overwrite
                val newValue = "$value-new"
                val result = ops.set(key, newValue)
                result shouldBeRight RedisSetResult.Success(value)
                ops.get(key) shouldBeRight RedisGetResult.Found(newValue)

                // Del
                ops.del(key) shouldBeRight RedisDeleteResult.Deleted(newValue)
                ops.exists(key) shouldBeRight RedisExistsResult.NotFound
                ops.get(key) shouldBeRight RedisGetResult.NotFound

                // Del non-existent
                ops.del(key) shouldBeRight RedisDeleteResult.NotFound
            }
        }

        "TTL expiry (pexpire, set with TTL)" {
            checkAll(
                PropTestConfig(10),
                Arb.string(minSize = 4),
                Arb.string(minSize = 1),
                Arb.string(minSize = 4),
                Arb.long(10L..50L),
            ) { key, value, key2, ttlMs ->
                val ops = createRedisOperations()

                ops.set(key, value, ttlMs) shouldBeRight RedisSetResult.Success(null)
                ops.get(key) shouldBeRight RedisGetResult.Found(value)

                ops.set(key2, value) shouldBeRight RedisSetResult.Success(null)
                ops.pexpire(key2, ttlMs) shouldBeRight true
                ops.get(key2) shouldBeRight RedisGetResult.Found(value)

                delay((ttlMs + 10).milliseconds)

                ops.get(key) shouldBeRight RedisGetResult.NotFound
                ops.get(key2) shouldBeRight RedisGetResult.NotFound
                ops.exists(key) shouldBeRight RedisExistsResult.NotFound
                ops.exists(key2) shouldBeRight RedisExistsResult.NotFound

                ops.pexpire("ghost", 1000) shouldBeRight false
            }
        }

        "scan operations (count, page)" {
            checkAll(
                PropTestConfig(iterations = 100),
                Arb.long(5L..30L),
                Arb.int(1..10),
                Arb.long(1L..20L),
            ) { keyCount, pageLimit, batchSize ->
                val prefix = "scan-${Uuid.random()}"
                val ops = createRedisOperations(prefix)

                val allKeys = (1..keyCount).map { "key-$it" }.toSet()
                allKeys.forEach { ops.set(it, "$it-value") shouldBeRight RedisSetResult.Success(null) }

                ops.scanCount() shouldBeRight keyCount
                ops.scanCount(batchSize) shouldBeRight keyCount

                val retrieved = mutableSetOf<String>()
                var cursor = RedisScanCursor.INITIAL

                do {
                    val page = when (val result = ops.scanPage(cursor, pageLimit).shouldBeRight()) {
                        is RedisScanPageResult.Success -> result
                    }
                    retrieved.addAll(page.keys)
                    cursor = page.nextCursor
                } while (!cursor.isFinished)

                retrieved shouldBe allKeys
            }
        }

        "locking: acquire, release, set with lock" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.string(minSize = 1),
                Arb.long(1000L..50000L),
            ) { key, value, ttlMs ->
                val ops = createRedisOperations()

                val lock = when (val acquired = ops.acquireLock(key, ttlMs).shouldBeRight()) {
                    is RedisLockAcquireResult.Acquired -> acquired.token
                    else -> throw AssertionError("Expected lock to be acquired")
                }
                lock.shouldNotBeNull()
                lock.size shouldBe 16

                ops.acquireLock(key, ttlMs).shouldBeRight().shouldBeInstanceOf<RedisLockAcquireResult.AlreadyHeld>()

                ops.set(key, value, lock) shouldBeRight RedisSetResult.Success(null)
                ops.get(key) shouldBeRight RedisGetResult.Found(value)

                ops.acquireLock(key, ttlMs).shouldBeRight().shouldBeInstanceOf<RedisLockAcquireResult.AlreadyHeld>()

                val wrongToken = ByteArray(16) { 0 }
                ops.releaseLock(key, wrongToken).shouldBeRight().shouldBeInstanceOf<RedisLockReleaseResult.Mismatch>()

                val releaseRes = ops.releaseLock(key, lock).shouldBeRight()
                releaseRes shouldBe RedisLockReleaseResult.Released
                ops.get(key) shouldBeRight RedisGetResult.Found(value)

                val lock2 = when (val acquired2 = ops.acquireLock(key, ttlMs).shouldBeRight()) {
                    is RedisLockAcquireResult.Acquired -> acquired2.token
                    else -> throw AssertionError("Expected lock to be acquired")
                }

                val releaseRes2 = ops.releaseLock(key, lock2).shouldBeRight()
                releaseRes2 shouldBe RedisLockReleaseResult.Released

                when (val reacquired = ops.acquireLock(key, ttlMs).shouldBeRight()) {
                    is RedisLockAcquireResult.Acquired -> reacquired.token.shouldNotBeNull()
                    else -> throw AssertionError("Expected lock to be acquired")
                }
            }
        }

        "set with TTL and lock, lock and key should at least expire at the same time" {
            checkAll(
                PropTestConfig(iterations = 10),
                Arb.string(minSize = 4),
                Arb.string(),
                Arb.long(10L..50L),
            ) { key, value, ttlMs ->
                val ops = createRedisOperations()

                val lock = when (val acquired = ops.acquireLock(key, ttlMs).shouldBeRight()) {
                    is RedisLockAcquireResult.Acquired -> acquired.token
                    else -> throw AssertionError("Expected lock to be acquired")
                }

                ops.set(key, value, ttlMs, lock) shouldBeRight RedisSetResult.Success(null)
                ops.get(key) shouldBeRight RedisGetResult.Found(value)

                delay((ttlMs + 10).milliseconds)

                ops.get(key) shouldBeRight RedisGetResult.NotFound

                val lock2 = ops.acquireLock(key, ttlMs).shouldBeRight()
                lock2.shouldNotBeNull()
            }
        }

        "set with non-positive TTL returns Left" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.string(),
                Arb.long(Long.MIN_VALUE..0L),
            ) { key, value, badTtl ->
                val ops = createRedisOperations()

                ops.set(key, value, badTtl).shouldBeLeft().shouldBeInstanceOf<PersistenceError.OperationFailed>()
            }
        }

        "pexpire with non-positive TTL returns Left" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.long(Long.MIN_VALUE..0L),
            ) { key, badTtl ->
                val ops = createRedisOperations()
                ops.pexpire(key, badTtl).shouldBeLeft().shouldBeInstanceOf<PersistenceError.OperationFailed>()
            }
        }

        "scanPage with non-positive limit returns Left" {
            checkAll(Arb.int(Int.MIN_VALUE..0)) { badLimit ->
                val ops = createRedisOperations()
                ops.scanPage(null, badLimit).shouldBeLeft().shouldBeInstanceOf<PersistenceError.OperationFailed>()
            }
        }

        "concurrent acquireLock only one succeeds" {
            checkAll(
                Arb.int(3..100),
                Arb.long(1000L..50000L),
            ) { attempts, ttlMs ->
                val ops = createRedisOperations()
                val successCount = AtomicInteger(0)

                coroutineScope {
                    List(attempts) {
                        async(Dispatchers.IO) {
                            ops.acquireLock("shared", ttlMs).fold(
                                ifLeft = { /* ignore */ },
                                ifRight = {
                                    if (it is RedisLockAcquireResult.Acquired) {
                                        successCount.incrementAndGet()
                                    }
                                },
                            )
                        }
                    }.awaitAll()
                }
                successCount.get() shouldBe 1
            }
        }

        "concurrent set on same key leaves consistent state" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.int(3..20),
            ) { key, writers ->
                val ops = createRedisOperations()
                val values = (1..writers).map { "value-$it" }

                coroutineScope {
                    values.map { v ->
                        async(Dispatchers.IO) { ops.set(key, v) }
                    }.awaitAll()
                }

                val result = ops.get(key).shouldBeRight().also {
                    it.value.shouldBeIn(values)
                }
            }
        }

        "binary values roundtrip" {
            val binaryOps = redisOperationsFactory.create(
                prefix = "binary-${Uuid.random()}",
                keySerializer = PersistenceStringSerializerImpl(),
                valueSerializer = object : PersistenceSerializer<ByteArray> {
                    override fun serialize(value: ByteArray): ByteArray {
                        return value
                    }

                    override fun deserialize(value: ByteArray): ByteArray {
                        return value
                    }
                },
            )

            checkAll(
                Arb.string(minSize = 4),
                Arb.byteArray(Arb.int(1..1024), Arb.byte()),
            ) { key, value ->
                redisTestFixture.flushAll()

                binaryOps.set(key, value) shouldBeRight RedisSetResult.Success(null)

                binaryOps.get(key).shouldBeRight().also { result ->
                    result.shouldBeTypeOf<RedisGetResult.Found<ByteArray>>()
                    result.value shouldBe value
                }
            }
        }

        "serializable values roundtrip" {
            data class User(
                val id: Uuid,
                val name: String,
                val age: Int,
                val email: String,
                val isActive: Boolean,
            )

            val mapper = jacksonObjectMapper()
            val jsonOps = redisOperationsFactory.create(
                prefix = "json-${Uuid.random()}",
                keySerializer = PersistenceStringSerializerImpl(),
                valueSerializer = object : PersistenceSerializer<User> {
                    override fun serialize(value: User): ByteArray = mapper.writeValueAsBytes(value)

                    override fun deserialize(value: ByteArray): User = mapper.readValue(value, User::class.java)
                },
            )

            checkAll(
                Arb.string(minSize = 4),
                Arb.uuid(),
                Arb.string(),
                Arb.int(18..90),
                Arb.string().map { "$it@example.com" },
                Arb.boolean(),
            ) { key, id, name, age, email, isActive ->
                redisTestFixture.flushAll()

                val user = User(
                    id = id.toKotlinUuid(),
                    name = name,
                    age = age,
                    email = email,
                    isActive = isActive,
                )

                jsonOps.set(key, user) shouldBeRight RedisSetResult.Success(null)

                jsonOps.get(key).shouldBeRight() shouldBe RedisGetResult.Found(user)
            }
        }
    }
}
