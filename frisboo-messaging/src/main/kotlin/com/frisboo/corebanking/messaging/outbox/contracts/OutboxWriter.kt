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
package com.frisboo.corebanking.messaging.outbox.contracts

import arrow.core.Either
import com.frisboo.corebanking.messaging.outbox.model.OutboxMessage

/**
 * Write-only contract for mutating outbox state.
 */
public interface OutboxWriter {
    /**
     * Saves an outbox [message].
     */
    public suspend fun save(message: OutboxMessage): Either<OutboxError, Unit>

    /**
     * Marks the outbox message identified by [messageId] as dispatched.
     */
    public suspend fun markDispatched(messageId: String): Either<OutboxError, Unit>

    /**
     * Marks the outbox message identified by [messageId] as failed.
     */
    public suspend fun markFailed(
        messageId: String,
        cause: Throwable? = null,
    ): Either<OutboxError, Unit>

    /**
     * Moves the outbox message identified by [messageId] to DLQ with [reason].
     */
    public suspend fun moveToDlq(
        messageId: String,
        reason: String,
    ): Either<OutboxError, Unit>
}
