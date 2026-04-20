package com.frisboo.corebanking.statemanager.adapters.distributed.redis

import com.frisboo.corebanking.persistence.core.constants.REDIS_LATEST_IMAGE
import com.frisboo.corebanking.persistence.redis.RedisOperationsFactoryImpl
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_LOCK_TTL
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_OPERATION_TIMEOUT
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperationsFactory
import com.frisboo.corebanking.persistence.redis.testing.RedisTestFixture
import com.frisboo.corebanking.statemanager.adapters.StateManagerTestTemplates
import com.frisboo.corebanking.statemanager.contracts.StateManagerSerializer
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.StateManagerIsHealthyResult
import com.frisboo.corebanking.persistence.core.models.StateManagerScope
import com.frisboo.corebanking.persistence.core.serializers.PersistenceStringSerializerImpl
import com.frisboo.corebanking.tests.testcontainers.withChaosProxyContainer
import com.redis.testcontainers.RedisContainer
import eu.rekawek.toxiproxy.model.ToxicDirection
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.coreshouldBeRight
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.yield
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKotest::class)
internal class StateManagerRedisImplTest : StringSpec() {

    private val redisTestFixture = RedisTestFixture()

    private lateinit var  redisOperationsFactory: RedisOperationsFactory

    override suspend fun beforeSpec(spec: Spec) {
        redisTestFixture.start()

        redisOperationsFactory = RedisOperationsFactoryImpl(
            connectionPool = redisTestFixture.getPool(),
            operationTimeout = REDIS_DEFAULT_OPERATION_TIMEOUT,
            lockTtl = REDIS_DEFAULT_LOCK_TTL,
        )
    }

    override suspend fun afterSpec(spec: Spec) {
        redisTestFixture.stop()
    }

    override suspend fun beforeTest(testCase: TestCase) {
        redisTestFixture.flushAll()
    }

    private suspend fun createStringStateManager(scopeName: String = "cache-${UUID.randomUUID()}") =
        StateManagerRedisImpl(
            scope = StateManagerScope(name = scopeName, team = "test"),
            redisOperations = redisOperationsFactory.create(
                prefix = "test:$scopeName",
                keySerializer = PersistenceStringSerializerImpl(),
                valueSerializer = PersistenceStringSerializerImpl(),
            ),
        )

    private suspend fun createBinaryStateManager(scopeName: String = "binary-${UUID.randomUUID()}") =
        StateManagerRedisImpl(
            scope = StateManagerScope(name = scopeName, team = "test"),
            redisOperations = redisOperationsFactory.create(
                prefix = "test:$scopeName",
                keySerializer = PersistenceStringSerializerImpl(),
                valueSerializer = object : StateManagerSerializer<ByteArray> {
                    override fun serialize(value: ByteArray) = value
                    override fun deserialize(value: ByteArray) = value
                },
            ),
        )

    private suspend fun cleanupScope(scopePrefix: String) {
        redisTestFixture.deleteKeys("$scopePrefix:*")
    }

    init {

        // ========== Redis-specific tests ==========

        "isHealthy reports Up/Down correctly" {
            withChaosProxyContainer(
                container = RedisContainer(REDIS_LATEST_IMAGE),
                upstreamAlias = "redis",
                upstreamPort = 6379,
            ) {
                val proxyClient = RedisClient.create("redis://$proxyHost:$proxyPort")
                val proxyURI = RedisURI.create("redis://$proxyHost:$proxyPort")
                val proxyPool = AsyncConnectionPoolSupport.createBoundedObjectPoolAsync(
                    { proxyClient.connectAsync(ByteArrayCodec(), proxyURI).toCompletableFuture() },
                    BoundedPoolConfig.builder().maxTotal(5).build(),
                ).await()
                try {
                    val proxyFactory = RedisOperationsFactoryImpl(
                        connectionPool = proxyPool,
                        operationTimeout = 1.seconds,
                        lockTtl = REDIS_DEFAULT_LOCK_TTL,
                    )

                    val proxyStateManager = StateManagerRedisImpl(
                        scope = StateManagerScope(name = "test-health", team = "state-manager"),
                        redisOperations = proxyFactory.create(
                            prefix = "state-manager:test-health",
                            keySerializer = PersistenceStringSerializerImpl(),
                            valueSerializer = PersistenceStringSerializerImpl(),
                        ),
                    )

                    proxyStateManager.isHealthy() shouldBeRight  StateManagerIsHealthyResult.Up)

                    proxy.toxics().bandwidth("break", ToxicDirection.DOWNSTREAM, 0)

                    proxyStateManager.isHealthy() shouldBeRight  )
                        .shouldBeInstanceOf<StateManagerIsHealthyResult.Down>()
                        .message shouldContain "Failed to ping Redis"

                    proxy.toxics().get("break")?.remove()

                    proxyStateManager.isHealthy() shouldBeRight  StateManagerIsHealthyResult.Up)
                } finally {
                    proxyPool.closeAsync().await()
                    proxyClient.shutdownAsync().await()
                }
            }
        }

        "getOrPut waiters timeout when writer exceeds operationTimeout" {
            checkAll(
                Arb.string(),
                Arb.string(),
            ) { key, value ->
                val testScope = StateManagerScope(name = "test", team = "test-binary-${UUID.randomUUID()}")
                val stateManager = createStringStateManager(scopeName = testScope.name)

                val reader = StateManagerRedisImpl(
                    scope = testScope,
                    redisOperations = RedisOperationsFactoryImpl(
                        connectionPool = redisTestFixture.getPool(),
                        operationTimeout = 500.milliseconds,
                        lockTtl = REDIS_DEFAULT_LOCK_TTL,
                    ).create(
                        prefix = testScope.prefix,
                        keySerializer = PersistenceStringSerializerImpl(),
                        valueSerializer = PersistenceStringSerializerImpl(),
                    ),
                )
                val writerStarted = CompletableDeferred<Unit>()

                cleanupScope(testScope.prefix)

                coroutineScope {
                    val writerJob = async(Dispatchers.IO) {
                        stateManager.getOrPut(key) {
                            writerStarted.complete(Unit)
                            delay(2.seconds)
                            value
                        }
                    }

                    writerStarted.await()
                    val readerJob = async(Dispatchers.IO) {
                        reader.getOrPut(key) { "should-not-be-used" }
                    }

                    writerJob.await() shouldBeRight  )
                    readerJob.await().shouldBeLeft().shouldBeInstanceOf<StateManagerError.OperationTimeout>().also {
                        it.message shouldContain "Timed out waiting for cache"
                        it.duration shouldBe 500
                    }
                }
            }
        }

        "getOrPut across multiple state manager instances (distributed concurrency) calls factory once" {
            checkAll(
                Arb.string(),
                Arb.string(),
                Arb.int(2..20),
            ) { key, value, concurrentInstances ->
                val testScope = StateManagerScope(name = "cache", team = "test-binary-${UUID.randomUUID()}")

                val registries = List(size = concurrentInstances) {
                    StateManagerRedisImpl(
                        scope = testScope,
                        redisOperations = redisOperationsFactory.create(
                            prefix = testScope.prefix,
                            keySerializer = PersistenceStringSerializerImpl(),
                            valueSerializer = PersistenceStringSerializerImpl(),
                        ),
                    )
                }

                val factoryCalls = AtomicInteger(0)

                cleanupScope(testScope.prefix)

                coroutineScope {
                    registries.map { instance ->
                        async(Dispatchers.IO) {
                            instance.getOrPut(key) {
                                factoryCalls.incrementAndGet()
                                yield()
                                value
                            }
                        }
                    }.awaitAll()
                }

                factoryCalls.get() shouldBe 1
            }
        }

        // ========== Common State Manager tests ==========

        "put and get round-trip" {
            StateManagerTestTemplates.putAndGetRoundTrip { createStringStateManager() }.invoke()
        }

        "put and get round-trip of binary data" {
            StateManagerTestTemplates.putAndGetRoundTripOfBinaryData { createBinaryStateManager() }.invoke()
        }

        "put and get round-trip handle concurrent puts and gets" {
            StateManagerTestTemplates.putAndGetRoundTripHandleConcurrentPutsAndGets { createStringStateManager() }
                .invoke()
        }

        "concurrent evict and put do not leave the key in an inconsistent state" {
            StateManagerTestTemplates.concurrentEvictAndPutDoNotLeaveTheKeyInAnInconsistentState { createStringStateManager() }
                .invoke()
        }

        "put with existing key returns Updated" {
            StateManagerTestTemplates.putWithExistingKeyReturnsUpdated { createStringStateManager() }.invoke()
        }

        "put handle concurrent puts to the same key" {
            StateManagerTestTemplates.putHandleConcurrentPutsToTheSameKey { createStringStateManager() }.invoke()
        }

        "put with negative TTL returns InvalidArgument" {
            StateManagerTestTemplates.putWithNegativeTTLReturnsInvalidArgument { createStringStateManager() }.invoke()
        }

        "put with TTL expires the record" {
            StateManagerTestTemplates.putWithTTLExpiresTheRecord { createStringStateManager() }.invoke()
        }

        "contains checks if key exists without retrieving value" {
            StateManagerTestTemplates.containsChecksIfKeyExistsWithoutRetrievingValue { createStringStateManager() }
                .invoke()
        }

        "return the number of records saved in the current scope" {
            StateManagerTestTemplates.returnTheNumberOfRecordsSavedInTheCurrentScope { createStringStateManager() }
                .invoke()
        }

        "concurrent size and modifications yield eventually correct count" {
            StateManagerTestTemplates
                .concurrentSizeAndModificationsYieldEventuallyCorrectCount { createStringStateManager() }
                .invoke()
        }

        "getOrPut retrieves or saves value" {
            StateManagerTestTemplates.getOrPutRetrievesOrSavesValue { createStringStateManager() }.invoke()
        }

        "getOrPut handle concurrent calls to same key" {
            StateManagerTestTemplates.getOrPutHandleConcurrentCallsToSameKey { createStringStateManager() }.invoke()
        }

        "getOrPut propagates factory exceptions" {
            StateManagerTestTemplates.getOrPutPropagatesFactoryExceptions { createStringStateManager() }.invoke()
        }

        "getOrPut with negative TTL returns InvalidArgument" {
            StateManagerTestTemplates.getOrPutWithNegativeTTLReturnsInvalidArgument { createStringStateManager() }
                .invoke()
        }

        "getOrPut with TTL expires the record" {
            StateManagerTestTemplates.getOrPutWithTTLExpiresTheRecord { createStringStateManager() }.invoke()
        }

        "evict removes existing key" {
            StateManagerTestTemplates.evictRemovesExistingKey { createStringStateManager() }.invoke()
        }

        "keysPage returns paginated keys" {
            StateManagerTestTemplates.keysPageReturnsPaginatedKeys { createStringStateManager() }.invoke()
        }

        "keysPage with invalid limit returns InvalidArgument" {
            StateManagerTestTemplates.keysPageWithInvalidLimitReturnsInvalidArgument { createStringStateManager() }
                .invoke()
        }

        "setTTL changes expiry of existing record" {
            StateManagerTestTemplates.setTTLChangesExpiryOfExistingRecord { createStringStateManager() }.invoke()
        }

        "setTTL with negative TTL returns InvalidArgument" {
            StateManagerTestTemplates.setTTLWithNegativeTTLReturnsInvalidArgument { createStringStateManager() }
                .invoke()
        }

        "stress test: many concurrent operations on different keys" {
            StateManagerTestTemplates.stressTestManyConcurrentOperationsOnDifferentKeys { createStringStateManager() }
                .invoke()
        }
    }
}
