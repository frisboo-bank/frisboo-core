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
package com.frisboo.corebanking.data.idempotency.contracts

import arrow.core.Either

/**
 * Write-only idempotency contract.
 */
public interface IdempotencyMarker {
    /**
     * Marks a key as currently being processed.
     *
     * @param key idempotency key.
     * @return operation result.
     */
    public suspend fun markInProgress(key: String): Either<IdempotencyError, Unit>

    /**
     * Marks a key as completed and optionally stores a serialized result.
     *
     * @param key idempotency key.
     * @param result optional serialized result payload.
     * @return operation result.
     */
    public suspend fun markComplete(
        key: String,
        result: ByteArray? = null,
    ): Either<IdempotencyError, Unit>

    /**
     * Removes a key from the idempotency store.
     *
     * @param key idempotency key.
     * @return operation result.
     */
    public suspend fun remove(key: String): Either<IdempotencyError, Unit>
}
