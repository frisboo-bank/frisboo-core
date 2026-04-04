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
package com.frisboo.corebanking.coordination.reservation.contracts

import kotlin.time.Instant

/**
 * Read-only contract for querying reservation state.
 */
public interface ReservationReader {
    /**
     * Returns whether the resource currently has an active reservation.
     *
     * @param resourceId unique resource identifier.
     * @return `true` when the resource is reserved.
     */
    public suspend fun isReserved(resourceId: String): Boolean

    /**
     * Returns the current reservation holder for the resource.
     *
     * @param resourceId unique resource identifier.
     * @return holder identifier, or `null` when unreserved.
     */
    public suspend fun reservedBy(resourceId: String): String?

    /**
     * Returns the expiration instant for the current reservation.
     *
     * @param resourceId unique resource identifier.
     * @return reservation expiry instant, or `null` when unreserved.
     */
    public suspend fun reservationExpiry(resourceId: String): Instant?
}
