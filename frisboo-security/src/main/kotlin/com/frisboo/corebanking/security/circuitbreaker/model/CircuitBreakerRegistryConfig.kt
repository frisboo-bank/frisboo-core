package com.frisboo.corebanking.security.circuitbreaker.model

import java.time.Duration

/**
 * Configuration for the circuit breaker registry.
 */
public data class CircuitBreakerRegistryConfig(
    val cleanupInitialDelayMinutes: Long,
    val cleanupPeriodMinutes: Long,
    val cleanupTimeBudgetMs: Long,
    val dynamicBreakerTTL: Duration,
    val maxDynamicBreakers: Int,
    val shutdownTimeoutSeconds: Long,
) {
    init {
        validate()
    }

    private fun validate() {
        require(cleanupInitialDelayMinutes > 0) { "cleanupInitialDelayMinutes must be > 0" }
        require(cleanupPeriodMinutes > 0) { "cleanupPeriodMinutes must be > 0" }
        require(cleanupTimeBudgetMs > 0) { "cleanupTimeBudgetMs must be > 0" }
        require(dynamicBreakerTTL > Duration.ZERO) { "dynamicBreakerTtl must be positive" }
        require(maxDynamicBreakers > 0) { "maxDynamicBreakers must be > 0" }
        require(shutdownTimeoutSeconds > 0) { "shutdownTimeoutSeconds must be > 0" }
    }
}
