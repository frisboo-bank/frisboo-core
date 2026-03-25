package com.frisboo.corebanking.security.circuitbreaker.model

public enum class CircuitBreakerRegistryState {
    IDLE,
    STARTED,
    CLOSED,
}
