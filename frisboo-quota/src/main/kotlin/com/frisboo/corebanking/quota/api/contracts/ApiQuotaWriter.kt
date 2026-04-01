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
package com.frisboo.corebanking.quota.api.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Describes failures that can occur while mutating API quota state.
 */
public sealed interface ApiQuotaError {
    /**
     * Indicates API rate limit was exceeded for the key and endpoint.
     */
    public data class RateLimitExceeded(
        public val apiKey: String,
        public val endpoint: String,
        public val retryAfter: Duration,
    ) : ApiQuotaError

    /**
     * Indicates the underlying quota system is unavailable.
     */
    public data class Unavailable(
        public val cause: Throwable? = null,
    ) : ApiQuotaError
}

/**
 * Provides write operations for API quota state.
 */
public interface ApiQuotaWriter {
    /**
     * Records a request and returns remaining requests.
     */
    public suspend fun recordRequest(
        apiKey: String,
        endpoint: String,
    ): Either<ApiQuotaError, Long>

    /**
     * Resets usage for the given API key and endpoint.
     */
    public suspend fun resetUsage(
        apiKey: String,
        endpoint: String,
    ): Either<ApiQuotaError, Unit>
}
