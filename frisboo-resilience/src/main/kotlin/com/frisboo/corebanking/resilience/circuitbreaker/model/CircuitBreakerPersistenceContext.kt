package com.frisboo.corebanking.resilience.circuitbreaker.model

import com.frisboo.corebanking.registry.contracts.Registry
import kotlinx.coroutines.CoroutineScope

internal data class CircuitBreakerPersistenceContext(
    val stateRegistry: Registry<String, String>,
    val coroutineScope: CoroutineScope,
)
