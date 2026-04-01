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

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerMetrics
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import io.github.resilience4j.circuitbreaker.CircuitBreaker as R4jCircuitBreaker

internal fun CircuitBreakerMetrics.Companion.from(breaker: R4jCircuitBreaker): CircuitBreakerMetrics {
    val metrics = breaker.metrics

    return object : CircuitBreakerMetrics {
        override val state: CircuitBreakerState get() = CircuitBreakerState.from(breaker.state)
        override val failureRate: Float get() = metrics.failureRate
        override val slowCallRate: Float get() = metrics.slowCallRate
        override val numberOfBufferedCalls: Int get() = metrics.numberOfBufferedCalls
        override val numberOfFailedCalls: Int get() = metrics.numberOfFailedCalls
        override val numberOfSuccessfulCalls: Int get() = metrics.numberOfSuccessfulCalls
        override val numberOfSlowCalls: Int get() = metrics.numberOfSlowCalls
        override val numberOfSlowSuccessfulCalls: Int get() = metrics.numberOfSlowSuccessfulCalls
        override val numberOfSlowFailedCalls: Int get() = metrics.numberOfSlowFailedCalls
        override val numberOfNotPermittedCalls: Long get() = metrics.numberOfNotPermittedCalls
    }
}
