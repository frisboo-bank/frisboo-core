package com.frisboo.corebanking.security.circuitbreaker.actuator

import com.frisboo.corebanking.security.circuitbreaker.contract.CircuitBreakerRegistry
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import kotlin.collections.mapValues

@Endpoint(id = "circuitbreakers")
public class CircuitBreakerEndpoint(
    private val registry: CircuitBreakerRegistry,
) {

    @ReadOperation
    public fun listBreakers(
        @Selector page: Int = 1,
        @Selector size: Int = 100,
    ): Map<String, CircuitBreakerSnapshotDto> {
        val snapshots = registry.getBreakersSnapshot(page - 1, size)

        return snapshots.mapValues { (_, snapshot) ->
            CircuitBreakerSnapshotDto(
                state = snapshot.state.name,
                failureRate = snapshot.failureRate,
                numberOfBufferedCalls = snapshot.numberOfBufferedCalls,
                numberOfFailedCalls = snapshot.numberOfFailedCalls,
                numberOfSuccessfulCalls = snapshot.numberOfSuccessfulCalls,
            )
        }
    }
}
