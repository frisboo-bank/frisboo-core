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
package com.frisboo.corebanking.registry.decorators

import com.frisboo.corebanking.registry.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.contracts.ResilienceExecutor
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.SetTtlResult
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

/**
 * Fake [ResilienceExecutor] that always runs primary.
 */
private class AlwaysPrimaryExecutor : ResilienceExecutor {
    override suspend fun <T> execute(
        block: suspend () -> T,
        fallback: suspend () -> T,
    ): T = block()
}

/**
 * Fake [ResilienceExecutor] that runs primary, falls back on exception.
 */
private class FallbackOnFailureExecutor : ResilienceExecutor {
    override suspend fun <T> execute(
        block: suspend () -> T,
        fallback: suspend () -> T,
    ): T =
        try {
            block()
        } catch (e: Exception) {
            if (e is kotlin.coroutines.cancellation.CancellationException) throw e
            fallback()
        }
}

/**
 * [Registry] wrapper that throws on every call, simulating a failed primary.
 */
private class FailingRegistry<K : Any, V : Any> : Registry<K, V> {
    override suspend fun get(key: K): V? = throw RuntimeException("primary down")

    override suspend fun getOrPut(
        key: K,
        ttl: kotlin.time.Duration?,
        factory: suspend () -> V,
    ): GetOrPutResult<V> = throw RuntimeException("primary down")

    override suspend fun contains(key: K): Boolean = throw RuntimeException("primary down")

    override suspend fun size(): Long = throw RuntimeException("primary down")

    override suspend fun put(
        key: K,
        value: V,
        ttl: kotlin.time.Duration?,
    ): PutResult = throw RuntimeException("primary down")

    override suspend fun evict(key: K): EvictResult = throw RuntimeException("primary down")

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): com.frisboo.corebanking.registry.models.RegistryPage<K> = throw RuntimeException("primary down")

    override suspend fun setTTL(
        key: K,
        ttl: kotlin.time.Duration,
    ): SetTtlResult = throw RuntimeException("primary down")
}

internal class ResilientRegistryImplTest :
    StringSpec({
        val scope = RegistryScope(name = "resilient-test", team = "test-team")

        "get delegates to primary when healthy" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("k1", "primary-value")
                fallback.put("k1", "fallback-value")

                registry.get("k1") shouldBe "primary-value"
            }
        }

        "get falls back when primary fails" {
            runBlocking {
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(FailingRegistry(), fallback, FallbackOnFailureExecutor())

                fallback.put("k1", "fallback-value")

                registry.get("k1") shouldBe "fallback-value"
            }
        }

        "put delegates to primary and returns Created" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                registry.put("k1", "v1") shouldBe PutResult.Created
                primary.get("k1") shouldBe "v1"
            }
        }

        "put falls back when primary fails" {
            runBlocking {
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(FailingRegistry(), fallback, FallbackOnFailureExecutor())

                registry.put("k1", "v1") shouldBe PutResult.Created
                fallback.get("k1") shouldBe "v1"
            }
        }

        "evict delegates to primary" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("k1", "v1")

                registry.evict("k1") shouldBe EvictResult.Evicted
                registry.evict("missing") shouldBe EvictResult.NotFound
            }
        }

        "evict falls back when primary fails" {
            runBlocking {
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(FailingRegistry(), fallback, FallbackOnFailureExecutor())

                fallback.put("k1", "v1")

                registry.evict("k1") shouldBe EvictResult.Evicted
            }
        }

        "contains delegates to primary" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("k1", "v1")

                registry.contains("k1") shouldBe true
                registry.contains("missing") shouldBe false
            }
        }

        "size delegates to primary" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("k1", "v1")
                primary.put("k2", "v2")

                registry.size() shouldBe 2L
            }
        }

        "keysPage delegates to primary" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("a", "v1")
                primary.put("b", "v2")

                registry.keysPage(null, Int.MAX_VALUE).items.toSet() shouldBe setOf("a", "b")
            }
        }

        "keysPage delegates to primary" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("a", "v1")
                primary.put("b", "v2")

                val page = registry.keysPage(cursor = null, limit = 10)
                page.items shouldBe listOf("a", "b")
                page.nextCursor shouldBe null
            }
        }

        "setTTL delegates to primary" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("k1", "v1")

                registry.setTTL("k1", 30.seconds) shouldBe SetTtlResult.Applied
                registry.setTTL("missing", 30.seconds) shouldBe SetTtlResult.KeyNotFound
            }
        }

        "setTTL falls back when primary fails" {
            runBlocking {
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(FailingRegistry(), fallback, FallbackOnFailureExecutor())

                fallback.put("k1", "v1")

                registry.setTTL("k1", 30.seconds) shouldBe SetTtlResult.Applied
            }
        }

        "getOrPut delegates to primary when healthy" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                val result = registry.getOrPut("k1") { "computed" }

                result shouldBe GetOrPutResult.Created("computed")
                primary.get("k1") shouldBe "computed"
            }
        }

        "getOrPut falls back when primary fails and factory is called only once" {
            runBlocking {
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(FailingRegistry(), fallback, FallbackOnFailureExecutor())
                val factoryCalls = AtomicInteger(0)

                val result =
                    registry.getOrPut("k1") {
                        factoryCalls.incrementAndGet()
                        "computed"
                    }

                result shouldBe GetOrPutResult.Created("computed")
                factoryCalls.get() shouldBe 1
                fallback.get("k1") shouldBe "computed"
            }
        }

        "getOrPut returns cached primary value without calling factory" {
            runBlocking {
                val primary = InMemoryRegistryImpl<String, String>(scope)
                val fallback = InMemoryRegistryImpl<String, String>(scope)
                val registry = ResilientRegistryImpl(primary, fallback, AlwaysPrimaryExecutor())

                primary.put("k1", "existing")
                val factoryCalls = AtomicInteger(0)

                val result =
                    registry.getOrPut("k1") {
                        factoryCalls.incrementAndGet()
                        "should-not-be-used"
                    }

                result shouldBe GetOrPutResult.Found("existing")
                factoryCalls.get() shouldBe 0
            }
        }
    })
