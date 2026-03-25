package com.frisboo.corebanking.security.circuitbreaker.model

/**
 * Priority classification for circuit breakers affecting health aggregation.
 *
 * - CRITICAL: Service-affecting operations (payments, transfers).
 *   Open state triggers DOWN health status.
 * - STANDARD: Important but non-critical operations.
 *   Open state triggers DEGRADED health status.
 * - BEST_EFFORT: Optional operations where failures are acceptable.
 *   Does not affect overall health status.
 */
public enum class CircuitBreakerPriorityTier {
    CRITICAL,
    STANDARD,
    BEST_EFFORT
}
