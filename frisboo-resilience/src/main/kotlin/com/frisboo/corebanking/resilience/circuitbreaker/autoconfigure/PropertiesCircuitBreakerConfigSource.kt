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
package com.frisboo.corebanking.resilience.circuitbreaker.autoconfigure

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerConfigSource
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.model.SlidingWindowType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

internal class PropertiesCircuitBreakerConfigSource(
    private val properties: ResilienceProperties,
) : CircuitBreakerConfigSource {

    override fun resolve(name: String): CircuitBreakerConfig? {
        val global = properties.circuitBreaker
        val instance = global.instances[name]

        if (instance == null && !hasAnyGlobalDefault(global)) return null

        return CircuitBreakerConfig(
            failureRateThreshold = instance?.failureRateThreshold
                ?: global.failureRateThreshold
                ?: DEFAULT_FAILURE_RATE_THRESHOLD,
            slowCallDurationThreshold = (instance?.slowCallDurationThreshold ?: global.slowCallDurationThreshold)
                ?.toKotlinDuration()
                ?: DEFAULT_SLOW_CALL_DURATION_THRESHOLD,
            slowCallRateThreshold = instance?.slowCallRateThreshold
                ?: global.slowCallRateThreshold
                ?: DEFAULT_SLOW_CALL_RATE_THRESHOLD,
            waitDurationInOpenState = (instance?.waitDurationInOpenState ?: global.waitDurationInOpenState)
                ?.toKotlinDuration()
                ?: DEFAULT_WAIT_DURATION_IN_OPEN_STATE,
            slidingWindowSize = instance?.slidingWindowSize
                ?: global.slidingWindowSize
                ?: DEFAULT_SLIDING_WINDOW_SIZE,
            slidingWindowType = instance?.slidingWindowType
                ?: global.slidingWindowType
                ?: DEFAULT_SLIDING_WINDOW_TYPE,
            minimumNumberOfCalls = instance?.minimumNumberOfCalls
                ?: global.minimumNumberOfCalls
                ?: DEFAULT_MINIMUM_NUMBER_OF_CALLS,
            permittedNumberOfCallsInHalfOpenState = instance?.permittedNumberOfCallsInHalfOpenState
                ?: global.permittedNumberOfCallsInHalfOpenState
                ?: DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN,
            maxWaitDurationInHalfOpenState = (instance?.maxWaitDurationInHalfOpenState
                ?: global.maxWaitDurationInHalfOpenState)
                ?.toKotlinDuration()
                ?: DEFAULT_MAX_WAIT_DURATION_IN_HALF_OPEN,
            writableStackTraceEnabled = instance?.writableStackTraceEnabled
                ?: global.writableStackTraceEnabled
                ?: DEFAULT_WRITABLE_STACK_TRACE_ENABLED,
        )
    }

    private companion object {
        private const val DEFAULT_FAILURE_RATE_THRESHOLD = 50f
        private const val DEFAULT_SLOW_CALL_RATE_THRESHOLD = 100f
        private val DEFAULT_SLOW_CALL_DURATION_THRESHOLD = 2.seconds
        private val DEFAULT_WAIT_DURATION_IN_OPEN_STATE = 60.seconds
        private const val DEFAULT_SLIDING_WINDOW_SIZE = 100
        private val DEFAULT_SLIDING_WINDOW_TYPE = SlidingWindowType.COUNT_BASED
        private const val DEFAULT_MINIMUM_NUMBER_OF_CALLS = 10
        private const val DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN = 10
        private val DEFAULT_MAX_WAIT_DURATION_IN_HALF_OPEN = Duration.ZERO
        private const val DEFAULT_WRITABLE_STACK_TRACE_ENABLED = true

        private fun hasAnyGlobalDefault(global: ResilienceProperties.CircuitBreakerProperties): Boolean =
            global.failureRateThreshold != null ||
                global.slowCallDurationThreshold != null ||
                global.slowCallRateThreshold != null ||
                global.waitDurationInOpenState != null ||
                global.slidingWindowSize != null ||
                global.slidingWindowType != null ||
                global.minimumNumberOfCalls != null ||
                global.permittedNumberOfCallsInHalfOpenState != null ||
                global.maxWaitDurationInHalfOpenState != null ||
                global.writableStackTraceEnabled != null
    }
}
