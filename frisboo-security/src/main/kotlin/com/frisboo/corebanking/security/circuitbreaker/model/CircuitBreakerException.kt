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
package com.frisboo.corebanking.security.circuitbreaker.model

public sealed class CircuitBreakerException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    /**
     * Thrown when a CircuitBreaker is in OPEN or FORCED_OPEN state
     * and rejects execution attempts.
     */
    public class CircuitBreakerOpenException(
        message: String,
        public val breakerName: String,
        public val state: CircuitBreakerState,
        cause: Throwable? = null,
    ) : CircuitBreakerException(message, cause) {
        init {
            require(breakerName.isNotBlank()) { "Circuit breaker name must not be blank" }
        }

        override fun toString(): String {
            return "CircuitBreakerOpenException(breakerName='$breakerName', state=$state, message=${message}, cause=${cause?.toString() ?: "null"})"
        }
    }

    public class CircuitBreakerConfigurationException(
        message: String,
        cause: Throwable? = null,
    ) : CircuitBreakerException(message, cause)
}
