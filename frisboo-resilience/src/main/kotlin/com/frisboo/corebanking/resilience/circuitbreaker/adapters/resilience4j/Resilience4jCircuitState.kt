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

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State.DISABLED
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State.FORCED_OPEN
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State.HALF_OPEN
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State.METRICS_ONLY
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN
import io.github.resilience4j.circuitbreaker.CircuitBreaker as R4jCircuitBreaker

internal fun CircuitBreakerState.Companion.from(state: R4jCircuitBreaker.State): CircuitBreakerState =
    when (state) {
        CLOSED -> CircuitBreakerState.CLOSED
        OPEN -> CircuitBreakerState.OPEN
        HALF_OPEN -> CircuitBreakerState.HALF_OPEN
        DISABLED -> CircuitBreakerState.DISABLED
        FORCED_OPEN -> CircuitBreakerState.FORCED_OPEN
        METRICS_ONLY -> CircuitBreakerState.METRICS_ONLY
    }

internal fun R4jCircuitBreaker.transitionTo(state: CircuitBreakerState) {
    when (state) {
        CircuitBreakerState.CLOSED -> transitionToClosedState()
        CircuitBreakerState.OPEN -> transitionToOpenState()
        CircuitBreakerState.HALF_OPEN -> transitionToHalfOpenState()
        CircuitBreakerState.DISABLED -> transitionToDisabledState()
        CircuitBreakerState.FORCED_OPEN -> transitionToForcedOpenState()
        CircuitBreakerState.METRICS_ONLY -> transitionToMetricsOnlyState()
    }
}
