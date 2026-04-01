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

import com.frisboo.corebanking.messaging.outbox.model.OutboxMessage

/**
 * Read-only contract for idempotent outbox state.
 */
public interface IdempotentOutboxReader {
    /**
     * Returns true when a message for [idempotencyKey] has already been sent.
     */
    public suspend fun isAlreadySent(idempotencyKey: String): Boolean

    /**
     * Retrieves the message associated with [idempotencyKey], if any.
     */
    public suspend fun getByIdempotencyKey(idempotencyKey: String): OutboxMessage?
}
