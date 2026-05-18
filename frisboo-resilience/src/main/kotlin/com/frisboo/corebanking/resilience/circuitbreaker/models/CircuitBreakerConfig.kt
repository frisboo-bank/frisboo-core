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
package com.frisboo.corebanking.resilience.circuitbreaker.models

import kotlin.time.Duration

/**
 * Configuration for a circuit breaker instance.
 *
 * All values are validated at construction time to ensure correctness.
 */
public data class CircuitBreakerConfig(
    val failureRateThreshold: Float,
    val maxWaitDurationInHalfOpenState: Duration,
    val minimumNumberOfCalls: Int,
    val permittedNumberOfCallsInHalfOpenState: Int,
    val slidingWindowSize: Int,
    val slidingWindowType: SlidingWindowType = SlidingWindowType.COUNT_BASED,
    val slowCallDurationThreshold: Duration,
    val slowCallRateThreshold: Float,
    val waitDurationInOpenState: Duration,
    val recordExceptions: Set<Class<out Throwable>> = emptySet(),
    val ignoreExceptions: Set<Class<out Throwable>> = emptySet(),
    val writableStackTraceEnabled: Boolean = true,
) {
    public companion object {
        /** Minimum 1 % — a 0 % threshold would open the circuit on any single failure. */
        public const val MIN_FAILURE_RATE: Float = 1f
        public const val MAX_FAILURE_RATE: Float = 100f
        public const val MIN_SLOW_CALL_RATE: Float = 1f
        public const val MAX_SLOW_CALL_RATE: Float = 100f
        public const val MIN_SLIDING_WINDOW_SIZE: Int = 10
        public const val MIN_MINIMUM_CALLS: Int = 1

        private fun requireInRange(
            value: Float,
            min: Float,
            max: Float,
            name: String,
        ) {
            require(value in min..max) { "$name must be between $min and $max, got $value" }
        }
    }

    init {
        validate()
    }

    private fun validate() {
        requireInRange(failureRateThreshold, MIN_FAILURE_RATE, MAX_FAILURE_RATE, "failureRateThreshold")
        requireInRange(slowCallRateThreshold, MIN_SLOW_CALL_RATE, MAX_SLOW_CALL_RATE, "slowCallRateThreshold")
        require(slowCallDurationThreshold.isPositive()) {
            "slowCallDurationThreshold must be positive"
        }
        require(waitDurationInOpenState.isPositive()) {
            "waitDurationInOpenState must be positive"
        }
        require(slidingWindowSize >= MIN_SLIDING_WINDOW_SIZE) {
            "slidingWindowSize must be >= $MIN_SLIDING_WINDOW_SIZE, got $slidingWindowSize"
        }
        require(minimumNumberOfCalls in MIN_MINIMUM_CALLS..slidingWindowSize) {
            "minimumNumberOfCalls must be between $MIN_MINIMUM_CALLS " +
                "and slidingWindowSize ($slidingWindowSize), got $minimumNumberOfCalls"
        }
        require(permittedNumberOfCallsInHalfOpenState > 0) {
            "permittedNumberOfCallsInHalfOpenState must be positive, got $permittedNumberOfCallsInHalfOpenState"
        }
        require(!maxWaitDurationInHalfOpenState.isNegative()) {
            "maxWaitDurationInHalfOpenState must not be negative"
        }
        require(recordExceptions.none { it in ignoreExceptions }) {
            "recordExceptions and ignoreExceptions must not overlap"
        }
    }
}
