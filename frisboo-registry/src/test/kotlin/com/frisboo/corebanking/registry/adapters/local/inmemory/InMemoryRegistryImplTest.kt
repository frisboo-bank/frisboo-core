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
package com.frisboo.corebanking.registry.adapters.local.inmemory

import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.SetTtlResult
import io.kotest.extensions.clock.TestClock as KotestTestClock
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Wraps Kotest's [java.time.Clock]-based TestClock to implement [kotlin.time.Clock]
 * for use with [InMemoryRegistryImpl] which expects [kotlin.time.Clock].
 */
private class WrappedTestClock(
    private val delegate: KotestTestClock = KotestTestClock(java.time.Instant.EPOCH, ZoneOffset.UTC),
) : Clock {
    override fun now(): Instant =
        Instant.fromEpochMilliseconds(delegate.instant().toEpochMilli())

    fun advanceBy(duration: Duration) {
        delegate.plus(duration)
    }
}

internal class InMemoryRegistryImplTest :
    StringSpec({
        val scope = RegistryScope(name = "test-scope", team = "test-team")

        "get returns null for missing key" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.get("missing") shouldBe null
            }
        }

        "put returns Created for new key and Updated for existing key" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1") shouldBe PutResult.Created
                registry.put("k1", "v2") shouldBe PutResult.Updated
            }
        }

        "put with negative TTL returns Failed with InvalidTtl" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                val result = registry.put("k1", "v1", ttl = (-1).seconds)
                val failed = result.shouldBeInstanceOf<PutResult.Failed>()
                failed.error.shouldBeInstanceOf<RegistryError.InvalidTtl>()
            }
        }

        "get returns value after put" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1") shouldBe PutResult.Created
                registry.get("k1") shouldBe "v1"
            }
        }

        "get returns null after entry expires" {
            runBlocking {
                val testClock = WrappedTestClock()
                val registry = InMemoryRegistryImpl<String, String>(scope = scope, clock = testClock)

                registry.put("k1", "v1", ttl = 2.seconds) shouldBe PutResult.Created
                testClock.advanceBy(3.seconds)

                registry.get("k1") shouldBe null
            }
        }

        "getOrPut calls factory when key missing and returns cached value on second call" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)
                val factoryCalls = AtomicInteger(0)

                val first =
                    registry.getOrPut("k1", ttl = null) {
                        factoryCalls.incrementAndGet()
                        "computed"
                    }
                val second =
                    registry.getOrPut("k1", ttl = null) {
                        factoryCalls.incrementAndGet()
                        "other"
                    }

                first shouldBe GetOrPutResult.Created("computed")
                second shouldBe GetOrPutResult.Found("computed")
                factoryCalls.get() shouldBe 1
            }
        }

        "getOrPut with negative TTL returns Failed with InvalidTtl" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                val result = registry.getOrPut("k1", ttl = (-1).seconds) { "value" }
                val failed = result.shouldBeInstanceOf<GetOrPutResult.Failed>()
                failed.error.shouldBeInstanceOf<RegistryError.InvalidTtl>()
            }
        }

        "contains returns true for existing and false for missing" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1") shouldBe PutResult.Created

                registry.contains("k1") shouldBe true
                registry.contains("missing") shouldBe false
            }
        }

        "contains evicts expired entries" {
            runBlocking {
                val testClock = WrappedTestClock()
                val registry = InMemoryRegistryImpl<String, String>(scope = scope, clock = testClock)

                registry.put("k1", "v1", ttl = 1.seconds) shouldBe PutResult.Created
                testClock.advanceBy(2.seconds)

                registry.contains("k1") shouldBe false
                registry.size() shouldBe 0L
            }
        }

        "evict returns Evicted for existing key and NotFound for missing key" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1") shouldBe PutResult.Created

                registry.evict("k1") shouldBe EvictResult.Evicted
                registry.evict("missing") shouldBe EvictResult.NotFound
            }
        }

        "size counts only non-expired entries" {
            runBlocking {
                val testClock = WrappedTestClock()
                val registry = InMemoryRegistryImpl<String, String>(scope = scope, clock = testClock)

                registry.put("k1", "v1", ttl = 1.seconds) shouldBe PutResult.Created
                registry.put("k2", "v2") shouldBe PutResult.Created
                testClock.advanceBy(2.seconds)

                registry.size() shouldBe 1L
            }
        }

        "keys returns only non-expired keys" {
            runBlocking {
                val testClock = WrappedTestClock()
                val registry = InMemoryRegistryImpl<String, String>(scope = scope, clock = testClock)

                registry.put("alive", "v1") shouldBe PutResult.Created
                registry.put("expired", "v2", ttl = 1.seconds) shouldBe PutResult.Created
                testClock.advanceBy(2.seconds)

                registry.keys() shouldBe setOf("alive")
            }
        }

        "keysPage returns paginated results with cursor" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.put("b", "vb") shouldBe PutResult.Created
                registry.put("a", "va") shouldBe PutResult.Created
                registry.put("c", "vc") shouldBe PutResult.Created

                val firstPage = registry.keysPage(cursor = null, limit = 2)
                firstPage.items shouldBe listOf("a", "b")
                firstPage.nextCursor shouldBe "2"

                val secondPage = registry.keysPage(cursor = firstPage.nextCursor, limit = 2)
                secondPage.items shouldBe listOf("c")
                secondPage.nextCursor shouldBe null
            }
        }

        "setTTL applies new TTL to existing key" {
            runBlocking {
                val testClock = WrappedTestClock()
                val registry = InMemoryRegistryImpl<String, String>(scope = scope, clock = testClock)

                registry.put("k1", "v1") shouldBe PutResult.Created
                registry.setTTL("k1", 2.seconds) shouldBe SetTtlResult.Applied
                testClock.advanceBy(1.seconds)
                registry.get("k1") shouldBe "v1"
                testClock.advanceBy(2.seconds)
                registry.get("k1") shouldBe null
            }
        }

        "setTTL returns KeyNotFound for missing key" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                registry.setTTL("missing", 1.seconds) shouldBe SetTtlResult.KeyNotFound
            }
        }

        "setTTL with negative TTL returns Failed with InvalidTtl" {
            runBlocking {
                val registry = InMemoryRegistryImpl<String, String>(scope = scope)

                val result = registry.setTTL("k1", (-1).seconds)
                val failed = result.shouldBeInstanceOf<SetTtlResult.Failed>()
                failed.error.shouldBeInstanceOf<RegistryError.InvalidTtl>()
            }
        }

        "cleanupExpired removes expired entries and returns count" {
            runBlocking {
                val testClock = WrappedTestClock()
                val registry = InMemoryRegistryImpl<String, String>(scope = scope, clock = testClock)

                registry.put("k1", "v1", ttl = 1.seconds) shouldBe PutResult.Created
                registry.put("k2", "v2", ttl = 1.seconds) shouldBe PutResult.Created
                registry.put("k3", "v3") shouldBe PutResult.Created
                testClock.advanceBy(2.seconds)

                registry.cleanupExpired() shouldBe 2
                registry.size() shouldBe 1L
                registry.keys() shouldBe setOf("k3")
            }
        }
    })
