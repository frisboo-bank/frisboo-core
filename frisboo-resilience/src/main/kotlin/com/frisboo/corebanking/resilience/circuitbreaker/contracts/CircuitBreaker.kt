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
package com.frisboo.corebanking.resilience.circuitbreaker.contracts

import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig

/**
 * Core contract for circuit breaker implementations.
 *
 * Provides fault tolerance by preventing cascading failures. Implementations
 * track call outcomes within a sliding window and transition between [CircuitBreakerState]s
 * to protect downstream services.
 *
 * Thread-safety: implementations must be safe for concurrent use.
 */
public interface CircuitBreaker :
    CircuitBreakerDecision,
    CircuitBreakerRecorder,
    CircuitBreakerConfigured {

    /** Read‑only metrics and state. */
    public val metrics: CircuitBreakerMetrics

    /** Read‑only domain configuration as originally supplied at creation time. */
    public val config: CircuitBreakerConfig

    /**
     * Executes the given [block] within the circuit breaker's protection.
     *
     * @param block the suspend function to execute.
     * @return the result of [block].
     * @throws CallNotPermittedException if the circuit is open.
     * @throws Error JVM errors are always rethrown.
     * @throws kotlin.coroutines.cancellation.CancellationException propagated for structured concurrency.
     */
    public suspend fun <T> executeSuspend(block: suspend () -> T): T

    /**
     * Executes the given [block] within the circuit breaker's protection,
     * falling back to [fallback] on failure.
     *
     * When the circuit is open and the call is not permitted, [fallback] receives a
     * [CallNotPermittedException]. On execution failure, [fallback] receives the
     * original exception. JVM [Error]s are never sent to [fallback] — they are
     * always rethrown.
     *
     * @param block the suspend function to execute.
     * @param fallback the fallback function invoked with the failure cause.
     * @return the result of [block] or [fallback].
     */
    public suspend fun <T> executeSuspend(
        block: suspend () -> T,
        fallback: suspend (Throwable) -> T,
    ): T
}
