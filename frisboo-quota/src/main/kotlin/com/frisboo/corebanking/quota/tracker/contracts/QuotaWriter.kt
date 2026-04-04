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
package com.frisboo.corebanking.quota.tracker.contracts

import arrow.core.Either

/**
 * Describes failures that can occur while mutating quota state.
 */
public sealed interface QuotaError {
    /**
     * Indicates quota consumption exceeded the configured limit.
     */
    public data class Exhausted(
        public val quotaId: String,
        public val limit: Long,
    ) : QuotaError

    /**
     * Indicates the requested quota identifier does not exist.
     */
    public data class NotFound(
        public val quotaId: String,
    ) : QuotaError

    /**
     * Indicates the underlying quota system is unavailable.
     */
    public data class Unavailable(
        public val cause: Throwable? = null,
    ) : QuotaError
}

/**
 * Provides write operations for quota state.
 */
public interface QuotaWriter {
    /**
     * Consumes [amount] units and returns the remaining quota.
     */
    public suspend fun consume(
        quotaId: String,
        amount: Long = 1,
    ): Either<QuotaError, Long>

    /**
     * Releases [amount] units and returns the remaining quota.
     */
    public suspend fun release(
        quotaId: String,
        amount: Long = 1,
    ): Either<QuotaError, Long>

    /**
     * Resets usage for the provided quota identifier.
     */
    public suspend fun reset(quotaId: String): Either<QuotaError, Unit>
}
