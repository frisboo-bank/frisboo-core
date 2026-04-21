package com.frisboo.corebanking.persistence.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.core.serializers.PersistenceStringSerializerImpl
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
class RedisOperationsImplTest : StringSpec() {

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

    private suspend fun createRedisOperations(prefix: String = "test-${Uuid.random()}") =
        redisOperationsFactory.create(
            prefix = prefix,
            keySerializer = PersistenceStringSerializerImpl(),
            valueSerializer = PersistenceStringSerializerImpl(),
        )

    init {
        "ping returns PONG" {
            createRedisOperations().ping() shouldBeRight true
        }

        "CRUD roundtrip (set, get, exists, del)" {
            checkAll(
                Arb.string(),
                Arb.string(),
                Arb.boolean(),
            ) { key, value, withTtl ->
                val ops = createRedisOperations()

                // Set
                if (withTtl) {
                    ops.set(key, value, 1000) shouldBeRight null
                } else {
                    ops.set(key, value) shouldBeRight null
                }

                // Get & Exists
                ops.get(key) shouldBeRight value
                ops.exists(key) shouldBeRight true

                // Overwrite
                val newValue = "$value-new"
                ops.set(key, newValue) shouldBeRight value
                ops.get(key) shouldBeRight newValue

                // Del
                ops.del(key) shouldBeRight true
                ops.exists(key) shouldBeRight false
                ops.get(key) shouldBeRight null

                // Del non-existent
                ops.del(key) shouldBeRight false
            }
        }

        "TTL expiry (pexpire, set with TTL)" {
            checkAll(
                PropTestConfig(10),
                Arb.string(minSize = 4),
                Arb.string(),
                Arb.string(minSize = 4),
                Arb.long(10L..50L),
            ) { key, value, key2, ttlMs ->
                val ops = createRedisOperations()

                ops.set(key, value, ttlMs) shouldBeRight null
                ops.get(key) shouldBeRight value

                ops.set(key2, value) shouldBeRight null
                ops.pexpire(key2, ttlMs) shouldBeRight true
                ops.get(key2) shouldBeRight value

                delay((ttlMs + 10).milliseconds)

                ops.get(key) shouldBeRight null
                ops.get(key2) shouldBeRight null
                ops.exists(key) shouldBeRight false
                ops.exists(key2) shouldBeRight false

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
                allKeys.forEach { ops.set(it, "$it-value") shouldBeRight null }

                ops.scanCount() shouldBeRight keyCount
                ops.scanCount(batchSize) shouldBeRight keyCount

                val retrieved = mutableSetOf<String>()
                var cursor = RedisScanCursor.INITIAL

                do {
                    val page = ops.scanPage(cursor, pageLimit).shouldBeRight()
                    retrieved.addAll(page.keys)
                    cursor = page.nextCursor
                } while (!cursor.isFinished)

                retrieved shouldBe allKeys
            }
        }

        "locking: acquire, release, set with lock" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.string(),
            ) { key, value ->
                val ops = createRedisOperations()

                val lock = ops.acquireLock(key).shouldBeRight()
                lock.shouldNotBeNull()
                lock.size shouldBe 16

                ops.acquireLock(key).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.LockAlreadyHeld>()

                ops.set(key, value, lock) shouldBeRight null
                ops.get(key) shouldBeRight value

                ops.acquireLock(key).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.LockAlreadyHeld>()

                val wrongToken = ByteArray(16) { 0 }
                ops.releaseLock(key, wrongToken).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.LockNotHeld>()

                ops.releaseLock(key, lock) shouldBeRight Unit
                ops.get(key) shouldBeRight value

                val lock2 = ops.acquireLock(key).shouldBeRight()
                lock2.shouldNotBeNull()

                ops.releaseLock(key, lock2) shouldBeRight Unit
                ops.acquireLock(key).shouldBeRight().shouldNotBeNull()
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

                val lock = ops.acquireLock(key).shouldBeRight()
                lock.shouldNotBeNull()

                ops.set(key, value, ttlMs, lock) shouldBeRight null
                ops.get(key) shouldBeRight value

                delay((ttlMs + 10).milliseconds)

                ops.get(key) shouldBeRight null

                val lock2 = ops.acquireLock(key).shouldBeRight()
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

                ops.set(key, value, badTtl).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.OperationFailed>()
            }
        }

        "pexpire with non-positive TTL returns Left" {
            checkAll(
                Arb.string(minSize = 4),
                Arb.long(Long.MIN_VALUE..0L),
            ) { key, badTtl ->
                val ops = createRedisOperations()
                ops.pexpire(key, badTtl).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.OperationFailed>()
            }
        }

        "scanPage with non-positive limit returns Left" {
            checkAll(Arb.int(Int.MIN_VALUE..0)) { badLimit ->
                val ops = createRedisOperations()
                ops.scanPage(null, badLimit).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.OperationFailed>()
            }
        }

        "concurrent acquireLock only one succeeds" {
            checkAll(
                Arb.int(3..100),
            ) { attempts ->
                val ops = createRedisOperations()
                val successCount = AtomicInteger(0)

                coroutineScope {
                    List(attempts) {
                        async(Dispatchers.IO) {
                            ops.acquireLock("shared").fold(
                                ifLeft = { /* ignore */ },
                                ifRight = { successCount.incrementAndGet() },
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

                ops.get(key).shouldBeRight().shouldBeIn(values)
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
                Arb.byteArray(Arb.int(0..1024), Arb.byte()),
            ) { key, bytes ->
                redisTestFixture.flushAll()

                binaryOps.set(key, bytes) shouldBeRight null

                binaryOps.get(key).shouldBeRight().also {
                    it.contentEquals(bytes) shouldBe true
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
                    override fun serialize(value: User): ByteArray =
                        mapper.writeValueAsBytes(value)

                    override fun deserialize(value: ByteArray): User =
                        mapper.readValue(value, User::class.java)
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

                jsonOps.set(key, user) shouldBeRight null

                jsonOps.get(key).shouldBeRight() shouldBe user
            }
        }
    }
}
