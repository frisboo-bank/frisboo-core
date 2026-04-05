package com.frisboo.corebanking.resilience.circuitbreaker.testutils

import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MAX_FAILURE_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MAX_SLOW_CALL_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_FAILURE_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_MINIMUM_CALLS
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_SLIDING_WINDOW_SIZE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_SLOW_CALL_RATE
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.duration
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

internal fun createCircuitBreakerConfigArb(): Arb<CircuitBreakerConfig> {
    val minWindowSize = max(MIN_SLIDING_WINDOW_SIZE, MIN_MINIMUM_CALLS)

    return Arb.int(minWindowSize, 1000).flatMap { windowSize ->
        Arb.bind(
            Arb.float(MIN_FAILURE_RATE, MAX_FAILURE_RATE),
            Arb.duration(0.milliseconds..30_000.milliseconds),
            Arb.int(MIN_MINIMUM_CALLS, windowSize),
            Arb.int(1, 50),
            Arb.duration(1.milliseconds..10_000.milliseconds),
            Arb.float(MIN_SLOW_CALL_RATE, MAX_SLOW_CALL_RATE),
            Arb.duration(1.milliseconds..10_000.milliseconds),
        ) { failureRate, maxWaitHalfOpen, minCalls, permittedHalfOpen, slowDuration, slowRate, waitOpen ->
            CircuitBreakerConfig(
                failureRateThreshold = failureRate,
                maxWaitDurationInHalfOpenState = maxWaitHalfOpen,
                minimumNumberOfCalls = minCalls,
                permittedNumberOfCallsInHalfOpenState = permittedHalfOpen,
                slidingWindowSize = windowSize,
                slowCallDurationThreshold = slowDuration,
                slowCallRateThreshold = slowRate,
                waitDurationInOpenState = waitOpen,
            )
        }
    }
}
