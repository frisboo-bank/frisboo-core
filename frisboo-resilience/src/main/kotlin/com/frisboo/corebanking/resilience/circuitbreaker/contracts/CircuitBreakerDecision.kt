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
 * Decision-oriented operations for circuit breaker permission and state control.
 */
public interface CircuitBreakerDecision {
    /**
     * Attempts to acquire permission to make a call.
     *
     * This is a low-level primitive for manual recording flows. Do not call
     * before [CircuitBreaker.executeSuspend][com.frisboo.corebanking.resilience.circuitbreaker.contracts.CircuitBreaker.executeSuspend]
     * — it acquires its own permission internally. Acquiring twice leaks a permit.
     *
     * @return `true` if the call is permitted, `false` if the circuit is open.
     */
    public fun tryAcquirePermission(): Boolean

    /** Forces the circuit breaker into the [CircuitBreakerState.OPEN] state. */
    public fun trip()

    /** Resets the circuit breaker to [CircuitBreakerState.CLOSED]. */
    public fun reset()
}
