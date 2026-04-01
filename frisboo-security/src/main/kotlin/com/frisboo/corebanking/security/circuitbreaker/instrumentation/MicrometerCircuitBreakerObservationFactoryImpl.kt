package com.frisboo.corebanking.security.circuitbreaker.instrumentation

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservation
import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservationFactory
import io.micrometer.observation.ObservationRegistry

public class MicrometerCircuitBreakerObservationFactoryImpl(
    private val observationRegistry: ObservationRegistry,
) : CircuitBreakerObservationFactory {

    override fun createObservation(breakerName: String): CircuitBreakerObservation {
        return MicrometerCircuitBreakerObservationImpl(breakerName, observationRegistry)
    }
}
