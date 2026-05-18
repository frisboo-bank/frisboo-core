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
package com.frisboo.corebanking.resilience.ratelimiter

import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterConfigSource
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterConfig
import com.frisboo.corebanking.resilience.ratelimiter.testutils.createRateLimiterConfigArb
import com.frisboo.corebanking.statemanager.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.statemanager.contracts.StateManager
import com.frisboo.corebanking.statemanager.models.RegistryScope
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

internal class RateLimiterFactoryImplTest :
    StringSpec(
        {

            fun testRegistry(): StateManager<String, String> =
                InMemoryRegistryImpl(
                    scope = RegistryScope(name = "rl-test", team = "resilience"),
                )

            val serviceNameArb = Arb.string(minSize = 1, maxSize = 50)

            "same name and config returns the same instance" {
                runTest {
                    val scope = this
                    checkAll(
                        serviceNameArb,
                        createRateLimiterConfigArb(),
                    ) { name: String, config: RateLimiterConfig ->
                        val factory = RateLimiterFactoryImpl(testRegistry(), coroutineScope = scope)

                        val first = factory.create(name, config)
                        val second = factory.create(name, config)

                        (first === second) shouldBe true
                    }
                }
            }

            "different config warns and replaces with new instance" {
                runTest {
                    val scope = this
                    checkAll(
                        serviceNameArb,
                        createRateLimiterConfigArb(),
                        createRateLimiterConfigArb(),
                    ) { name: String, configA: RateLimiterConfig, configB: RateLimiterConfig ->
                        val factory = RateLimiterFactoryImpl(testRegistry(), coroutineScope = scope)

                        val first = factory.create(name, configA)
                        val second = factory.create(name, configB)

                        (first === second) shouldBe false
                    }
                }
            }

            "different names create different instances" {
                runTest {
                    val scope = this
                    checkAll(
                        serviceNameArb,
                        serviceNameArb,
                        createRateLimiterConfigArb(),
                    ) { name1, name2, config ->
                        if (name1 == name2) return@checkAll

                        val factory = RateLimiterFactoryImpl(testRegistry(), coroutineScope = scope)

                        val a = factory.create(name1, config)
                        val b = factory.create(name2, config)

                        (a !== b) shouldBe true
                    }
                }
            }

            "create by name resolves config from source" {
                runTest {
                    val scope = this
                    checkAll(serviceNameArb, createRateLimiterConfigArb()) { name, config ->
                        val source =
                            RateLimiterConfigSource { n ->
                                if (n == name) config else null
                            }

                        val factory =
                            RateLimiterFactoryImpl(
                                stateStateManager = testRegistry(),
                                coroutineScope = scope,
                                configSource = source,
                            )

                        val limiter = factory.create(name)
                        limiter.config.limitForPeriod shouldBe config.limitForPeriod
                    }
                }
            }

            "create by name throws when no config source" {
                runTest {
                    val scope = this
                    checkAll(serviceNameArb) { name ->
                        val factory = RateLimiterFactoryImpl(testRegistry(), coroutineScope = scope)
                        val exception =
                            shouldThrow<IllegalStateException> {
                                factory.create(name)
                            }
                        exception.message shouldContain "No RateLimiterConfigSource"
                    }
                }
            }

            "create by name throws when source returns null" {
                runTest {
                    val scope = this
                    checkAll(serviceNameArb) { name ->
                        val source = RateLimiterConfigSource { null }
                        val factory =
                            RateLimiterFactoryImpl(
                                stateStateManager = testRegistry(),
                                coroutineScope = scope,
                                configSource = source,
                            )
                        val exception =
                            shouldThrow<IllegalStateException> {
                                factory.create(name)
                            }
                        exception.message shouldContain "No rate limiter configuration found"
                    }
                }
            }

            "concurrent access does not crash and all limiters are functional" {
                runTest {
                    val scope = this
                    checkAll(serviceNameArb, createRateLimiterConfigArb()) { name, config ->
                        val factory =
                            RateLimiterFactoryImpl(
                                testRegistry(),
                                coroutineScope = scope,
                            )
                        val results =
                            withContext(Dispatchers.Default) {
                                (1..50)
                                    .map {
                                        async {
                                            factory.create(name, config)
                                        }
                                    }.awaitAll()
                            }

                        results.size shouldBe 50
                        results.forEach { limiter ->
                            limiter.metrics.availablePermits shouldBe config.limitForPeriod.toLong()
                        }
                    }
                }
            }
        },
    )
