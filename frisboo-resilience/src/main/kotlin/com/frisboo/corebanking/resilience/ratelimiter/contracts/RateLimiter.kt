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

/**
 * Core contract for rate limiter implementations.
 *
 * Controls the rate of operations by limiting the number of permits
 * available within a configurable time window.
 *
 * Thread-safety: implementations must be safe for concurrent use.
 */
public interface RateLimiter {
    /**
     * Attempts to acquire a single permit without blocking.
     *
     * @return `true` if a permit was acquired, `false` if the rate limit is exceeded.
     */
    public suspend fun tryAcquire(): Boolean

    /**
     * Attempts to acquire the specified number of permits without blocking.
     *
     * @param permits the number of permits to acquire.
     * @return `true` if all permits were acquired, `false` if the rate limit is exceeded.
     */
    public suspend fun tryAcquire(permits: Int): Boolean

    /**
     * Returns the number of permits currently available.
     */
    public suspend fun availablePermits(): Int
}
