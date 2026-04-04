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
package com.frisboo.corebanking.data.deduplication.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Write contract for message deduplication state.
 */
public interface MessageDedupWriter {
    /**
     * Marks a message key as seen.
     *
     * @param key message deduplication key.
     * @param ttl optional expiration duration.
     * @return [Either.Right] `true` if first time, `false` if duplicate.
     */
    public suspend fun markSeen(
        key: String,
        ttl: Duration? = null,
    ): Either<DeduplicationError, Boolean>

    /**
     * Purges expired deduplication records.
     *
     * @return [Either.Right] number of purged records.
     */
    public suspend fun purgeExpired(): Either<DeduplicationError, Int>
}
