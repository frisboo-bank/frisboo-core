package com.frisboo.corebanking.security.circuitbreaker.contract

import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import java.time.Duration

public interface CircuitBreakerObservation : AutoCloseable {

    public fun start()

    public fun recordSuccess(duration: Duration)

    public fun recordFailure(duration: Duration, throwable: Throwable)

    public fun recordNotPermitted(duration: Duration, state: CircuitBreakerState)

    override fun close()
}
