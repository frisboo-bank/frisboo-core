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
import com.frisboo.corebanking.registry.contracts.RegistryMetrics
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.seconds

internal class InstrumentedRegistryImplTest :
    StringSpec({
        val scope = RegistryScope(name = "metrics-test", team = "test-team")

        fun createInstrumented(): InstrumentedRegistryImpl<String, String> {
            val delegate = InMemoryRegistryImpl<String, String>(scope)
            return InstrumentedRegistryImpl(delegate)
        }

        "initial counters are all zero" {
            val registry = createInstrumented()

            registry.hitCount shouldBe 0L
            registry.missCount shouldBe 0L
            registry.putCount shouldBe 0L
            registry.evictionCount shouldBe 0L
            registry.failureCount shouldBe 0L
        }

        "get increments hitCount when key exists" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                registry.get("k1")

                registry.hitCount shouldBe 1L
                registry.missCount shouldBe 0L
            }
        }

        "get increments missCount when key is absent" {
            runBlocking {
                val registry = createInstrumented()

                registry.get("missing")

                registry.hitCount shouldBe 0L
                registry.missCount shouldBe 1L
            }
        }

        "contains increments hitCount when key exists" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                registry.contains("k1")

                registry.hitCount shouldBe 1L
            }
        }

        "contains increments missCount when key is absent" {
            runBlocking {
                val registry = createInstrumented()

                registry.contains("missing")

                registry.missCount shouldBe 1L
            }
        }

        "put increments putCount on Created" {
            runBlocking {
                val registry = createInstrumented()

                val result = registry.put("k1", "v1")

                result.shouldBeInstanceOf<PutResult.Created>()
                registry.putCount shouldBe 1L
            }
        }

        "put increments putCount on Updated" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                val result = registry.put("k1", "v2")

                result.shouldBeInstanceOf<PutResult.Updated>()
                registry.putCount shouldBe 2L
            }
        }

        "put increments failureCount on Failed" {
            runBlocking {
                val registry = createInstrumented()

                val result = registry.put("k1", "v1", ttl = (-1).seconds)

                result.shouldBeInstanceOf<PutResult.Failed>()
                registry.failureCount shouldBe 1L
                registry.putCount shouldBe 0L
            }
        }

        "getOrPut increments hitCount on Found" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                val result = registry.getOrPut("k1") { "v2" }

                result.shouldBeInstanceOf<GetOrPutResult.Found<String>>()
                result.value shouldBe "v1"
                registry.hitCount shouldBe 1L
                registry.missCount shouldBe 0L
            }
        }

        "getOrPut increments missCount and putCount on Created" {
            runBlocking {
                val registry = createInstrumented()

                val result = registry.getOrPut("k1") { "v1" }

                result.shouldBeInstanceOf<GetOrPutResult.Created<String>>()
                result.value shouldBe "v1"
                registry.missCount shouldBe 1L
                registry.putCount shouldBe 1L
                registry.hitCount shouldBe 0L
            }
        }

        "getOrPut increments failureCount on Failed" {
            runBlocking {
                val registry = createInstrumented()

                val result = registry.getOrPut("k1", ttl = (-1).seconds) { "v1" }

                result.shouldBeInstanceOf<GetOrPutResult.Failed>()
                registry.failureCount shouldBe 1L
                registry.putCount shouldBe 0L
            }
        }

        "evict increments evictionCount on Evicted" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                val result = registry.evict("k1")

                result.shouldBeInstanceOf<EvictResult.Evicted>()
                registry.evictionCount shouldBe 1L
            }
        }

        "evict does not increment evictionCount on NotFound" {
            runBlocking {
                val registry = createInstrumented()

                val result = registry.evict("missing")

                result.shouldBeInstanceOf<EvictResult.NotFound>()
                registry.evictionCount shouldBe 0L
            }
        }

        "setTTL does not increment failureCount on Applied" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                registry.setTTL("k1", 30.seconds)

                registry.failureCount shouldBe 0L
            }
        }

        "setTTL increments failureCount on invalid TTL" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                registry.setTTL("k1", (-1).seconds)

                registry.failureCount shouldBe 1L
            }
        }

        "implements RegistryMetrics interface" {
            val registry = createInstrumented()

            val metrics: RegistryMetrics = registry
            metrics.hitCount shouldBe 0L
        }

        "counters accumulate across multiple operations" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")
                registry.put("k2", "v2")
                registry.get("k1")
                registry.get("k2")
                registry.get("missing")
                registry.evict("k1")

                registry.putCount shouldBe 2L
                registry.hitCount shouldBe 2L
                registry.missCount shouldBe 1L
                registry.evictionCount shouldBe 1L
                registry.failureCount shouldBe 0L
            }
        }

        "delegates read operations unchanged" {
            runBlocking {
                val registry = createInstrumented()

                registry.put("k1", "v1")

                registry.get("k1") shouldBe "v1"
                registry.contains("k1") shouldBe true
                registry.size() shouldBe 1L
                registry.keysPage(null, Int.MAX_VALUE).items.toSet() shouldBe setOf("k1")
            }
        }
    })
