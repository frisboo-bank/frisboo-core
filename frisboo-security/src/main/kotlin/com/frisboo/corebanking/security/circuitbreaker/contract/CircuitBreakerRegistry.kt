package com.frisboo.corebanking.security.circuitbreaker.contract

import com.frisboo.corebanking.security.circuitbreaker.config.CircuitBreakerProperties.CircuitBreakerInstanceConfig
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerSnapshot

public interface CircuitBreakerRegistry : AutoCloseable {

    public fun start()

    public fun getBreakersSnapshot(page: Int, size: Int): Map<String, CircuitBreakerSnapshot>

    @Throws(NoSuchElementException::class)
    public fun get(name: String): CircuitBreaker

    public fun getOrCreate(name: String, config: CircuitBreakerInstanceConfig): CircuitBreaker
}
