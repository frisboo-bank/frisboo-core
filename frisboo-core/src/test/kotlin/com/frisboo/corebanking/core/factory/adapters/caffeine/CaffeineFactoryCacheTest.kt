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
package com.frisboo.corebanking.core.factory.adapters.caffeine

import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class CaffeineFactoryCacheTest :
    StringSpec({

        val keyArb = Arb.string(minSize = 1, maxSize = 50)
        val valueArb = Arb.string(minSize = 1, maxSize = 50)

        fun createCache(
            maxSize: Long = 100,
            recordStats: Boolean = true,
            coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        ): FactoryCache<String, String> =
            CaffeineFactoryCache(
                maxSize = maxSize,
                expireAfterAccess = 5.minutes,
                recordStats = recordStats,
                coroutineScope = coroutineScope,
            )

        "rejects zero maxSize" {
            shouldThrow<IllegalArgumentException> {
                CaffeineFactoryCache<String, String>(
                    maxSize = 0,
                    expireAfterAccess = 5.minutes,
                )
            }.message shouldBe "maxSize must be greater than 0"
        }

        "rejects negative maxSize" {
            checkAll(Arb.long(min = Long.MIN_VALUE, max = -1)) { negativeSize ->
                shouldThrow<IllegalArgumentException> {
                    CaffeineFactoryCache<String, String>(
                        maxSize = negativeSize,
                        expireAfterAccess = 5.minutes,
                    )
                }.message shouldBe "maxSize must be greater than 0"
            }
        }

        "rejects zero expireAfterAccess" {
            shouldThrow<IllegalArgumentException> {
                CaffeineFactoryCache<String, String>(
                    maxSize = 10,
                    expireAfterAccess = 0.seconds,
                )
            }.message shouldBe "expireAfterAccess must be positive"
        }

        "rejects negative expireAfterAccess" {
            shouldThrow<IllegalArgumentException> {
                CaffeineFactoryCache<String, String>(
                    maxSize = 10,
                    expireAfterAccess = (-1).seconds,
                )
            }.message shouldBe "expireAfterAccess must be positive"
        }

        "get computes and caches a value" {
            checkAll(keyArb, valueArb) { key, value ->
                val cache = createCache()
                val computations = AtomicInteger(0)

                val first =
                    cache.get(key) {
                        computations.incrementAndGet()
                        value
                    }
                val second =
                    cache.get(key) {
                        computations.incrementAndGet()
                        "should-not-be-used"
                    }

                first shouldBe value
                second shouldBe value
                computations.get() shouldBe 1
            }
        }

        "getIfPresent returns null for absent key" {
            val cache = createCache()

            checkAll(keyArb) { key ->
                cache.getIfPresent(key).shouldBeNull()
            }
        }

        "getIfPresent returns value for present key" {
            val cache = createCache()

            checkAll(keyArb, valueArb) { key, value ->
                cache.put(key, value)
                cache.getIfPresent(key) shouldBe value
            }
        }

        "put overwrites existing value" {
            checkAll(keyArb, valueArb, valueArb) { key, first, second ->
                val cache = createCache()
                cache.put(key, first)
                cache.put(key, second)

                cache.getIfPresent(key) shouldBe second
                cache.estimatedSize() shouldBeGreaterThanOrEqual 1
            }
        }

        "invalidate removes a cached entry" {
            checkAll(keyArb, valueArb) { key, value ->
                val cache = createCache()
                cache.put(key, value)
                cache.getIfPresent(key).shouldNotBeNull()

                cache.invalidate(key)
                cache.getIfPresent(key).shouldBeNull()
            }
        }

        "estimatedSize reflects number of entries" {
            checkAll(keyArb, keyArb, keyArb) { a, b, c ->
                val keys = setOf(a, b, c)
                val cache = createCache()
                cache.estimatedSize() shouldBe 0

                keys.forEach { cache.put(it, "v") }

                cache.estimatedSize() shouldBe keys.size.toLong()
            }
        }

        "stats returns non-null when recordStats is true" {
            checkAll(keyArb, valueArb) { key, value ->
                val cache = createCache(recordStats = true)

                cache.get(key) { value }
                cache.get(key) { value }

                val stats = cache.stats()
                stats.shouldNotBeNull()
                stats.hitCount shouldBe 1
                stats.missCount shouldBe 1
                stats.loadSuccessCount shouldBe 1
            }
        }

        "eviction listener fires on size-based eviction" {
            checkAll(keyArb, valueArb, keyArb, valueArb) { key1, val1, key2, val2 ->
                if (key1 == key2) return@checkAll

                val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
                val cache =
                    CaffeineFactoryCache<String, String>(
                        maxSize = 1,
                        expireAfterAccess = 5.minutes,
                        coroutineScope = scope,
                    )

                val evicted = CompletableDeferred<Pair<String?, String?>>()
                cache.setEvictionListener { k, v ->
                    evicted.complete(k to v)
                }

                cache.put(key1, val1)
                cache.put(key2, val2)

                val (evictedKey, evictedValue) = evicted.await()
                evictedKey shouldBe key1
                evictedValue shouldBe val1
            }
        }

        "setEvictionListener replaces previous listener" {
            checkAll(keyArb, valueArb, keyArb, valueArb) { key1, val1, key2, val2 ->
                if (key1 == key2) return@checkAll

                val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
                val cache =
                    CaffeineFactoryCache<String, String>(
                        maxSize = 1,
                        expireAfterAccess = 5.minutes,
                        coroutineScope = scope,
                    )

                val firstListenerCalls = AtomicInteger(0)
                cache.setEvictionListener { _, _ -> firstListenerCalls.incrementAndGet() }

                val secondEvicted = CompletableDeferred<String?>()
                cache.setEvictionListener { k, _ -> secondEvicted.complete(k) }

                cache.put(key1, val1)
                cache.put(key2, val2)

                secondEvicted.await() shouldBe key1
                firstListenerCalls.get() shouldBe 0
            }
        }

        "setEvictionListener with null disables eviction callback" {
            checkAll(keyArb, valueArb, keyArb, valueArb) { key1, val1, key2, val2 ->
                if (key1 == key2) return@checkAll

                val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
                val cache =
                    CaffeineFactoryCache<String, String>(
                        maxSize = 1,
                        expireAfterAccess = 5.minutes,
                        coroutineScope = scope,
                    )

                val calls = AtomicInteger(0)
                cache.setEvictionListener { _, _ -> calls.incrementAndGet() }
                cache.setEvictionListener(null)

                cache.put(key1, val1)
                cache.put(key2, val2)

                delay(100)
                calls.get() shouldBe 0
            }
        }

        "concurrent get with same key invokes mappingFunction at most once" {
            checkAll(keyArb, valueArb) { key, value ->
                val cache = createCache()
                val computations = AtomicInteger(0)
                val iterations = 100

                val results =
                    withContext(Dispatchers.Default) {
                        (1..iterations)
                            .map {
                                async {
                                    cache.get(key) {
                                        computations.incrementAndGet()
                                        value
                                    }
                                }
                            }.awaitAll()
                    }

                results.forEach { it shouldBe value }
                computations.get() shouldBe 1
            }
        }

        "concurrent put with different keys creates all entries" {
            val cache = createCache(maxSize = 200)
            val iterations = 100

            withContext(Dispatchers.Default) {
                (1..iterations)
                    .map { i ->
                        async {
                            cache.put("key-$i", "value-$i")
                        }
                    }.awaitAll()
            }

            cache.estimatedSize() shouldBe iterations.toLong()
        }

        "concurrent put and invalidate do not corrupt state" {
            val cache = createCache(maxSize = 200)
            val iterations = 100

            withContext(Dispatchers.Default) {
                (1..iterations)
                    .map { i ->
                        async { cache.put("key-$i", "value-$i") }
                    }.awaitAll()
            }

            withContext(Dispatchers.Default) {
                (1..iterations)
                    .map { i ->
                        async { cache.invalidate("key-$i") }
                    }.awaitAll()
            }

            cache.estimatedSize() shouldBe 0
        }
    })
