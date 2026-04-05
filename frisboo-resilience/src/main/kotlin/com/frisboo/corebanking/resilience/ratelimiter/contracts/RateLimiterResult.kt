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

import com.frisboo.corebanking.resilience.ratelimiter.errors.RateLimiterError.LimitExceeded

public sealed class RateLimiterResult<out T> {

    public data class Success<T>(public val value: T) : RateLimiterResult<T>()

    public data class Failure(public val cause: Throwable) : RateLimiterResult<Nothing>()

    public data class Rejected(public val error: LimitExceeded) : RateLimiterResult<Nothing>()
}
