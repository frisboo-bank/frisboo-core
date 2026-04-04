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
package com.frisboo.corebanking.resilience.circuitbreaker.contracts

/**
 * Circuit breaker state machine states.
 *
 * State transitions:
 * - CLOSED -> OPEN: when failure rate exceeds threshold
 * - OPEN -> HALF_OPEN: after wait duration elapses
 * - HALF_OPEN -> CLOSED: if trial call succeeds
 * - HALF_OPEN -> OPEN: if trial call fails
 * - Any state -> DISABLED: when circuit breaker is disabled
 * - Any state -> FORCED_OPEN: when circuit breaker is forced open
 * - Any state -> METRICS_ONLY: when circuit breaker is in metrics-only mode
 */
public enum class CircuitBreakerState(
    public val stateName: String,
) {
    CLOSED("closed"),
    OPEN("open"),
    HALF_OPEN("half-open"),
    DISABLED("disabled"),
    FORCED_OPEN("forced-open"),
    METRICS_ONLY("metrics-only"),
    ;

    public companion object {
        /** Returns the state matching [name], or `null` if no match. */
        public fun fromStateNameOrNull(name: String): CircuitBreakerState? =
            entries.firstOrNull { it.stateName == name }
    }
}
