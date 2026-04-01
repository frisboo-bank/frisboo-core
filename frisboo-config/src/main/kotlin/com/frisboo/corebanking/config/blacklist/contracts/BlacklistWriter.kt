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
package com.frisboo.corebanking.config.blacklist.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Write contract for token blacklist mutations.
 */
public interface BlacklistWriter {
    /**
     * Adds a token to the blacklist with a reason and optional TTL.
     */
    public suspend fun add(
        token: String,
        reason: String,
        ttl: Duration? = null,
    ): Either<BlacklistError, Unit>

    /**
     * Removes a token from the blacklist.
     */
    public suspend fun remove(token: String): Either<BlacklistError, Unit>

    /**
     * Purges expired entries and returns how many were removed.
     */
    public suspend fun purgeExpired(): Either<BlacklistError, Int>
}

/**
 * Domain errors for blacklist write operations.
 */
public sealed interface BlacklistError {
    /**
     * The token already exists in the blacklist.
     */
    public data class AlreadyBlacklisted(
        public val token: String,
    ) : BlacklistError

    /**
     * The token was not found in the blacklist.
     */
    public data class NotFound(
        public val token: String,
    ) : BlacklistError

    /**
     * The underlying blacklist store is unavailable.
     */
    public data class Unavailable(
        public val cause: Throwable? = null,
    ) : BlacklistError
}
