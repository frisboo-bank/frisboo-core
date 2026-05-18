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
package com.frisboo.corebanking.resilience.circuitbreaker.adapters.resilience4j

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerResult
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerPersistenceContext
import com.frisboo.corebanking.resilience.circuitbreaker.testutils.createCircuitBreakerConfigArb
import com.frisboo.corebanking.statemanager.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.statemanager.contracts.StateManager
import com.frisboo.corebanking.statemanager.models.RegistryScope
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig as R4jConfig

internal class Resilience4jCircuitBreakerTest :
    StringSpec(
        {

            fun testRegistry(): StateManager<String, String> =
                InMemoryRegistryImpl(
                    scope = RegistryScope(name = "r4j-test", team = "resilience"),
                )

            val breakerNameArb = Arb.string(minSize = 1, maxSize = 50)

            suspend fun createBreaker(
                name: String,
                config: CircuitBreakerConfig,
                stateManager: StateManager<String, String> = testRegistry(),
                scope: CoroutineScope,
            ): Resilience4jCircuitBreaker =
                createResilience4jBreaker(
                    name = name,
                    config = config,
                    persistence = CircuitBreakerPersistenceContext(stateManager, scope),
                )

            "successful call returns Released with value" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        val result = cb.executeSuspend { "hello" }
                        result.shouldBeInstanceOf<CircuitBreakerResult.Success<String>>()
                        result.value shouldBe "hello"
                    }
                }
            }

            "failed call returns Failure with original exception" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        val result = cb.executeSuspend { throw RuntimeException("boom") }
                        result.shouldBeInstanceOf<CircuitBreakerResult.Failure>()
                        result.cause.shouldBeInstanceOf<RuntimeException>()
                        result.cause.message shouldBe "boom"
                    }
                }
            }

            "open circuit returns Rejected with CallNotPermitted error" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        cb.trip()
                        val result = cb.executeSuspend { "never" }
                        result.shouldBeInstanceOf<CircuitBreakerResult.Rejected>()
                        result.error.message shouldContain name
                    }
                }
            }

            "CancellationException propagates without recording" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        shouldThrow<CancellationException> {
                            cb.executeSuspend { throw CancellationException("cancelled") }
                        }
                        cb.metrics.numberOfFailedCalls shouldBe 0
                        cb.metrics.numberOfSuccessfulCalls shouldBe 0
                    }
                }
            }

            "metrics reflect call outcomes" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        cb.executeSuspend { "ok" }
                        cb.executeSuspend { throw RuntimeException("fail") }
                        cb.metrics.numberOfSuccessfulCalls shouldBe 1
                        cb.metrics.numberOfFailedCalls shouldBe 1
                        cb.metrics.numberOfBufferedCalls shouldBe 2
                    }
                }
            }

            "trip and reset transition states correctly" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        cb.metrics.state shouldBe CircuitBreakerState.CLOSED
                        cb.trip()
                        cb.metrics.state shouldBe CircuitBreakerState.OPEN
                        cb.reset()
                        cb.metrics.state shouldBe CircuitBreakerState.CLOSED
                    }
                }
            }

            "config returns the domain config supplied at creation" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        cb.config shouldBe config
                    }
                }
            }

            "internalConfig returns the Resilience4j native config" {
                runTest {
                    val testScope = this
                    checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                        val cb = createBreaker(name, config, scope = testScope)
                        val internal = cb.internalConfig
                        internal.shouldBeInstanceOf<R4jConfig>()
                        internal.failureRateThreshold shouldBe config.failureRateThreshold
                        internal.slidingWindowSize shouldBe config.slidingWindowSize
                        internal.minimumNumberOfCalls shouldBe config.minimumNumberOfCalls
                    }
                }
            }
        },
    )
