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
 * Write contract for idempotent receiver state.
 */
public interface IdempotentReceiverWriter {
    /**
     * Accepts a message for a receiver.
     *
     * @param receiverId receiver identifier.
     * @param messageId message identifier.
     * @return [Either.Right] `true` if accepted for the first time.
     */
    public suspend fun accept(
        receiverId: String,
        messageId: String,
    ): Either<IdempotencyError, Boolean>

    /**
     * Resets acceptance state for a receiver.
     *
     * @param receiverId receiver identifier.
     * @return operation result.
     */
    public suspend fun reset(receiverId: String): Either<IdempotencyError, Unit>
}
