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
import com.frisboo.corebanking.messaging.outbox.model.OutboxStatus

/**
 * Read-only contract for querying outbox state.
 */
public interface OutboxReader {
    /**
     * Finds pending outbox messages up to the provided [limit].
     */
    public suspend fun findPending(limit: Int): Either<OutboxError, List<OutboxMessage>>

    /**
     * Finds an outbox message by its [messageId].
     */
    public suspend fun findById(messageId: String): Either<OutboxError, OutboxMessage?>

    /**
     * Counts outbox messages by [status].
     */
    public suspend fun countByStatus(status: OutboxStatus): Either<OutboxError, Long>
}
