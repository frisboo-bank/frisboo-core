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

import arrow.core.Either
import kotlin.time.Duration

/**
 * Write contract for reservation lifecycle operations.
 */
public interface ReservationWriter {
    /**
     * Creates a reservation for a resource under a holder identity.
     *
     * @param resourceId unique resource identifier.
     * @param holderId unique holder identifier.
     * @param ttl reservation time-to-live.
     * @return [Either.Right] on success, [Either.Left] with [ReservationError] on failure.
     */
    public suspend fun reserve(
        resourceId: String,
        holderId: String,
        ttl: Duration,
    ): Either<ReservationError, Unit>

    /**
     * Releases a reservation held by a holder identity.
     *
     * @param resourceId unique resource identifier.
     * @param holderId unique holder identifier expected to own the reservation.
     * @return [Either.Right] on success, [Either.Left] with [ReservationError] on failure.
     */
    public suspend fun release(
        resourceId: String,
        holderId: String,
    ): Either<ReservationError, Unit>
}

/**
 * Errors that can occur during reservation operations.
 */
public sealed interface ReservationError {
    /**
     * The resource is already reserved by another holder.
     *
     * @property resourceId reserved resource identifier.
     * @property currentHolder current reservation holder identifier.
     */
    public data class AlreadyReserved(
        val resourceId: String,
        val currentHolder: String,
    ) : ReservationError

    /**
     * The resource has no active reservation.
     *
     * @property resourceId resource identifier that was expected to be reserved.
     */
    public data class NotReserved(
        val resourceId: String,
    ) : ReservationError

    /**
     * The reservation backend is unavailable.
     *
     * @property cause root cause when available.
     */
    public data class Unavailable(
        val cause: Throwable? = null,
    ) : ReservationError
}
