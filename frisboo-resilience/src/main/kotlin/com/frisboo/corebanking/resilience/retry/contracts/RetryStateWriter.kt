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
package com.frisboo.corebanking.resilience.retry.contracts

import arrow.core.Either

/**
 * Error model for retry state persistence operations.
 */
public sealed interface RetryError {
    /**
     * Indicates retry state store is unavailable.
     *
     * @property reason human-readable failure reason.
     */
    public data class StoreUnavailable(
        public val reason: String,
    ) : RetryError
}

/**
 * Write contract for retry state transitions.
 */
public interface RetryStateWriter {
    /**
     * Records an attempt failure for [operationId].
     */
    public suspend fun recordAttempt(
        operationId: String,
        error: Throwable,
    ): Either<RetryError, Unit>

    /**
     * Resets retry state for [operationId].
     */
    public suspend fun reset(operationId: String): Either<RetryError, Unit>
}
