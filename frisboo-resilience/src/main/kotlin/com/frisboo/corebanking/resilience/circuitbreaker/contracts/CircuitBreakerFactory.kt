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
 * Factory for creating and managing [CircuitBreaker] instances.
 *
 * Implementations are responsible for lifecycle management and may maintain
 * an internal registry of created breakers.
 */
public interface CircuitBreakerFactory {
    /**
     * Returns a circuit breaker for the given [name], creating one if none exists.
     *
     * This operation is **idempotent by name while the instance is retained** by the
     * factory. Repeated calls with the same [name] and [config] return the same
     * [CircuitBreaker] instance as long as the factory has not evicted it. After
     * eviction (e.g. TTL or capacity pressure), a new instance is created and its
     * persisted state is restored.
     *
     * If called with the same [name] but a **different** [config], the existing
     * breaker is replaced with a new one using the new configuration and a warning
     * is logged. This prevents caller crashes while surfacing config drift for
     * operators to fix.
     *
     * @param name unique identifier for the circuit breaker.
     * @param config the configuration parameters.
     * @return the [CircuitBreaker] for [name] (possibly cached).
     */
    public suspend fun create(
        name: String,
        config: CircuitBreakerConfig,
    ): CircuitBreaker

    /**
     * Returns a circuit breaker for the given [name], resolving configuration
     * from the factory's [CircuitBreakerConfigSource].
     *
     * @throws IllegalStateException if no config source is configured or the
     *   source returns `null` for [name].
     */
    public suspend fun create(name: String): CircuitBreaker
}
