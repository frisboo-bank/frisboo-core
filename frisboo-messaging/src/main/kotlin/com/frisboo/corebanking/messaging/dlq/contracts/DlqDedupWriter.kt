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
package com.frisboo.corebanking.messaging.dlq.contracts

import arrow.core.Either

/**
 * Errors that can occur during DLQ operations.
 */
public sealed interface DlqError {
    public data class StoreUnavailable(
        public val cause: Throwable? = null,
    ) : DlqError

    public data class MessageNotFound(
        public val messageId: String,
    ) : DlqError
}

/**
 * Write-only contract for DLQ deduplication state.
 */
public interface DlqDedupWriter {
    /**
     * Records [messageId] in DLQ with a [reason].
     */
    public suspend fun record(
        messageId: String,
        reason: String,
    ): Either<DlqError, Unit>

    /**
     * Increments retry count for [messageId] and returns the new count.
     */
    public suspend fun incrementRetry(messageId: String): Either<DlqError, Int>

    /**
     * Marks [messageId] as resolved in deduplication state.
     */
    public suspend fun markResolved(messageId: String): Either<DlqError, Unit>
}
