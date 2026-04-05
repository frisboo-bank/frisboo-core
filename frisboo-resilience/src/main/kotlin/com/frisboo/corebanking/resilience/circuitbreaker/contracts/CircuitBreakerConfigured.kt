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

/**
 * Configuration-oriented operations for circuit breaker permission and state control.
 */
public interface CircuitBreakerConfigured {

    /**
     * Returns the adapter's native configuration object.
     *
     * The actual type depends on the underlying implementation (e.g.
     * `io.github.resilience4j.circuitbreaker.CircuitBreakerConfig` for the
     * Resilience4j adapter). Consumers must cast the result to the expected type.
     */
    public fun getInternalConfig(): Any

}
