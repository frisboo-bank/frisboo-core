package com.frisboo.corebanking.persistence.redis

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
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
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
import io.kotest.property.arbitrary.string
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
            createRedisOperations().ping().shouldBeRight("PONG")
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
                    ops.set(key, value, 1000).shouldBeRight(null)
                } else {
                    ops.set(key, value).shouldBeRight(null)
                }

                // Get & Exists
                ops.get(key).shouldBeRight(value)
                ops.exists(key).shouldBeRight(true)

                // Overwrite
                val newValue = "$value-new"
                ops.set(key, newValue).shouldBeRight(value)
                ops.get(key).shouldBeRight(newValue)

                // Del
                ops.del(key).shouldBeRight(true)
                ops.exists(key).shouldBeRight(false)
                ops.get(key).shouldBeRight(null)

                // Del non-existent
                ops.del(key).shouldBeRight(false)
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

                ops.set(key, value, ttlMs).shouldBeRight(null)
                ops.get(key).shouldBeRight(value)

                ops.set(key2, value).shouldBeRight(null)
                ops.pexpire(key2, ttlMs).shouldBeRight(true)
                ops.get(key2).shouldBeRight(value)

                delay((ttlMs + 10).milliseconds)

                ops.get(key).shouldBeRight(null)
                ops.get(key2).shouldBeRight(null)
                ops.exists(key).shouldBeRight(false)
                ops.exists(key2).shouldBeRight(false)

                ops.pexpire("ghost", 1000).shouldBeRight(false)
            }
        }

        "scan operations (count, page)" {
            checkAll(
                PropTestConfig(iterations = 100),
                Arb.int(5..30),
                Arb.int(1..10),
                Arb.long(1L..20L),
            ) { keyCount, pageLimit, batchSize ->
                val prefix = "scan-${Uuid.random()}"
                val ops = createRedisOperations(prefix)

                val allKeys = (1..keyCount).map { "key-$it" }.toSet()
                allKeys.forEach { ops.set(it, "$it-value").shouldBeRight(null) }

                ops.scanCount().shouldBeRight(keyCount.toLong())
                ops.scanCount(batchSize).shouldBeRight(keyCount.toLong())

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

//
//            val firstPage = ops.scanPage(null, 4).shouldBeRight()
//            firstPage.keys.size shouldBeLessThanOrEqual 4
//            firstPage.nextCursor.shouldNotBeNull()
//        }

        "locking: acquire, release, set with lock" {
            checkAll(
                PropTestConfig(iterations = 10),
                Arb.string(minSize = 4),
                Arb.string(),
            ) { key, value ->
                val ops = createRedisOperations()

                val lock = ops.acquireLock(key).shouldBeRight()
                lock.shouldNotBeNull()
                lock.size shouldBe 16

                ops.acquireLock(key).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.LockAlreadyHeld>()

                ops.set(key, value, lock).shouldBeRight(null)
                ops.get(key).shouldBeRight(value)

                ops.acquireLock(key).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.LockAlreadyHeld>()

                val wrongToken = ByteArray(16) { 0 }
                ops.releaseLock(key, wrongToken).shouldBeLeft()
                    .shouldBeInstanceOf<PersistenceError.LockNotHeld>()

                ops.releaseLock(key, lock).shouldBeRight(Unit)
                ops.get(key).shouldBeRight(value)

                val lock2 = ops.acquireLock(key).shouldBeRight()
                lock2.shouldNotBeNull()
                ops.releaseLock(key, lock2).shouldBeRight(Unit)
                ops.acquireLock(key).shouldBeRight().shouldNotBeNull()
            }
        }

        "set with TTL and lock" {
            val ops = createRedisOperations()

            val lock = ops.acquireLock("key").shouldBeRight()
            lock.shouldNotBeNull()

            ops.set("key", "temp", 10L, lock).shouldBeRight(null)
            ops.get("key").shouldBeRight("temp")
            delay(20.milliseconds)
            ops.get("key").shouldBeRight(null)
        }

        "concurrent acquireLock only one succeeds" {
            checkAll(
                PropTestConfig(iterations = 10),
                Arb.int(3..10),
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
            checkAll(PropTestConfig(iterations = 10), Arb.string(), Arb.int(3..8)) { key, writers ->
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
                    override fun serialize(value: ByteArray) = value
                    override fun deserialize(value: ByteArray) = value
                },
            )
            checkAll(
                PropTestConfig(iterations = 20),
                Arb.string(),
                Arb.byteArray(Arb.int(0..1024), Arb.byte()),
            ) { key, bytes ->
                binaryOps.set(key, bytes).shouldBeRight(null)
                binaryOps.get(key).shouldBeRight().contentEquals(bytes).shouldBeTrue()
            }
        }
    }

    private fun <T> T.shouldBeIn(collection: Iterable<T>) {
        collection shouldContain this
    }
}
