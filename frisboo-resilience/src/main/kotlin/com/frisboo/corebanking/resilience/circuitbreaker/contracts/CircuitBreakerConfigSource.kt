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

import com.frisboo.corebanking.resilience.circuitbreaker.models.CircuitBreakerConfig

/**
 * Resolves [CircuitBreakerConfig] by circuit breaker name.
 *
 * Implementations may read configuration from external sources such as
 * Spring properties, a database, or a remote config service. The factory
 * consults this source when [CircuitBreakerFactory.create] is called
 * without an explicit config.
 */
public fun interface CircuitBreakerConfigSource {
    /**
     * Returns the configuration for the given [name], or `null` if no
     * configuration is defined for that name.
     */
    public fun resolve(name: String): CircuitBreakerConfig?
}
