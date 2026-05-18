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
package com.frisboo.corebanking.resilience.autoconfigure

import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.models.SlidingWindowType
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import kotlin.time.toKotlinDuration

@ConfigurationProperties(prefix = "frisboo.corebanking.resilience")
public data class ResilienceProperties(
    val enabled: Boolean = false,
    val circuitBreaker: CircuitBreakerProperties = CircuitBreakerProperties(),
    val rateLimiter: RateLimiterProperties = RateLimiterProperties(),
) {
    public data class CircuitBreakerProperties(
        val enabled: Boolean = false,
        val maxBreakers: Long = DEFAULT_MAX_BREAKERS,
        val failureRateThreshold: Float = DEFAULT_FAILURE_RATE_THRESHOLD,
        val slowCallDurationThreshold: Duration = DEFAULT_SLOW_CALL_DURATION_THRESHOLD,
        val slowCallRateThreshold: Float = DEFAULT_SLOW_CALL_RATE_THRESHOLD,
        val waitDurationInOpenState: Duration = DEFAULT_WAIT_DURATION_IN_OPEN_STATE,
        val slidingWindowSize: Int = DEFAULT_SLIDING_WINDOW_SIZE,
        val slidingWindowType: SlidingWindowType = DEFAULT_SLIDING_WINDOW_TYPE,
        val minimumNumberOfCalls: Int = DEFAULT_MINIMUM_NUMBER_OF_CALLS,
        val permittedNumberOfCallsInHalfOpenState: Int = DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN,
        val maxWaitDurationInHalfOpenState: Duration = DEFAULT_MAX_WAIT_DURATION_IN_HALF_OPEN,
        val writableStackTraceEnabled: Boolean = DEFAULT_WRITABLE_STACK_TRACE_ENABLED,
        val instances: Map<String, CircuitBreakerInstanceProperties> = emptyMap(),
    ) {
        init {
            require(maxBreakers > 0) { "maxBreakers must be positive, got $maxBreakers" }
        }

        public fun resolveConfig(name: String): CircuitBreakerConfig {
            val instance = instances[name]
            return CircuitBreakerConfig(
                failureRateThreshold =
                    instance?.failureRateThreshold
                        ?: failureRateThreshold,
                slowCallDurationThreshold =
                    (
                        instance?.slowCallDurationThreshold
                            ?: slowCallDurationThreshold
                    ).toKotlinDuration(),
                slowCallRateThreshold =
                    instance?.slowCallRateThreshold
                        ?: slowCallRateThreshold,
                waitDurationInOpenState =
                    (
                        instance?.waitDurationInOpenState
                            ?: waitDurationInOpenState
                    ).toKotlinDuration(),
                slidingWindowSize =
                    instance?.slidingWindowSize
                        ?: slidingWindowSize,
                slidingWindowType =
                    instance?.slidingWindowType
                        ?: slidingWindowType,
                minimumNumberOfCalls =
                    instance?.minimumNumberOfCalls
                        ?: minimumNumberOfCalls,
                permittedNumberOfCallsInHalfOpenState =
                    instance?.permittedNumberOfCallsInHalfOpenState
                        ?: permittedNumberOfCallsInHalfOpenState,
                maxWaitDurationInHalfOpenState =
                    (
                        instance?.maxWaitDurationInHalfOpenState
                            ?: maxWaitDurationInHalfOpenState
                    ).toKotlinDuration(),
                writableStackTraceEnabled =
                    instance?.writableStackTraceEnabled
                        ?: writableStackTraceEnabled,
            )
        }

        public companion object {
            public const val DEFAULT_MAX_BREAKERS: Long = 10_000
            public const val DEFAULT_FAILURE_RATE_THRESHOLD: Float = 50f
            public const val DEFAULT_SLOW_CALL_RATE_THRESHOLD: Float = 100f
            public val DEFAULT_SLOW_CALL_DURATION_THRESHOLD: Duration = Duration.ofSeconds(2)
            public val DEFAULT_WAIT_DURATION_IN_OPEN_STATE: Duration = Duration.ofSeconds(60)
            public const val DEFAULT_SLIDING_WINDOW_SIZE: Int = 100
            public val DEFAULT_SLIDING_WINDOW_TYPE: SlidingWindowType = SlidingWindowType.COUNT_BASED
            public const val DEFAULT_MINIMUM_NUMBER_OF_CALLS: Int = 10
            public const val DEFAULT_PERMITTED_CALLS_IN_HALF_OPEN: Int = 10
            public val DEFAULT_MAX_WAIT_DURATION_IN_HALF_OPEN: Duration = Duration.ZERO
            public const val DEFAULT_WRITABLE_STACK_TRACE_ENABLED: Boolean = true
        }
    }

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

    public data class RateLimiterProperties(
        val enabled: Boolean = false,
        val maxLimiters: Long = DEFAULT_MAX_LIMITERS,
        val limitForPeriod: Int = DEFAULT_LIMIT_FOR_PERIOD,
        val limitRefreshPeriod: Duration = DEFAULT_LIMIT_REFRESH_PERIOD,
        val timeoutDuration: Duration = DEFAULT_TIMEOUT_DURATION,
        val instances: Map<String, RateLimiterInstanceProperties> = emptyMap(),
    ) {
        init {
            require(maxLimiters > 0) { "maxLimiters must be positive, got $maxLimiters" }
        }

        public fun resolveConfig(name: String): RateLimiterConfig {
            val instance = instances[name]
            return RateLimiterConfig(
                limitForPeriod =
                    instance?.limitForPeriod
                        ?: limitForPeriod,
                limitRefreshPeriod =
                    (
                        instance?.limitRefreshPeriod
                            ?: limitRefreshPeriod
                    ).toKotlinDuration(),
                timeoutDuration =
                    (
                        instance?.timeoutDuration
                            ?: timeoutDuration
                    ).toKotlinDuration(),
            )
        }

        public companion object {
            public const val DEFAULT_MAX_LIMITERS: Long = 10_000
            public const val DEFAULT_LIMIT_FOR_PERIOD: Int = 50
            public val DEFAULT_LIMIT_REFRESH_PERIOD: Duration = Duration.ofSeconds(1)
            public val DEFAULT_TIMEOUT_DURATION: Duration = Duration.ofMillis(500)
        }
    }

    public data class RateLimiterInstanceProperties(
        val limitForPeriod: Int? = null,
        val limitRefreshPeriod: Duration? = null,
        val timeoutDuration: Duration? = null,
    )
}
