package com.frisboo.corebanking.security.circuitbreaker.contract

public interface CircuitBreakerObservationFactory {

    public fun createObservation(breakerName: String): CircuitBreakerObservation
}
