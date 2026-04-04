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
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class CircuitBreakerFactoryImplTest : StringSpec({

    fun testRegistry(): Registry<String, String> = InMemoryRegistryImpl(
        scope = RegistryScope(name = "cb-test", team = "resilience"),
    )

    fun testConfig(failureRateThreshold: Float = 50f) = CircuitBreakerConfig(
        failureRateThreshold = failureRateThreshold,
        slowCallDurationThreshold = 2.seconds,
        slowCallRateThreshold = 100f,
        waitDurationInOpenState = 60.seconds,
        slidingWindowSize = 100,
        minimumNumberOfCalls = 10,
        permittedNumberOfCallsInHalfOpenState = 10,
        maxWaitDurationInHalfOpenState = Duration.ZERO,
    )

    "same name and config returns the same instance" {
        runBlocking {
            val factory = CircuitBreakerFactoryImpl(testRegistry(), this)
            val config = testConfig()
            val first = factory.create("svc", config)
            val second = factory.create("svc", config)
            (first === second) shouldBe true
        }
    }

    "different config warns and replaces without crash" {
        runBlocking {
            val factory = CircuitBreakerFactoryImpl(testRegistry(), this)
            val configA = testConfig(failureRateThreshold = 50f)
            val configB = testConfig(failureRateThreshold = 75f)
            val first = factory.create("x", configA)
            val second = factory.create("x", configB)
            (first === second) shouldBe false
        }
    }

    "different names create different instances" {
        runBlocking {
            val factory = CircuitBreakerFactoryImpl(testRegistry(), this)
            val config = testConfig()
            val a = factory.create("a", config)
            val b = factory.create("b", config)
            (a !== b) shouldBe true
        }
    }

    "create by name resolves config from source" {
        runBlocking {
            val config = testConfig()
            val source = CircuitBreakerConfigSource { name ->
                if (name == "svc") config else null
            }
            val factory = CircuitBreakerFactoryImpl(
                stateRegistry = testRegistry(),
                coroutineScope = this,
                configSource = source,
            )
            val breaker = factory.create("svc")
            breaker.metrics.state shouldBe CircuitBreakerState.CLOSED
        }
    }

    "create by name throws when no config source" {
        runBlocking {
            val factory = CircuitBreakerFactoryImpl(testRegistry(), this)
            val exception = shouldThrow<IllegalStateException> {
                factory.create("test")
            }
            exception.message shouldContain "No CircuitBreakerConfigSource"
        }
    }

    "create by name throws when source returns null" {
        runBlocking {
            val source = CircuitBreakerConfigSource { null }
            val factory = CircuitBreakerFactoryImpl(
                stateRegistry = testRegistry(),
                coroutineScope = this,
                configSource = source,
            )
            val exception = shouldThrow<IllegalStateException> {
                factory.create("unknown")
            }
            exception.message shouldContain "No circuit breaker configuration found"
        }
    }

    "concurrent access does not crash" {
        runBlocking {
            val factory = CircuitBreakerFactoryImpl(testRegistry(), this)
            val config = testConfig()
            val results = (1..50).map {
                async(Dispatchers.Default) {
                    factory.create("shared", config)
                }
            }.awaitAll()
            results.size shouldBe 50
            results.forEach { breaker ->
                breaker.metrics.state shouldBe CircuitBreakerState.CLOSED
            }
        }
    }
})
