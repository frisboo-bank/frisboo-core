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
import com.frisboo.corebanking.resilience.circuitbreaker.CircuitBreakerFactoryImpl
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CallNotPermittedException
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class Resilience4jCircuitBreakerTest : StringSpec({

    fun testRegistry(): Registry<String, String> = InMemoryRegistryImpl(
        scope = RegistryScope(name = "r4j-test", team = "resilience"),
    )

    fun testConfig(
        failureRateThreshold: Float = 50f,
        minimumNumberOfCalls: Int = 10,
        slidingWindowSize: Int = 10,
        waitDurationInOpenState: Duration = 60.seconds,
        permittedNumberOfCallsInHalfOpenState: Int = 10,
    ) = CircuitBreakerConfig(
        failureRateThreshold = failureRateThreshold,
        slowCallDurationThreshold = 2.seconds,
        slowCallRateThreshold = 100f,
        waitDurationInOpenState = waitDurationInOpenState,
        slidingWindowSize = slidingWindowSize,
        minimumNumberOfCalls = minimumNumberOfCalls,
        permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState,
        maxWaitDurationInHalfOpenState = Duration.ZERO,
    )

    suspend fun CoroutineScope.createBreaker(
        name: String = "test-breaker",
        config: CircuitBreakerConfig = testConfig(),
    ): CircuitBreaker {
        val factory = CircuitBreakerFactoryImpl(testRegistry(), this)
        return factory.create(name, config)
    }

    "successful call returns value" {
        runBlocking {
            val cb = createBreaker()
            val result = cb.executeSuspend { "hello" }
            result shouldBe "hello"
        }
    }

    "failed call throws the original exception" {
        runBlocking {
            val cb = createBreaker()
            shouldThrow<RuntimeException> {
                cb.executeSuspend { throw RuntimeException("boom") }
            }.message shouldBe "boom"
        }
    }

    "fallback receives the original exception" {
        runBlocking {
            val cb = createBreaker()
            val result = cb.executeSuspend(
                block = { throw RuntimeException("boom") },
                fallback = { e -> "recovered: ${e.message}" },
            )
            result shouldBe "recovered: boom"
        }
    }

    "open circuit throws CallNotPermittedException" {
        runBlocking {
            val cb = createBreaker()
            cb.trip()
            shouldThrow<CallNotPermittedException> {
                cb.executeSuspend { "never" }
            }.circuitBreakerName shouldBe "test-breaker"
        }
    }

    "open circuit with fallback invokes fallback" {
        runBlocking {
            val cb = createBreaker()
            cb.trip()
            val result = cb.executeSuspend(
                block = { "never" },
                fallback = { e -> "fallback: ${e.javaClass.simpleName}" },
            )
            result shouldBe "fallback: CallNotPermittedException"
        }
    }

    "CancellationException propagates without recording" {
        runBlocking {
            val cb = createBreaker()
            shouldThrow<CancellationException> {
                cb.executeSuspend { throw CancellationException("cancelled") }
            }
            cb.metrics.numberOfFailedCalls shouldBe 0
            cb.metrics.numberOfSuccessfulCalls shouldBe 0
        }
    }

    "metrics reflect call outcomes" {
        runBlocking {
            val cb = createBreaker(config = testConfig(slidingWindowSize = 10, minimumNumberOfCalls = 1))
            cb.executeSuspend { "ok" }
            runCatching { cb.executeSuspend { throw RuntimeException("fail") } }
            cb.metrics.numberOfSuccessfulCalls shouldBe 1
            cb.metrics.numberOfFailedCalls shouldBe 1
            cb.metrics.numberOfBufferedCalls shouldBe 2
        }
    }

    "trip and reset transition states correctly" {
        runBlocking {
            val cb = createBreaker()
            cb.metrics.state shouldBe CircuitBreakerState.CLOSED
            cb.trip()
            cb.metrics.state shouldBe CircuitBreakerState.OPEN
            cb.reset()
            cb.metrics.state shouldBe CircuitBreakerState.CLOSED
        }
    }
})
