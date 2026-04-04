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

import arrow.core.Either
import com.frisboo.corebanking.coordination.lock.model.LockConfig

/**
 * Contract for distributed lock implementations.
 *
 * Provides mutual exclusion across distributed nodes for a named resource.
 * Implementations must guarantee that at most one holder owns the lock at any time
 * and that locks are released on timeout to prevent deadlocks.
 */
public interface DistributedLock {
    /**
     * Attempts to acquire a lock on the given resource.
     *
     * @param resource the unique name identifying the resource to lock.
     * @param config the lock acquisition and hold parameters.
     * @return [Either.Right] with a lock token on success, [Either.Left] with a [LockError] on failure.
     */
    public suspend fun acquire(
        resource: String,
        config: LockConfig,
    ): Either<LockError, LockToken>

    /**
     * Releases a previously acquired lock.
     *
     * @param token the token returned from a successful [acquire] call.
     * @return [Either.Right] on success, [Either.Left] with a [LockError] if the lock was already released or expired.
     */
    public suspend fun release(token: LockToken): Either<LockError, Unit>

    /**
     * Acquires a lock, executes [block], and releases the lock regardless of outcome.
     *
     * @param resource the unique name identifying the resource to lock.
     * @param config the lock acquisition and hold parameters.
     * @param block the suspend function to execute while holding the lock.
     * @return [Either.Right] with the result of [block], or [Either.Left] with a [LockError].
     */
    public suspend fun <T> withLock(
        resource: String,
        config: LockConfig,
        block: suspend () -> T,
    ): Either<LockError, T>
}

/**
 * Token representing an acquired lock. Used to release the lock.
 *
 * @property resource the locked resource name.
 * @property tokenValue opaque token value for lock ownership verification.
 */
public data class LockToken(
    val resource: String,
    val tokenValue: String,
)

/** Errors that can occur during lock operations. */
public sealed interface LockError {
    public data class AcquireTimeout(
        val resource: String,
    ) : LockError

    public data class AlreadyReleased(
        val resource: String,
    ) : LockError

    public data class Unavailable(
        val resource: String,
        val cause: Throwable? = null,
    ) : LockError
}
