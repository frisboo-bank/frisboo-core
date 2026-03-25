package com.frisboo.corebanking.security.circuitbreaker.instrumentation

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerObservation
import com.frisboo.corebanking.security.circuitbreaker.model.CircuitBreakerState
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import java.time.Duration

public class MicrometerCircuitBreakerObservationImpl(
    private val name: String,
    private val observationRegistry: ObservationRegistry,
) : CircuitBreakerObservation {

    private var observation: Observation? = null

    override fun start() {
        observation = Observation.start("circuitbreaker.execute", observationRegistry)
            .lowCardinalityKeyValue("circuitbreaker.name", name)
    }

    override fun recordSuccess(duration: Duration) {
        observation?.lowCardinalityKeyValue("circuitbreaker.result", "success")
        observation?.event(Observation.Event.of("success"))
    }

    override fun recordFailure(duration: Duration, throwable: Throwable) {
        observation?.lowCardinalityKeyValue("circuitbreaker.result", "failure")
        observation?.error(throwable)
        observation?.event(Observation.Event.of("failure"))
    }

    override fun recordNotPermitted(duration: Duration, state: CircuitBreakerState) {
        observation?.lowCardinalityKeyValue("circuitbreaker.result", "not_permitted")
        observation?.lowCardinalityKeyValue("circuitbreaker.state", state.name)
        observation?.event(Observation.Event.of("not_permitted"))
    }

    override fun close() {
        observation?.stop()
        observation = null
    }
}
