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
package com.frisboo.corebanking.registry.adapters.local

import com.frisboo.corebanking.registry.adapters.local.caffeine.CaffeineRegistryImpl
import com.frisboo.corebanking.registry.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.registry.contracts.LocalRegistry
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

/**
 * Concurrency stress tests for local registry implementations (convention rule #45).
 *
 * Each test runs >= 50 parallel coroutines against a shared registry to verify
 * thread-safety of put, getOrPut, evict, and size operations.
 */
internal class RegistryConcurrencyTest :
    StringSpec({
        val scope = RegistryScope(name = "concurrency-test", team = "test-team")
        val iterations = 100

        "InMemoryRegistryImpl: concurrent put operations are thread-safe" {
            val registry = InMemoryRegistryImpl<String, String>(scope)

            val results =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.put("key-$i", "value-$i", ttl = 30.seconds)
                    }
                }.awaitAll()

            results.forEach { it.shouldBeInstanceOf<PutResult.Created>() }
            registry.size() shouldBe iterations.toLong()
        }

        "InMemoryRegistryImpl: concurrent getOrPut returns consistent value for same key" {
            val registry = InMemoryRegistryImpl<String, String>(scope)

            val results =
                (1..iterations).map {
                    async(Dispatchers.Default) {
                        registry.getOrPut("shared-key", ttl = 30.seconds) {
                            "computed"
                        }
                    }
                }.awaitAll()

            results.forEach { it.value shouldBe "computed" }
            registry.get("shared-key") shouldBe "computed"
        }

        "InMemoryRegistryImpl: concurrent put and evict do not corrupt state" {
            val registry = InMemoryRegistryImpl<String, String>(scope)

            val putJobs =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.put("key-$i", "value-$i")
                    }
                }
            val evictJobs =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.evict("key-$i")
                    }
                }

            putJobs.awaitAll()
            evictJobs.awaitAll()

            val size = registry.size()
            val keys = registry.keysPage(null, Int.MAX_VALUE).items
            size shouldBe keys.size.toLong()
        }

        "CaffeineRegistryImpl: concurrent put operations are thread-safe" {
            val registry = CaffeineRegistryImpl<String, String>(scope)

            val results =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.put("key-$i", "value-$i", ttl = 30.seconds)
                    }
                }.awaitAll()

            results.forEach { it.shouldBeInstanceOf<PutResult.Created>() }
            registry.size() shouldBe iterations.toLong()
        }

        "CaffeineRegistryImpl: concurrent getOrPut invokes factory exactly once per key" {
            val registry = CaffeineRegistryImpl<String, String>(scope)
            val factoryCalls = AtomicInteger(0)

            val results =
                (1..iterations).map {
                    async(Dispatchers.Default) {
                        registry.getOrPut("shared-key", ttl = 30.seconds) {
                            factoryCalls.incrementAndGet()
                            "computed"
                        }
                    }
                }.awaitAll()

            val createdCount = results.count { it is GetOrPutResult.Created }
            val foundCount = results.count { it is GetOrPutResult.Found }

            createdCount shouldBe 1
            foundCount shouldBe iterations - 1
            factoryCalls.get() shouldBe 1
            results.forEach { it.value shouldBe "computed" }
        }

        "CaffeineRegistryImpl: concurrent put and evict do not corrupt state" {
            val registry = CaffeineRegistryImpl<String, String>(scope)

            val putJobs =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.put("key-$i", "value-$i", ttl = 30.seconds)
                    }
                }
            val evictJobs =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.evict("key-$i")
                    }
                }

            putJobs.awaitAll()
            evictJobs.awaitAll()

            val size = registry.size()
            val keys = registry.keysPage(null, Int.MAX_VALUE).items
            size shouldBe keys.size.toLong()
        }

        "InMemoryRegistryImpl: concurrent getOrPut with different keys creates all entries" {
            val registry = InMemoryRegistryImpl<String, String>(scope)

            val results =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.getOrPut("unique-key-$i", ttl = 30.seconds) { "value-$i" }
                    }
                }.awaitAll()

            results.forEach { it.shouldBeInstanceOf<GetOrPutResult.Created<String>>() }
            registry.size() shouldBe iterations.toLong()
        }

        "CaffeineRegistryImpl: concurrent getOrPut with different keys creates all entries" {
            val registry = CaffeineRegistryImpl<String, String>(scope)

            val results =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        registry.getOrPut("unique-key-$i", ttl = 30.seconds) { "value-$i" }
                    }
                }.awaitAll()

            results.forEach { it.shouldBeInstanceOf<GetOrPutResult.Created<String>>() }
            registry.size() shouldBe iterations.toLong()
        }
    })
