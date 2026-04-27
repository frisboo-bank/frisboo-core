package com.frisboo.corebanking.resilience.circuitbreaker.models

import com.frisboo.corebanking.resilience.autoconfigure.ResilienceProperties
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerConfigSource

internal class PropertiesCircuitBreakerConfigSource(
    private val properties: ResilienceProperties,
) : CircuitBreakerConfigSource {
    override fun resolve(name: String): CircuitBreakerConfig = properties.circuitBreaker.resolveConfig(name)
}
