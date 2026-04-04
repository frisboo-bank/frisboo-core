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

public interface CircuitBreakerMetrics {

    /** Current circuit breaker state. */
    public val state: CircuitBreakerState

    /**
     * Current failure rate percentage.
     * Returns -1 if insufficient data.
     */
    public val failureRate: Float

    /**
     * Current slow call rate percentage.
     * Returns -1 if insufficient data.
     */
    public val slowCallRate: Float

    /** Total number of buffered calls in the sliding window. */
    public val numberOfBufferedCalls: Int

    /** Number of failed calls in the sliding window. */
    public val numberOfFailedCalls: Int

    /** Number of successful calls in the sliding window. */
    public val numberOfSuccessfulCalls: Int

    /** Total number of calls that were slower than the threshold. */
    public val numberOfSlowCalls: Int

    /** Number of successful calls that were slower than the threshold. */
    public val numberOfSlowSuccessfulCalls: Int

    /** Number of failed calls that were slower than the threshold. */
    public val numberOfSlowFailedCalls: Int

    /** Number of calls rejected because the circuit is open. */
    public val numberOfNotPermittedCalls: Long

    public companion object
}
