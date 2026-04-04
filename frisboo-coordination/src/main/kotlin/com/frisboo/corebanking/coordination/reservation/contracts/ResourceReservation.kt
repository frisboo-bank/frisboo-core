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
 * Aggregate reservation facade combining read and write contracts.
 */
public interface ResourceReservation :
    ReservationReader,
    ReservationWriter {
    /**
     * Reserves the resource when free, or extends it when already owned by the same holder.
     *
     * @param resourceId unique resource identifier.
     * @param holderId unique holder identifier.
     * @param ttl reservation time-to-live.
     * @return [Either.Right] on success, [Either.Left] with [ReservationError] on failure.
     */
    public suspend fun reserveOrExtend(
        resourceId: String,
        holderId: String,
        ttl: Duration,
    ): Either<ReservationError, Unit>
}
