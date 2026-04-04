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
package com.frisboo.corebanking.coordination.session.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Write contract for session lifecycle operations.
 */
public interface SessionWriter {
    /**
     * Creates a new session bound to a node with optional attributes.
     *
     * @param sessionId unique session identifier.
     * @param nodeId unique owner node identifier.
     * @param ttl session time-to-live.
     * @param attributes initial session attributes.
     * @return [Either.Right] on success, [Either.Left] with [SessionError] on failure.
     */
    public suspend fun create(
        sessionId: String,
        nodeId: String,
        ttl: Duration,
        attributes: Map<String, String> = emptyMap(),
    ): Either<SessionError, Unit>

    /**
     * Extends the lifetime of an existing session.
     *
     * @param sessionId unique session identifier.
     * @param ttl new session time-to-live.
     * @return [Either.Right] on success, [Either.Left] with [SessionError] on failure.
     */
    public suspend fun touch(
        sessionId: String,
        ttl: Duration,
    ): Either<SessionError, Unit>

    /**
     * Destroys an existing session.
     *
     * @param sessionId unique session identifier.
     * @return [Either.Right] on success, [Either.Left] with [SessionError] on failure.
     */
    public suspend fun destroy(sessionId: String): Either<SessionError, Unit>
}

/**
 * Errors that can occur during session operations.
 */
public sealed interface SessionError {
    /**
     * The session already exists and cannot be created again.
     *
     * @property sessionId session identifier that already exists.
     */
    public data class AlreadyExists(
        val sessionId: String,
    ) : SessionError

    /**
     * The session does not exist.
     *
     * @property sessionId session identifier that could not be found.
     */
    public data class NotFound(
        val sessionId: String,
    ) : SessionError

    /**
     * The session backend is unavailable.
     *
     * @property cause root cause when available.
     */
    public data class Unavailable(
        val cause: Throwable? = null,
    ) : SessionError
}
