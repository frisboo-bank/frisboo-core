package com.frisboo.corebanking.registry.adapters.distributed.redis

import com.frisboo.corebanking.persistence.redis.testing.deleteAll
import com.frisboo.corebanking.persistence.redis.testing.redisContainer
import com.frisboo.corebanking.registry.contracts.RegistrySerializer
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.REGISTRY_DEFAULT_MAX_PAGE_SIZE
import com.frisboo.corebanking.registry.models.RegistryContainsResult
import com.frisboo.corebanking.registry.models.RegistryEvictResult
import com.frisboo.corebanking.registry.models.RegistryGetOrPutResult
import com.frisboo.corebanking.registry.models.RegistryGetResult
import com.frisboo.corebanking.registry.models.RegistryIsHealthyResult
import com.frisboo.corebanking.registry.models.RegistryPutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.RegistrySetTtlResult
import com.frisboo.corebanking.registry.models.RegistrySizeResult
import com.frisboo.corebanking.registry.serialization.StringRegistrySerializer
import com.frisboo.corebanking.tests.testcontainers.withChaosProxyContainer
import eu.rekawek.toxiproxy.model.ToxicDirection
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKotest::class)
internal class RedisRegistryImplTest : StringSpec() {

    private val redisContainer = redisContainer()

    private lateinit var client: RedisClient
    private lateinit var connection: StatefulRedisConnection<ByteArray, ByteArray>
    private lateinit var registry: RedisRegistryImpl<String, String>

    override suspend fun beforeSpec(spec: Spec) {
        redisContainer.start()
        client = RedisClient.create("redis://${redisContainer.host}:${redisContainer.firstMappedPort}")
        connection = client.connect(ByteArrayCodec())
    }

    override suspend fun afterSpec(spec: Spec) {
        connection.close()
        client.shutdown()
        redisContainer.stop()
    }

    override suspend fun beforeTest(testCase: TestCase) {
        registry = RedisRegistryImpl(
            scope = RegistryScope(
                name = "test-${UUID.randomUUID()}",
                team = "registry",
            ),
            connection = connection,
            keySerializer = StringRegistrySerializer(),
            valueSerializer = StringRegistrySerializer(),
            operationTimeout = 5.seconds,
        )
        connection.deleteAll(registry.scope.prefix)
    }

    init {
        "checks redis health" {
            withChaosProxyContainer(
                container = redisContainer(),
                upstreamAlias = "redis",
                upstreamPort = 6379,
            ) {
                val proxyClient = RedisClient.create("redis://$proxyHost:$proxyPort")

                try {
                    proxyClient.connect(ByteArrayCodec()).use { proxyConnection ->
                        val proxyRegistry = RedisRegistryImpl(
                            scope = RegistryScope(name = "test-health", team = "registry"),
                            connection = proxyConnection,
                            keySerializer = StringRegistrySerializer(),
                            valueSerializer = StringRegistrySerializer(),
                            operationTimeout = 1.seconds,
                        )

                        proxyRegistry.isHealthy().shouldBeRight(RegistryIsHealthyResult.Up)

                        proxy.toxics().bandwidth("break", ToxicDirection.DOWNSTREAM, 0)

                        proxyRegistry.isHealthy().shouldBeRight().shouldBeInstanceOf<RegistryIsHealthyResult.Down>()
                            .message shouldContain "Failed to ping Redis"

                        proxy.toxics().get("break")?.remove()

                        proxyRegistry.isHealthy().shouldBeRight(RegistryIsHealthyResult.Up)
                    }
                } finally {
                    proxyClient.shutdown()
                }
            }
        }

        "put and get round-trip" {
            checkAll(Arb.string(1..100), Arb.string(1..100), Arb.boolean()) { key, value, saved ->
                registry.evict(key).shouldBeRight()

                when (saved) {
                    true -> {
                        registry.put(key, value).shouldBeRight(RegistryPutResult.Created(value))
                        registry.get(key).shouldBeRight(RegistryGetResult.Found(value))
                    }

                    false -> registry.get(key).shouldBeRight(RegistryGetResult.NotFound)
                }
            }
        }

        "put and get round-trip of binary data" {
            val binaryRegistry = RedisRegistryImpl(
                scope = RegistryScope(name = "test-binary", team = "registry"),
                connection = connection,
                keySerializer = StringRegistrySerializer(),
                valueSerializer = object : RegistrySerializer<ByteArray> {
                    override fun serialize(value: ByteArray): ByteArray = value
                    override fun deserialize(value: ByteArray): ByteArray = value
                },
                operationTimeout = 5.seconds,
            )

            checkAll(
                Arb.string(1..100),
                Arb.byteArray(Arb.int(0..4096), Arb.byte()),
            ) { key, value ->
                binaryRegistry.evict(key).shouldBeRight()

                binaryRegistry.put(key, value).shouldBeRight(RegistryPutResult.Created(value))

                binaryRegistry.get(key).shouldBeRight().shouldBeInstanceOf<RegistryGetResult.Found<ByteArray>>()
                    .value.contentEquals(value) shouldBe true
            }
        }


        "put with existing key returns Updated" {
            checkAll(Arb.string(1..100), Arb.string(1..100), Arb.string(1..100)) { key, initialValue, newValue ->
                registry.evict(key).shouldBeRight()

                registry.put(key, initialValue).shouldBeRight(RegistryPutResult.Created(initialValue))
                registry.put(key, newValue)
                    .shouldBeRight(RegistryPutResult.Updated(previousValue = initialValue, newValue = newValue))
                registry.get(key).shouldBeRight(RegistryGetResult.Found(newValue))
            }
        }

        "put with negative TTL returns InvalidArgument" {
            val result = registry.put("key", "value", ttl = (-1).milliseconds)

            result.shouldBeLeft().shouldBeInstanceOf<RegistryError.InvalidArgument>().also {
                it.name shouldBe "ttl"
                it.message shouldContain "TTL must be >= 1ms"
            }
        }

        "put with TTL expires the record" {
            checkAll(
                PropTestConfig(iterations = 100),
                Arb.string(1..100),
                Arb.string(1..100),
                Arb.int(10..20),
            ) { key, value, ttl ->
                registry.evict(key).shouldBeRight()

                val ttlMs = ttl.milliseconds

                registry.put(key, value, ttl = ttlMs).shouldBeRight(RegistryPutResult.Created(value))
                registry.get(key).shouldBeRight(RegistryGetResult.Found(value))

                delay(ttlMs + 10.milliseconds)

                registry.get(key).shouldBeRight(RegistryGetResult.NotFound)
            }
        }

        "contains checks if key exists without retrieving value" {
            checkAll(Arb.string(1..100), Arb.string(1..100), Arb.boolean()) { key, value, saved ->
                registry.evict(key).shouldBeRight()

                when (saved) {
                    true -> {
                        registry.put(key, value).shouldBeRight(RegistryPutResult.Created(value))
                        registry.contains(key).shouldBeRight(RegistryContainsResult.Found)
                    }

                    false -> registry.contains(key).shouldBeRight(RegistryContainsResult.NotFound)
                }
            }
        }

        "return the number of records saved in the current scope" {
            checkAll(Arb.list(Arb.string())) { keys ->
                val uniqueKeys = keys.toSet()

                connection.deleteAll(registry.scope.prefix)

                uniqueKeys.forEachIndexed { index, key ->
                    registry.put(key, "value$index").shouldBeRight()
                }

                registry.size().shouldBeRight(RegistrySizeResult.Size(uniqueKeys.size.toLong()))
            }
        }

        "getOrPut retrieves or saves value in one unified command" {
            checkAll(Arb.string(), Arb.string(), Arb.boolean()) { key, value, exists ->
                registry.evict(key).shouldBeRight()

                if (exists) {
                    registry.put(key, value).shouldBeRight(RegistryPutResult.Created(value))
                }

                var factoryCalled = false
                val result = registry.getOrPut(key) {
                    factoryCalled = true
                    value
                }

                when (exists) {
                    true -> {
                        result.shouldBeRight(RegistryGetOrPutResult.Found(value))
                        factoryCalled shouldBe false
                    }

                    false -> {
                        result.shouldBeRight(RegistryGetOrPutResult.Created(value))
                        factoryCalled shouldBe true
                        registry.get(key).shouldBeRight(RegistryGetResult.Found(value))
                    }
                }
            }
        }

        "getOrPut handle concurrent calls to the same key" {
            checkAll(
                Arb.string(),
                Arb.string(),
                Arb.int(2..100),
            ) { key, value, concurrentCalls ->
                val factoryCalls = AtomicInteger(0)

                registry.evict(key).shouldBeRight()

                coroutineScope {
                    val jobs = List(concurrentCalls) {
                        async(Dispatchers.IO) {
                            registry.getOrPut(key) {
                                delay(1)
                                factoryCalls.incrementAndGet()
                                value
                            }
                        }
                    }
                    val results = jobs.awaitAll()
                    results.forEach { it.shouldBeRight() }
                }

                factoryCalls.get() shouldBe 1
                registry.get(key).shouldBeRight(RegistryGetResult.Found(value))
            }
        }

        "getOrPut propagates factory exceptions as RegistryError" {
            val result = registry.getOrPut("new-record") {
                throw RuntimeException("Factory failed")
            }

            result.shouldBeLeft().shouldBeInstanceOf<RegistryError.ComputationFailed>().also { res ->
                res.message shouldBe "Factory function for key `new-record` failed"
                res.cause.shouldBeInstanceOf<RuntimeException>()
                    .message shouldBe "Factory failed"
            }
        }

        "getOrPut with negative TTL returns InvalidArgument" {
            val result = registry.getOrPut("key", ttl = (-1).milliseconds) { "value" }

            result.shouldBeLeft().shouldBeInstanceOf<RegistryError.InvalidArgument>().also {
                it.name shouldBe "ttl"
                it.message shouldContain "TTL must be >= 1ms"
            }
        }

        "getOrPut with TTL expires the record" {
            checkAll(
                PropTestConfig(iterations = 100),
                Arb.string(1..100),
                Arb.string(1..100),
                Arb.int(10..20),
            ) { key, value, ttl ->
                registry.evict(key).shouldBeRight()

                val ttlMs = ttl.milliseconds

                registry.getOrPut(key, ttl = ttlMs) { value }
                    .shouldBeRight(RegistryGetOrPutResult.Created(value))
                registry.get(key).shouldBeRight(RegistryGetResult.Found(value))

                delay(ttlMs + 10.milliseconds)

                registry.get(key).shouldBeRight(RegistryGetResult.NotFound)
            }
        }

        "evict removes existing key" {
            checkAll(Arb.string(1..100), Arb.string(1..100), Arb.boolean()) { key, value, exists ->
                if (exists) {
                    registry.put(key, value).shouldBeRight()
                }

                when (exists) {
                    true -> registry.evict(key).shouldBeRight(RegistryEvictResult.Evicted(1))
                    false -> registry.evict(key).shouldBeRight(RegistryEvictResult.NotFound)
                }

                registry.contains(key).shouldBeRight(RegistryContainsResult.NotFound)
            }
        }

        "keysPage returns paginated keys" {
            checkAll(
                Arb.list(Arb.string(1..100), 1..50),
                Arb.int(10..60),
            ) { keys, limit ->
                val uniqueKeys = keys.toSet()

                connection.deleteAll(registry.scope.prefix)

                uniqueKeys.forEach { key ->
                    registry.put(key, "val-$key").shouldBeRight()
                }

                val retrievedKeys = mutableSetOf<String>()
                var cursor: String? = null
                do {
                    val page = registry.keysPage(cursor, limit = limit).shouldBeRight()
                    retrievedKeys.addAll(page.items)
                    cursor = page.nextCursor
                } while (cursor != null)

                retrievedKeys shouldBe uniqueKeys
            }
        }

        "keysPage with invalid limit returns InvalidArgument" {
            checkAll(
                Arb.int(min = Int.MIN_VALUE, max = 0),                     // below valid range
                Arb.int(min = REGISTRY_DEFAULT_MAX_PAGE_SIZE + 1, max = Int.MAX_VALUE), // above valid range
            ) { belowMin, aboveMax ->
                listOf(belowMin, aboveMax).forEach { invalidLimit ->
                    val result = registry.keysPage(null, invalidLimit)

                    result.shouldBeLeft().shouldBeInstanceOf<RegistryError.InvalidArgument>().also {
                        it.name shouldBe "limit"
                        it.message shouldBe  "Page size must be between 1 and $REGISTRY_DEFAULT_MAX_PAGE_SIZE, got: $invalidLimit"
                    }
                }
            }
        }

        "change the ttl of a record" {
            checkAll(
                PropTestConfig(iterations = 100),
                Arb.string(1..100),
                Arb.string(1..100),
                Arb.boolean(),
                Arb.int(10..20),
            ) { key, value, exists, ttlMs ->
                registry.evict(key).shouldBeRight()

                if (exists) {
                    registry.put(key, value).shouldBeRight(RegistryPutResult.Created(value))
                }

                val ttl = ttlMs.milliseconds
                val setResult = registry.setTTL(key, ttl)

                when (exists) {
                    false -> setResult.shouldBeRight(RegistrySetTtlResult.NotFound)

                    true -> {
                        setResult.shouldBeRight(RegistrySetTtlResult.Applied)
                        registry.contains(key).shouldBeRight(RegistryContainsResult.Found)

                        delay(ttl + 10.milliseconds)

                        registry.contains(key).shouldBeRight(RegistryContainsResult.NotFound)
                    }
                }
            }
        }

        "change the ttl with negative TTL returns InvalidArgument" {
            val result = registry.setTTL("key", (-1).milliseconds)

            result.shouldBeLeft().shouldBeInstanceOf<RegistryError.InvalidArgument>().also {
                it.name shouldBe "ttl"
                it.message shouldContain "TTL must be >= 1ms"
            }
        }
    }
}
