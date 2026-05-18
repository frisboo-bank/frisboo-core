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

import com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreakerState
import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerConfig
import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerPersistenceContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import io.github.resilience4j.circuitbreaker.CircuitBreaker as R4jCircuitBreaker

private val logger = KotlinLogging.logger {}

internal suspend fun createResilience4jBreaker(
    name: String,
    config: CircuitBreakerConfig,
    persistence: CircuitBreakerPersistenceContext,
): Resilience4jCircuitBreaker {
    val r4jConfig = config.toResilience4jConfig()
    val r4jBreaker = R4jCircuitBreaker.of(name, r4jConfig)
    val (stateRegistry, coroutineScope) = persistence

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

    val lastStateName =
        try {
            stateRegistry.get(name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warn(e) { "Failed to read circuit breaker state for '$name'; starting with default state" }
            null
        }

    if (lastStateName != null) {
        val state = CircuitBreakerState.fromStateNameOrNull(lastStateName)
        if (state == null) {
            logger.warn { "Unknown persisted circuit breaker state '$lastStateName' for '$name'; ignoring" }
        } else {
            r4jBreaker.transitionTo(state)
        }
    }

    return Resilience4jCircuitBreaker(r4jBreaker, config)
}
