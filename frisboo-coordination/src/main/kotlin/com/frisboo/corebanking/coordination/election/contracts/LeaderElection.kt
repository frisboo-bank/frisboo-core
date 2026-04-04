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

/**
 * Contract for leader election implementations.
 *
 * Provides single-leader semantics across distributed nodes. At most one node
 * is the leader at any time. Implementations must handle leader failure detection
 * and automatic re-election.
 */
public interface LeaderElection {
    /**
     * Enters this node as a candidate for leadership.
     *
     * @param candidateId unique identifier for this node.
     * @return [Either.Right] on successful candidacy registration, [Either.Left] on failure.
     */
    public suspend fun campaign(candidateId: String): Either<ElectionError, Unit>

    /**
     * Voluntarily resigns leadership. No-op if this node is not the current leader.
     *
     * @param candidateId the identifier of the resigning candidate.
     * @return [Either.Right] on success, [Either.Left] on failure.
     */
    public suspend fun resign(candidateId: String): Either<ElectionError, Unit>

    /**
     * Returns whether the given candidate is currently the leader.
     *
     * @param candidateId the candidate to check.
     * @return `true` if [candidateId] is the current leader.
     */
    public suspend fun isLeader(candidateId: String): Boolean
}

/** Errors that can occur during election operations. */
public sealed interface ElectionError {
    public data class RegistrationFailed(
        val candidateId: String,
        val cause: Throwable? = null,
    ) : ElectionError

    public data class ResignationFailed(
        val candidateId: String,
        val cause: Throwable? = null,
    ) : ElectionError

    public data class Unavailable(
        val cause: Throwable? = null,
    ) : ElectionError
}
