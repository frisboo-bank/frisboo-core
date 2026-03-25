package com.frisboo.corebanking.security.circuitbreaker.model

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreaker

public data class CircuitBreakerSnapshot(
    val state: CircuitBreakerState,
    val failureRate: Float,
    val numberOfBufferedCalls: Int,
    val numberOfFailedCalls: Int,
    val numberOfSuccessfulCalls: Int,
) {
    public companion object {
        public fun from(breaker: CircuitBreaker): CircuitBreakerSnapshot {
            return CircuitBreakerSnapshot(
                state = breaker.state,
                failureRate = breaker.failureRate,
                numberOfBufferedCalls = breaker.numberOfBufferedCalls,
                numberOfFailedCalls = breaker.numberOfFailedCalls,
                numberOfSuccessfulCalls = breaker.numberOfSuccessfulCalls,
            )
        }
    }
}
