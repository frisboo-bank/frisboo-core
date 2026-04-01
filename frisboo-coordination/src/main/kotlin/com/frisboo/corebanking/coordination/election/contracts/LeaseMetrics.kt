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
package com.frisboo.corebanking.coordination.election.contracts

import kotlin.time.Instant

/**
 * Read-only contract exposing current lease leadership metrics.
 */
public interface LeaseMetrics {
    /**
     * Returns the current lease holder identifier.
     *
     * @return leader candidate identifier, or `null` if no lease is currently held.
     */
    public suspend fun currentLeader(): String?

    /**
     * Returns the expiration instant of the active lease.
     *
     * @return lease expiry instant, or `null` if no lease is currently active.
     */
    public suspend fun leaseExpiry(): Instant?

    /**
     * Returns whether the currently tracked lease is valid at evaluation time.
     *
     * @return `true` when there is a non-expired active lease.
     */
    public suspend fun isLeaseValid(): Boolean
}
