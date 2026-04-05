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
package com.frisboo.corebanking.resilience.ratelimiter.model

import kotlin.time.Duration

public data class RateLimiterConfig(
    val limitForPeriod: Int,
    val limitRefreshPeriod: Duration,
    val timeoutDuration: Duration,
) {
    init {
        require(
            limitForPeriod in MIN_LIMIT_FOR_PERIOD..MAX_LIMIT_FOR_PERIOD,
        ) { "limitForPeriod must be in $MIN_LIMIT_FOR_PERIOD..$MAX_LIMIT_FOR_PERIOD, got $limitForPeriod" }
        require(limitRefreshPeriod.isPositive()) { "limitRefreshPeriod must be positive" }
        require(!timeoutDuration.isNegative()) { "timeoutDuration must not be negative" }
    }

    public companion object {
        public const val MIN_LIMIT_FOR_PERIOD: Int = 1
        public const val MAX_LIMIT_FOR_PERIOD: Int = 1_000_000
    }
}
