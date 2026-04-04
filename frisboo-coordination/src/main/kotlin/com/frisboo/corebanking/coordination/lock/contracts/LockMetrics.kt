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
package com.frisboo.corebanking.coordination.lock.contracts

import kotlin.time.Instant

/**
 * Read-only contract exposing distributed lock state and ownership metrics.
 */
public interface LockMetrics {
    /**
     * Returns whether the specified resource is currently locked.
     *
     * @param resource resource identifier.
     * @return `true` when the resource currently has an active lock.
     */
    public suspend fun isLocked(resource: String): Boolean

    /**
     * Returns the current holder identifier for the resource lock.
     *
     * @param resource resource identifier.
     * @return lock holder identifier, or `null` if the resource is unlocked.
     */
    public suspend fun holderOf(resource: String): String?

    /**
     * Returns the lock expiration instant for the resource.
     *
     * @param resource resource identifier.
     * @return lock expiry instant, or `null` when no active lock exists.
     */
    public suspend fun lockExpiry(resource: String): Instant?
}
