package com.frisboo.corebanking.security.circuitbreaker.model

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreaker
import java.util.concurrent.atomic.AtomicLong

internal data class CircuitBreakerEntry(
    val breaker: CircuitBreaker,
    val lastAccessedAt: AtomicLong,
)
