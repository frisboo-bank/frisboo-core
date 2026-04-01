package com.frisboo.corebanking.security.circuitbreaker.contract

import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import java.time.Duration

public interface CircuitBreakerInstrumentation {

    // Registry events
    public fun recordBreakerAccessed(name: String)

    public fun recordDynamicBreakerCreated(name: String)

    public fun recordDynamicBreakerEvicted(name: String)

    // Breaker events
    public fun recordCallStarted(name: String)

    public fun recordCallSuccess(name: String, duration: Duration)

    public fun recordCallFailure(name: String, duration: Duration, throwable: Throwable)

    public fun recordCallNotPermitted(name: String, duration: Duration, state: CircuitBreakerState)

    public fun recordStateTransition(name: String, fromState: CircuitBreakerState, toState: CircuitBreakerState)

    public fun recordRegistryCleanup(removedCount: Int, remainingCount: Int)

    public fun revokeBreakerMetrics(name: String)
}
