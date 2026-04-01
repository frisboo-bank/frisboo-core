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

import arrow.core.Either
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Contract for acquiring and renewing leadership leases.
 */
public interface LeaseAcquirer {
    /**
     * Attempts to acquire a lease for the candidate.
     *
     * @param candidateId unique identifier of the candidate requesting the lease.
     * @param leaseDuration requested lease duration.
     * @return [Either.Right] on successful lease acquisition, [Either.Left] with [LeaseError] on failure.
     */
    public suspend fun acquire(
        candidateId: String,
        leaseDuration: Duration,
    ): Either<LeaseError, Unit>

    /**
     * Attempts to renew an existing lease for the candidate.
     *
     * @param candidateId unique identifier of the candidate renewing the lease.
     * @param leaseDuration requested extension duration.
     * @return [Either.Right] on successful lease renewal, [Either.Left] with [LeaseError] on failure.
     */
    public suspend fun renew(
        candidateId: String,
        leaseDuration: Duration,
    ): Either<LeaseError, Unit>
}

/**
 * Errors that can occur during lease acquisition and renewal operations.
 */
public sealed interface LeaseError {
    /**
     * The lease is currently held by another candidate.
     *
     * @property currentHolder identifier of the active lease holder.
     */
    public data class AlreadyHeld(
        val currentHolder: String,
    ) : LeaseError

    /**
     * The lease state is expired and cannot be renewed or released as requested.
     *
     * @property candidateId candidate associated with the expired lease operation.
     * @property expiredAt instant when expiry was observed.
     */
    public data class Expired(
        val candidateId: String,
        val expiredAt: Instant,
    ) : LeaseError

    /**
     * The underlying lease coordination backend is unavailable.
     *
     * @property cause root cause when available.
     */
    public data class Unavailable(
        val cause: Throwable? = null,
    ) : LeaseError
}
