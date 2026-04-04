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
 * Exposes the adapter's native configuration object in a type-safe way.
 *
 * @param C the adapter-specific configuration type (e.g.
 *   `io.github.resilience4j.circuitbreaker.CircuitBreakerConfig`).
 */
public interface CircuitBreakerConfigured<out C : Any> {

    /** The adapter's native configuration as originally supplied at creation time. */
    public val internalConfig: C
}
