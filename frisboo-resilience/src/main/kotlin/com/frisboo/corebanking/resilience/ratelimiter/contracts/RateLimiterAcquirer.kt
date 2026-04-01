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
package com.frisboo.corebanking.resilience.ratelimiter.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Error model for rate limiter acquisition operations.
 */
public sealed interface RateLimiterError {
    /**
     * Indicates permit acquisition was rejected due to exceeded limits.
     *
     * @property requestedPermits number of permits requested.
     */
    public data class LimitExceeded(
        public val requestedPermits: Int,
    ) : RateLimiterError

    /**
     * Indicates the rate limiter backend is unavailable.
     *
     * @property reason human-readable failure reason.
     * @property retryAfter suggested retry delay.
     */
    public data class Unavailable(
        public val reason: String,
        public val retryAfter: Duration? = null,
    ) : RateLimiterError
}

/**
 * Contract for acquiring permits from a rate limiter.
 */
public interface RateLimiterAcquirer {
    /**
     * Acquires [permits] permits or returns a [RateLimiterError].
     *
     * @param permits number of permits to acquire.
     */
    public suspend fun acquire(permits: Int = 1): Either<RateLimiterError, Unit>
}
