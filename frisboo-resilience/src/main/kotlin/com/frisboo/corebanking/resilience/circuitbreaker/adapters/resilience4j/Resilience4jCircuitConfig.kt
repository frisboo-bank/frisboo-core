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

import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.models.SlidingWindowType
import kotlin.time.toJavaDuration
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig as R4jConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType as R4jSlidingWindowType

internal fun CircuitBreakerConfig.toResilience4jConfig(): R4jConfig {
    val builder =
        R4jConfig
            .custom()
            .failureRateThreshold(failureRateThreshold)
            .slowCallDurationThreshold(slowCallDurationThreshold.toJavaDuration())
            .slowCallRateThreshold(slowCallRateThreshold)
            .waitDurationInOpenState(waitDurationInOpenState.toJavaDuration())
            .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
            .minimumNumberOfCalls(minimumNumberOfCalls)
            .slidingWindowSize(slidingWindowSize)
            .slidingWindowType(
                when (slidingWindowType) {
                    SlidingWindowType.COUNT_BASED -> R4jSlidingWindowType.COUNT_BASED
                    SlidingWindowType.TIME_BASED -> R4jSlidingWindowType.TIME_BASED
                },
            ).maxWaitDurationInHalfOpenState(maxWaitDurationInHalfOpenState.toJavaDuration())
            .writableStackTraceEnabled(writableStackTraceEnabled)

    if (recordExceptions.isNotEmpty()) {
        builder.recordExceptions(*recordExceptions.toTypedArray())
    }
    if (ignoreExceptions.isNotEmpty()) {
        builder.ignoreExceptions(*ignoreExceptions.toTypedArray())
    }

    return builder.build()
}
