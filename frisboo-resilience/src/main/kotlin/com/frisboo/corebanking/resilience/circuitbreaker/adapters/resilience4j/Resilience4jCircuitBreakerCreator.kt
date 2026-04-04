/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.resilience.circuitbreaker.adapters.resilience4j

import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.resilience4j.circuitbreaker.CircuitBreaker as R4jCircuitBreaker

private val logger = KotlinLogging.logger {}

internal suspend fun createResilience4jBreaker(
    name: String,
    config: CircuitBreakerConfig,
    stateRegistry: Registry<String, String>,
    coroutineScope: CoroutineScope,
): Resilience4jCircuitBreaker {
    val r4jConfig = config.toResilience4jConfig()
    val r4jBreaker = R4jCircuitBreaker.of(name, r4jConfig)

    r4jBreaker.eventPublisher.onStateTransition { event ->
        val newState = CircuitBreakerState.from(event.stateTransition.toState)
        coroutineScope.launch {
            try {
                stateRegistry.put(name, newState.stateName)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn(e) { "Failed to persist circuit breaker state for '$name'" }
            }
        }
    }

    restoreState(name, r4jBreaker, stateRegistry)

    return Resilience4jCircuitBreaker(r4jBreaker)
}

private suspend fun restoreState(
    name: String,
    breaker: R4jCircuitBreaker,
    stateRegistry: Registry<String, String>,
) {
    val lastStateName = try {
        stateRegistry.get(name)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.warn(e) { "Failed to read circuit breaker state for '$name'; starting with default state" }
        return
    }

    if (lastStateName != null) {
        val state = CircuitBreakerState.fromStateNameOrNull(lastStateName)
        if (state != null) {
            breaker.transitionTo(state)
        } else {
            logger.warn { "Unknown persisted circuit breaker state '$lastStateName' for '$name'; ignoring" }
        }
    }
}
