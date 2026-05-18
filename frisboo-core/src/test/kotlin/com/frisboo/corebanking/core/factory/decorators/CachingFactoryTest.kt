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
package com.frisboo.corebanking.core.factory.decorators

import com.frisboo.corebanking.core.factory.contracts.AsyncFactory
import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import com.frisboo.corebanking.core.factory.models.FactoryCacheStats
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private data class TestConfig(
    val version: Int,
)

private data class TestInstance(
    val name: String,
    val version: Int,
)

private class FakeFactory(
    private val creationDelay: Long = 0,
) : AsyncFactory<TestConfig, TestInstance> {
    val creationCount = AtomicInteger(0)

    override suspend fun getOrCreate(
        name: String,
        config: TestConfig,
    ): TestInstance {
        creationCount.incrementAndGet()
        if (creationDelay > 0) delay(creationDelay)
        return TestInstance(name = name, version = config.version)
    }

    override suspend fun evict(name: String): Unit = Unit

    override suspend fun estimatedSize(): Long = 0
}

private class FakeCache : FactoryCache<String, TestInstance> {
    private val store = ConcurrentHashMap<String, TestInstance>()
    private var evictionListener: (suspend (String?, TestInstance?) -> Unit)? = null

    override suspend fun get(
        key: String,
        mappingFunction: suspend (String) -> TestInstance,
    ): TestInstance = store.getOrPut(key) { throw UnsupportedOperationException() }

    override suspend fun getIfPresent(key: String): TestInstance? = store[key]

    override suspend fun put(
        key: String,
        value: TestInstance,
    ) {
        store[key] = value
    }

    override suspend fun invalidate(key: String) {
        val removed = store.remove(key)
        if (removed != null) {
            evictionListener?.invoke(key, removed)
        }
    }

    override suspend fun estimatedSize(): Long = store.size.toLong()

    override suspend fun stats(): FactoryCacheStats? = null

    override fun setEvictionListener(listener: (suspend (String?, TestInstance?) -> Unit)?) {
        evictionListener = listener
    }
}

internal class CachingFactoryTest :
    StringSpec(
        {

            val keyArb = Arb.string(minSize = 1, maxSize = 50)

            fun createFactory(
                cache: FactoryCache<String, TestInstance> = FakeCache(),
                delegate: FakeFactory = FakeFactory(),
                matchesConfig: (TestInstance, TestConfig) -> Boolean = { inst, cfg -> inst.version == cfg.version },
            ): Triple<CachingFactory<TestConfig, TestInstance>, FakeFactory, FactoryCache<String, TestInstance>> {
                val factory =
                    CachingFactory(
                        cache = cache,
                        delegate = delegate,
                        matchesConfig = matchesConfig,
                    )
                return Triple(factory, delegate, cache)
            }

            "same name and config returns the same instance" {
                checkAll(keyArb) { name ->
                    val (factory, delegate) = createFactory()
                    val config = TestConfig(version = 1)

                    val first = factory.getOrCreate(name, config)
                    val second = factory.getOrCreate(name, config)

                    (first === second) shouldBe true
                    delegate.creationCount.get() shouldBe 1
                }
            }

            "different names create different instances" {
                checkAll(keyArb, keyArb) { name1, name2 ->
                    if (name1 == name2) return@checkAll

                    val (factory) = createFactory()
                    val config = TestConfig(version = 1)

                    val a = factory.getOrCreate(name1, config)
                    val b = factory.getOrCreate(name2, config)

                    (a !== b) shouldBe true
                    a.name shouldBe name1
                    b.name shouldBe name2
                }
            }

            "different config replaces existing instance (config drift)" {
                checkAll(keyArb, Arb.int(1..1_000), Arb.int(1_001..2_000)) { name, v1, v2 ->
                    val (factory, delegate) = createFactory()

                    val first = factory.getOrCreate(name, TestConfig(version = v1))
                    val second = factory.getOrCreate(name, TestConfig(version = v2))

                    (first !== second) shouldBe true
                    first.version shouldBe v1
                    second.version shouldBe v2
                    delegate.creationCount.get() shouldBe 2
                }
            }

            "matchesConfig controls whether instance is reused" {
                checkAll(keyArb, Arb.int(), Arb.int()) { key, v1, v2 ->
                    val alwaysMatch = createFactory(matchesConfig = { _, _ -> true })
                    val neverMatch = createFactory(matchesConfig = { _, _ -> false })

                    val (alwaysFactory, alwaysDelegate) = alwaysMatch
                    alwaysFactory.getOrCreate(key, TestConfig(v1))
                    alwaysFactory.getOrCreate(key, TestConfig(v2))
                    alwaysDelegate.creationCount.get() shouldBe 1

                    val (neverFactory, neverDelegate) = neverMatch
                    neverFactory.getOrCreate(key, TestConfig(v1))
                    neverFactory.getOrCreate(key, TestConfig(v1))
                    neverDelegate.creationCount.get() shouldBe 2
                }
            }

            "evict removes instance from cache" {
                checkAll(keyArb, Arb.int()) { name, version ->
                    val (factory, delegate) = createFactory()
                    val config = TestConfig(version = version)

                    factory.getOrCreate(name, config)
                    delegate.creationCount.get() shouldBe 1

                    factory.evict(name)

                    factory.getOrCreate(name, config)
                    delegate.creationCount.get() shouldBe 2
                }
            }

            "estimatedSize delegates to cache" {
                checkAll(keyArb, keyArb, Arb.int(), Arb.int()) { name1, name2, v1, v2 ->
                    if (name1 == name2) return@checkAll

                    val (factory, _, cache) = createFactory()

                    factory.estimatedSize() shouldBe 0

                    factory.getOrCreate(name1, TestConfig(v1))
                    factory.getOrCreate(name2, TestConfig(v2))

                    factory.estimatedSize() shouldBe cache.estimatedSize()
                    factory.estimatedSize() shouldBe 2
                }
            }

            "concurrent getOrCreate with same key delegates exactly once" {
                val delegate = FakeFactory(creationDelay = 50)
                val (factory) = createFactory(delegate = delegate)
                val config = TestConfig(version = 1)
                val iterations = 100

                val results =
                    withContext(Dispatchers.Default) {
                        (1..iterations)
                            .map {
                                async {
                                    factory.getOrCreate("shared", config)
                                }
                            }.awaitAll()
                    }

                results.forEach { it shouldBe TestInstance("shared", 1) }
                delegate.creationCount.get() shouldBe 1
                results.map { System.identityHashCode(it) }.distinct().size shouldBe 1
            }

            "concurrent getOrCreate with different keys creates all instances" {
                val delegate = FakeFactory()
                val (factory) = createFactory(delegate = delegate)
                val config = TestConfig(version = 1)
                val iterations = 100

                val results =
                    withContext(Dispatchers.Default) {
                        (1..iterations)
                            .map { i ->
                                async {
                                    factory.getOrCreate("key-$i", config)
                                }
                            }.awaitAll()
                    }

                results.size shouldBe iterations
                delegate.creationCount.get() shouldBe iterations
                factory.estimatedSize() shouldBe iterations.toLong()
            }

            "concurrent evict and getOrCreate do not corrupt state" {
                val delegate = FakeFactory()
                val (factory) = createFactory(delegate = delegate)
                val config = TestConfig(version = 1)
                val iterations = 100

                (1..iterations).forEach { i ->
                    factory.getOrCreate("key-$i", config)
                }

                withContext(Dispatchers.Default) {
                    val evictJobs =
                        (1..iterations).map { i ->
                            async { factory.evict("key-$i") }
                        }
                    val createJobs =
                        (1..iterations).map { i ->
                            async { factory.getOrCreate("key-$i", config) }
                        }
                    (evictJobs + createJobs).awaitAll()
                }

                val size = factory.estimatedSize()
                size shouldBe iterations.toLong()
            }

            "eviction listener cleans up creation locks" {
                checkAll(keyArb, Arb.int()) { name, version ->
                    val cache = FakeCache()
                    val (factory) = createFactory(cache = cache)
                    val config = TestConfig(version = version)

                    factory.getOrCreate(name, config)
                    factory.evict(name)

                    val recreated = factory.getOrCreate(name, config)
                    recreated.version shouldBe version
                    factory.estimatedSize() shouldBe 1
                }
            }
        },
    )
