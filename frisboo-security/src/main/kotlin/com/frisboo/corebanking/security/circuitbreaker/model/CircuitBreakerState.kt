package com.frisboo.corebanking.security.circuitbreaker.model

/**
 * Circuit breaker state machine states.
 *
 * State transitions:
 * - CLOSED → OPEN: When failure rate exceeds threshold
 * - OPEN → HALF_OPEN: After waitDurationInOpenState expires
 * - HALF_OPEN → CLOSED: When test calls succeed
 * - HALF_OPEN → OPEN: When test calls fail
 * - Any → DISABLED: Manual administrative override
 * - Any → FORCED_OPEN: Manual administrative override
 */
public enum class CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN,
    DISABLED,
    FORCED_OPEN,
}
