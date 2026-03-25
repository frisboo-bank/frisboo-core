package com.frisboo.corebanking.security.circuitbreaker.contract

import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerConfig

/**
 * Factory interface for creating circuit breakers.
 */
public interface CircuitBreakerFactory {

    /**
     * Creates a circuit breaker with the given name and configuration.
     *
     * @param name The name of the circuit breaker.
     * @param config The configuration for the circuit breaker.
     * @return A new instance of a circuit breaker.
     */
    public fun create(
        name: String,
        config: CircuitBreakerConfig,
    ): CircuitBreaker
}
