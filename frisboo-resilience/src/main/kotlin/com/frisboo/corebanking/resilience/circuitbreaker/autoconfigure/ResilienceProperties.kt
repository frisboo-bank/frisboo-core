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

import com.frisboo.corebanking.resilience.circuitbreaker.model.SlidingWindowType
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frisboo.corebanking.resilience")
public data class ResilienceProperties(
    val enabled: Boolean = false,
    val circuitBreaker: CircuitBreakerProperties = CircuitBreakerProperties(),
) {

    public data class CircuitBreakerProperties(
        val enabled: Boolean = false,
        val maxBreakers: Long = 10_000,
        val failureRateThreshold: Float? = null,
        val slowCallDurationThreshold: Duration? = null,
        val slowCallRateThreshold: Float? = null,
        val waitDurationInOpenState: Duration? = null,
        val slidingWindowSize: Int? = null,
        val slidingWindowType: SlidingWindowType? = null,
        val minimumNumberOfCalls: Int? = null,
        val permittedNumberOfCallsInHalfOpenState: Int? = null,
        val maxWaitDurationInHalfOpenState: Duration? = null,
        val writableStackTraceEnabled: Boolean? = null,
        val instances: Map<String, CircuitBreakerInstanceProperties> = emptyMap(),
    )

    public data class CircuitBreakerInstanceProperties(
        val failureRateThreshold: Float? = null,
        val slowCallDurationThreshold: Duration? = null,
        val slowCallRateThreshold: Float? = null,
        val waitDurationInOpenState: Duration? = null,
        val slidingWindowSize: Int? = null,
        val slidingWindowType: SlidingWindowType? = null,
        val minimumNumberOfCalls: Int? = null,
        val permittedNumberOfCallsInHalfOpenState: Int? = null,
        val maxWaitDurationInHalfOpenState: Duration? = null,
        val writableStackTraceEnabled: Boolean? = null,
    )
}
