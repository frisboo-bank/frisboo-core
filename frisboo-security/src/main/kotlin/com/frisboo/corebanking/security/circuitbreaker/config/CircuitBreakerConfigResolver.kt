package com.frisboo.corebanking.security.circuitbreaker.config

import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerConfig
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerPriorityTier
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerRegistryConfig

public object CircuitBreakerConfigResolver {

    public fun resolveRegistry(properties: CircuitBreakerProperties): CircuitBreakerRegistryConfig {
        return CircuitBreakerRegistryConfig(
            cleanupInitialDelayMinutes = properties.cleanupInitialDelayMinutes,
            cleanupPeriodMinutes = properties.cleanupPeriodMinutes,
            cleanupTimeBudgetMs = properties.cleanupTimeBudgetMs,
            dynamicBreakerTTL = properties.dynamicBreakerTTL,
            maxDynamicBreakers = properties.maxDynamicBreakers,
            shutdownTimeoutSeconds = properties.shutdownTimeoutSeconds,
        )
    }

    public fun resolve(properties: CircuitBreakerProperties): CircuitBreakerConfig {
        return CircuitBreakerConfig(
            failureRateThreshold = properties.failureRateThreshold,
            maxWaitDurationInHalfOpenState = properties.maxWaitDurationInHalfOpenState,
            minimumNumberOfCalls = properties.minimumNumberOfCalls,
            permittedNumberOfCallsInHalfOpenState = properties.permittedNumberOfCallsInHalfOpenState,
            priorityTier = CircuitBreakerPriorityTier.STANDARD,
            slidingWindowSize = properties.slidingWindowSize,
            slowCallDurationThreshold = properties.slowCallDurationThreshold,
            slowCallRateThreshold = properties.slowCallRateThreshold,
            waitDurationInOpenState = properties.waitDurationInOpenState,
        )
    }

    public fun resolve(
        global: CircuitBreakerConfig,
        instance: CircuitBreakerProperties.CircuitBreakerInstanceConfig,
    ): CircuitBreakerConfig {
        return CircuitBreakerConfig(
            failureRateThreshold = instance.failureRateThreshold ?: global.failureRateThreshold,
            maxWaitDurationInHalfOpenState = instance.maxWaitDurationInHalfOpenState
                ?: global.maxWaitDurationInHalfOpenState,
            minimumNumberOfCalls = instance.minimumNumberOfCalls ?: global.minimumNumberOfCalls,
            permittedNumberOfCallsInHalfOpenState = instance.permittedNumberOfCallsInHalfOpenState
                ?: global.permittedNumberOfCallsInHalfOpenState,
            priorityTier = instance.priorityTier,
            slidingWindowSize = instance.slidingWindowSize ?: global.slidingWindowSize,
            slowCallDurationThreshold = instance.slowCallDurationThreshold ?: global.slowCallDurationThreshold,
            slowCallRateThreshold = instance.slowCallRateThreshold ?: global.slowCallRateThreshold,
            waitDurationInOpenState = instance.waitDurationInOpenState ?: global.waitDurationInOpenState,
        )
    }
}
