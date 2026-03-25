package com.frisboo.corebanking.security.circuitbreaker.adapter.resilience4j

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreaker
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerFactory
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerInstrumentation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservationFactory
import com.frisboo.corebanking.security.circuitbreaker.instrumentation.InstrumentedCircuitBreakerImpl
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreaker.of  as VendorOf
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig as VendorConfig

/**
 * Factory for creating Resilience4j-backed circuit breakers.
 */
public class Resilience4jCircuitBreakerFactory(
    private val instrumentation: CircuitBreakerInstrumentation,
    private val observationFactory: CircuitBreakerObservationFactory,
) : CircuitBreakerFactory {

    override fun create(
        name: String,
        config: CircuitBreakerConfig,
    ): CircuitBreaker {
        require(name.isNotBlank()) { "Circuit breaker name must not be blank" }

        val vendorConfig = VendorConfig.custom()
            .failureRateThreshold(config.failureRateThreshold)
            .slowCallRateThreshold(config.slowCallRateThreshold)
            .slowCallDurationThreshold(config.slowCallDurationThreshold)
            .waitDurationInOpenState(config.waitDurationInOpenState)
            .permittedNumberOfCallsInHalfOpenState(config.permittedNumberOfCallsInHalfOpenState)
            .slidingWindowSize(config.slidingWindowSize)
            .minimumNumberOfCalls(config.minimumNumberOfCalls)
            .maxWaitDurationInHalfOpenState(config.maxWaitDurationInHalfOpenState)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build()

        val breaker = Resilience4jCircuitBreaker(name, VendorOf(name, vendorConfig))
        return InstrumentedCircuitBreakerImpl(breaker, instrumentation, observationFactory)
    }
}
