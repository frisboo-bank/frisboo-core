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
 * Backward-compatible generic deduplication contract.
 *
 * @param K generic key type used by legacy integrations.
 */
public interface DeduplicationStore<K> {
    /**
     * Checks whether the given key has been seen before.
     *
     * @param key the deduplication key.
     * @return `true` if the key was previously marked as seen.
     */
    public suspend fun isDuplicate(key: K): Boolean

    /**
     * Marks the given key as seen, with an optional time-to-live.
     *
     * @param key the deduplication key.
     * @param ttl optional duration after which the key record expires. `null` means no expiry.
     * @return [Either.Right] `true` if first seen, `false` if already present.
     */
    public suspend fun markSeen(
        key: K,
        ttl: Duration? = null,
    ): Either<DeduplicationError, Boolean>
}

/** Errors that can occur during deduplication operations. */
public sealed interface DeduplicationError {
    public data class StoreUnavailable(
        val cause: Throwable? = null,
    ) : DeduplicationError
}
