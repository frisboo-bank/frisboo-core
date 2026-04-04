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

import com.frisboo.corebanking.registry.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CallNotPermittedException
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerPersistenceContext
import com.frisboo.corebanking.resilience.circuitbreaker.testutils.createCircuitBreakerConfigArb
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig as R4jConfig

internal class Resilience4jCircuitBreakerTest : StringSpec(
    {

        fun testRegistry(): Registry<String, String> = InMemoryRegistryImpl(
            scope = RegistryScope(name = "r4j-test", team = "resilience"),
        )

        val breakerNameArb = Arb.string(minSize = 1, maxSize = 50)

        suspend fun createBreaker(
            name: String,
            config: CircuitBreakerConfig,
            registry: Registry<String, String> = testRegistry(),
            scope: CoroutineScope,
        ): Resilience4jCircuitBreaker = createResilience4jBreaker(
            name = name,
            config = config,
            persistence = CircuitBreakerPersistenceContext(registry, scope),
        )

        "successful call returns value" {
            runTest {
                val testScope = this
                checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val cb = createBreaker(name, config, scope = testScope)
                    val result = cb.executeSuspend { "hello" }
                    result shouldBe "hello"
                }
            }
        }

        "failed call throws the original exception" {
            runTest {
                val testScope = this
                checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val cb = createBreaker(name, config, scope = testScope)
                    shouldThrow<RuntimeException> {
                        cb.executeSuspend { throw RuntimeException("boom") }
                    }.message shouldBe "boom"
                }
            }
        }

        "fallback receives the original exception" {
            runTest {
                val testScope = this
                checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val cb = createBreaker(name, config, scope = testScope)
                    val result = cb.executeSuspend(
                        block = { throw RuntimeException("boom") },
                        fallback = { e -> "recovered: ${e.message}" },
                    )
                    result shouldBe "recovered: boom"
                }
            }
        }

        "open circuit throws CallNotPermittedException" {
            runTest {
                val testScope = this
                checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val cb = createBreaker(name, config, scope = testScope)
                    cb.trip()
                    shouldThrow<CallNotPermittedException> {
                        cb.executeSuspend { "never" }
                    }.circuitBreakerName shouldBe name
                }
            }
        }

        "open circuit with fallback invokes fallback" {
            runTest {
                val testScope = this
                checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val cb = createBreaker(name, config, scope = testScope)
                    cb.trip()
                    val result = cb.executeSuspend(
                        block = { "never" },
                        fallback = { e -> "fallback: ${e.javaClass.simpleName}" },
                    )
                    result shouldBe "fallback: CallNotPermittedException"
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
                    runCatching { cb.executeSuspend { throw RuntimeException("fail") } }
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

        "getInternalConfig returns the Resilience4j native config" {
            runTest {
                val testScope = this
                checkAll(breakerNameArb, createCircuitBreakerConfigArb()) { name, config ->
                    val cb = createBreaker(name, config, scope = testScope)
                    val internal = cb.getInternalConfig()
                    internal.shouldBeInstanceOf<R4jConfig>()
                    internal.failureRateThreshold shouldBe config.failureRateThreshold
                    internal.slidingWindowSize shouldBe config.slidingWindowSize
                    internal.minimumNumberOfCalls shouldBe config.minimumNumberOfCalls
                }
            }
        }
    },
)
