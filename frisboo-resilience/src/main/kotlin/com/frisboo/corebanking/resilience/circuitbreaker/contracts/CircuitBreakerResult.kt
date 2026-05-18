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

import com.frisboo.corebanking.resilience.circuitbreaker.errors.CircuitBreakerError

/**
 * Result of a circuit breaker–protected operation.
 *
 * Callers must handle all three outcomes via exhaustive `when`:
 * - [Success]: the protected block completed normally.
 * - [Failure]: the protected block threw an exception.
 * - [Rejected]: the circuit is open — the call was not attempted.
 */
public sealed class CircuitBreakerResult<out T> {
    public data class Success<T>(
        public val value: T,
    ) : CircuitBreakerResult<T>()

    public data class Failure(
        public val cause: Throwable,
    ) : CircuitBreakerResult<Nothing>()

    public data class Rejected(
        public val error: CircuitBreakerError.CallNotPermitted,
    ) : CircuitBreakerResult<Nothing>()
}
