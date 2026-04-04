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

import java.time.Duration

/**
 * Configuration for a rate limiter instance.
 *
 * @property limitForPeriod maximum number of permits available in one period.
 * @property limitRefreshPeriod duration of one period after which permits are refreshed.
 * @property timeoutDuration maximum time a thread will wait for a permit.
 */
public data class RateLimiterConfig(
    val limitForPeriod: Int,
    val limitRefreshPeriod: Duration,
    val timeoutDuration: Duration,
) {
    init {
        require(limitForPeriod > 0) { "limitForPeriod must be positive, got $limitForPeriod" }
        require(!limitRefreshPeriod.isNegative && !limitRefreshPeriod.isZero) {
            "limitRefreshPeriod must be positive"
        }
        require(!timeoutDuration.isNegative) {
            "timeoutDuration must not be negative"
        }
    }
}
