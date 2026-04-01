package com.frisboo.corebanking.security.circuitbreaker.config

import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerPriorityTier
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("frisboo.security.circuitbreaker")
public data class CircuitBreakerProperties(
    // registry configuration properties
    val cleanupInitialDelayMinutes: Long = 60L,
    val cleanupPeriodMinutes: Long = 60L,
    val cleanupTimeBudgetMs: Long = 100L,
    val dynamicBreakerTTL: Duration = Duration.ofHours(1),
    val maxDynamicBreakers: Int = 1000,
    val shutdownTimeoutSeconds: Long = 5L,

    // breaker configuration properties
    val failureRateThreshold: Float = 50f,
    val maxWaitDurationInHalfOpenState: Duration = Duration.ofMinutes(1),
    val minimumNumberOfCalls: Int = 10,
    val permittedNumberOfCallsInHalfOpenState: Int = 10,
    val slidingWindowSize: Int = 100,
    val slowCallDurationThreshold: Duration = Duration.ofSeconds(5),
    val slowCallRateThreshold: Float = 80f,
    val waitDurationInOpenState: Duration = Duration.ofSeconds(30),

    var instances: Map<String, CircuitBreakerInstanceConfig> = emptyMap(),
) {
    public data class CircuitBreakerInstanceConfig(
        val failureRateThreshold: Float? = null,
        val maxWaitDurationInHalfOpenState: Duration? = null,
        val minimumNumberOfCalls: Int? = null,
        val permittedNumberOfCallsInHalfOpenState: Int? = null,
        val priorityTier: CircuitBreakerPriorityTier = CircuitBreakerPriorityTier.STANDARD,
        val slidingWindowSize: Int? = null,
        val slowCallDurationThreshold: Duration? = null,
        val slowCallRateThreshold: Float? = null,
        val waitDurationInOpenState: Duration? = null,
    )
}
