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
package com.frisboo.corebanking.registry.adapters.local.caffeine

import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.SetTtlResult
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class CaffeineRegistryImplTest :
    StringSpec({
        val scope = RegistryScope(name = "test-scope", team = "test-team")

        "get returns null for missing key" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.get("missing") shouldBe null
            }
        }

        "put returns Created for new key and Updated for existing key" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1", ttl = null) shouldBe PutResult.Created
                registry.put("k1", "v2", ttl = null) shouldBe PutResult.Updated
            }
        }

        "put with negative TTL returns Failed with InvalidTtl" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                val result = registry.put("k1", "v1", ttl = (-1).milliseconds)

                val failed = result.shouldBeInstanceOf<PutResult.Failed>()
                failed.error.shouldBeInstanceOf<RegistryError.InvalidTtl>()
            }
        }

        "get returns value after put" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1", ttl = null)

                registry.get("k1") shouldBe "v1"
            }
        }

        "getOrPut calls factory when key missing and returns cached value on second call" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)
                val counter = AtomicInteger(0)

                val first =
                    registry.getOrPut("k1", ttl = null) {
                        counter.incrementAndGet()
                        "computed"
                    }
                val second =
                    registry.getOrPut("k1", ttl = null) {
                        counter.incrementAndGet()
                        "other"
                    }

                first shouldBe GetOrPutResult.Created("computed")
                second shouldBe GetOrPutResult.Found("computed")
                counter.get() shouldBe 1
            }
        }

        "getOrPut with negative TTL returns Failed with InvalidTtl" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                val result = registry.getOrPut("k1", ttl = (-1).milliseconds) { "value" }
                val failed = result.shouldBeInstanceOf<GetOrPutResult.Failed>()
                failed.error.shouldBeInstanceOf<RegistryError.InvalidTtl>()
            }
        }

        "contains returns true for existing and false for missing" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("present", "v", ttl = null)

                registry.contains("present") shouldBe true
                registry.contains("missing") shouldBe false
            }
        }

        "evict returns Evicted for existing key and NotFound for missing" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)
                registry.put("present", "v", ttl = null)

                registry.evict("present") shouldBe EvictResult.Evicted
                registry.evict("missing") shouldBe EvictResult.NotFound
            }
        }

        "size returns correct count" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("a", "v1", ttl = null)
                registry.put("b", "v2", ttl = null)
                registry.put("c", "v3", ttl = null)

                registry.size() shouldBe 3L
            }
        }

        "keys returns all non-expired keys" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("short", "v1", ttl = 50.milliseconds)
                registry.put("long", "v2", ttl = 5.seconds)
                Thread.sleep(200)

                registry.keys() shouldBe setOf("long")
            }
        }

        "keysPage returns paginated results with cursor" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("a", "va", ttl = null)
                registry.put("b", "vb", ttl = null)
                registry.put("c", "vc", ttl = null)

                val page1 = registry.keysPage(cursor = null, limit = 2)
                val page2 = registry.keysPage(cursor = page1.nextCursor, limit = 2)

                page1.items shouldBe listOf("a", "b")
                page1.nextCursor shouldBe "2"
                page2.items shouldBe listOf("c")
                page2.nextCursor shouldBe null
            }
        }

        "setTTL applies new TTL to existing key" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1", ttl = 1.seconds)

                registry.setTTL("k1", ttl = 50.milliseconds) shouldBe SetTtlResult.Applied
                Thread.sleep(200)
                registry.cleanupExpired()

                registry.get("k1") shouldBe null
            }
        }

        "setTTL returns KeyNotFound for missing key" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.setTTL("missing", ttl = 50.milliseconds) shouldBe SetTtlResult.KeyNotFound
            }
        }

        "setTTL with negative TTL returns Failed with InvalidTtl" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                val result = registry.setTTL("k1", ttl = (-1).milliseconds)

                val failed = result.shouldBeInstanceOf<SetTtlResult.Failed>()
                failed.error.shouldBeInstanceOf<RegistryError.InvalidTtl>()
            }
        }

        "cleanupExpired removes expired entries" {
            runBlocking {
                val registry = CaffeineRegistryImpl<String, String>(scope = scope)

                registry.put("k1", "v1", ttl = 50.milliseconds)
                registry.put("k2", "v2", ttl = 50.milliseconds)
                // Verify entries exist before expiration.
                registry.get("k1").shouldNotBeNull()
                registry.get("k2").shouldNotBeNull()

                Thread.sleep(200)
                registry.cleanupExpired()

                registry.get("k1").shouldBeNull()
                registry.get("k2").shouldBeNull()
            }
        }

        "defaultTtl is used when no explicit TTL provided" {
            runBlocking {
                val registry =
                    CaffeineRegistryImpl<String, String>(
                        scope = scope,
                        defaultTtl = 50.milliseconds,
                    )

                registry.put("k1", "v1", ttl = null)
                registry.get("k1") shouldBe "v1"
                Thread.sleep(200)
                registry.cleanupExpired()

                registry.get("k1") shouldBe null
            }
        }

        "maximumSize eviction works and size stays bounded" {
            runBlocking {
                val registry =
                    CaffeineRegistryImpl<String, String>(
                        scope = scope,
                        maximumSize = 5,
                    )

                (1..10).forEach { index ->
                    registry.put("k$index", "v$index", ttl = null)
                }

                val size = registry.size()
                (size <= 5L) shouldBe true
            }
        }
    })
