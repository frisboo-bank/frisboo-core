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
package com.frisboo.corebanking.resilience.circuitbreaker

import com.frisboo.corebanking.registry.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerConfigSource
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.testutils.createCircuitBreakerConfigArb
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

internal class CircuitBreakerFactoryImplTest : StringSpec(
    {

        fun testRegistry(): Registry<String, String> = InMemoryRegistryImpl(
            scope = RegistryScope(name = "cb-test", team = "resilience"),
        )

        val serviceNameArb = Arb.string(minSize = 1, maxSize = 50)

        "same name and config returns the same instance" {
            runTest {
                val scope = this
                checkAll(
                    serviceNameArb,
                    createCircuitBreakerConfigArb(),
                ) { name: String, config: CircuitBreakerConfig ->
                    val factory = CircuitBreakerFactoryImpl(testRegistry(), coroutineScope = scope)

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
                    createCircuitBreakerConfigArb(),
                    createCircuitBreakerConfigArb(),
                ) { name: String, configA: CircuitBreakerConfig, configB: CircuitBreakerConfig ->
                    val factory = CircuitBreakerFactoryImpl(testRegistry(), coroutineScope = scope)

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
                    createCircuitBreakerConfigArb(),
                ) { name1, name2, config ->
                    if (name1 == name2) return@checkAll

                    val factory = CircuitBreakerFactoryImpl(testRegistry(), coroutineScope = scope)

                    val a = factory.create(name1, config)
                    val b = factory.create(name2, config)

                    (a !== b) shouldBe true
                }
            }
        }

        "create by name resolves config from source" {
            runTest {
                val scope = this
                checkAll(serviceNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val source = CircuitBreakerConfigSource { n ->
                        if (n == name) config else null
                    }

                    val factory = CircuitBreakerFactoryImpl(
                        stateRegistry = testRegistry(),
                        coroutineScope = scope,
                        configSource = source,
                    )

                    val breaker = factory.create(name)
                    breaker.config.failureRateThreshold shouldBe config.failureRateThreshold
                }
            }
        }

        "create by name throws when no config source" {
            runTest {
                val scope = this
                checkAll(serviceNameArb) { name ->
                    val factory = CircuitBreakerFactoryImpl(testRegistry(), coroutineScope = scope)
                    val exception = shouldThrow<IllegalStateException> {
                        factory.create(name)
                    }
                    exception.message shouldContain "No CircuitBreakerConfigSource"
                }
            }
        }

        "create by name throws when source returns null" {
            runTest {
                val scope = this
                checkAll(serviceNameArb) { name ->
                    val source = CircuitBreakerConfigSource { null }
                    val factory = CircuitBreakerFactoryImpl(
                        stateRegistry = testRegistry(),
                        coroutineScope = scope,
                        configSource = source,
                    )
                    val exception = shouldThrow<IllegalStateException> {
                        factory.create(name)
                    }
                    exception.message shouldContain "No circuit breaker configuration found"
                }
            }
        }

        "concurrent access does not crash and all breakers are functional" {
            runTest {
                val scope = this
                checkAll(serviceNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val factory = CircuitBreakerFactoryImpl(
                        testRegistry(),
                        coroutineScope = scope,
                    )
                    val results = withContext(Dispatchers.Default) {
                        (1..50).map {
                            async {
                                factory.create(name, config)
                            }
                        }.awaitAll()
                    }

                    results.size shouldBe 50
                    results.forEach { breaker ->
                        breaker.metrics.state shouldBe CircuitBreakerState.CLOSED
                    }
                }
            }
        }
    },
)
