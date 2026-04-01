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

/**
 * Read contract for idempotent receiver state.
 */
public interface IdempotentReceiverReader {
    /**
     * Checks whether a message has been accepted for a receiver.
     *
     * @param receiverId receiver identifier.
     * @param messageId message identifier.
     * @return `true` when message is already accepted.
     */
    public suspend fun isAccepted(
        receiverId: String,
        messageId: String,
    ): Boolean

    /**
     * Counts accepted messages for a receiver.
     *
     * @param receiverId receiver identifier.
     * @return number of accepted messages.
     */
    public suspend fun acceptedCount(receiverId: String): Long
}
