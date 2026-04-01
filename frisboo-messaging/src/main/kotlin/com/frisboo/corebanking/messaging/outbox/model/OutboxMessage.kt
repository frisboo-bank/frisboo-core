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
package com.frisboo.corebanking.messaging.outbox.model

import kotlin.time.Instant

/**
 * Status of an outbox message in the dispatch lifecycle.
 */
public enum class OutboxMessageStatus {
    PENDING,
    DISPATCHED,
    FAILED,
}

/**
 * An outbox message awaiting dispatch.
 *
 * @property id unique message identifier.
 * @property aggregateId the aggregate that produced this message (for ordering/partitioning).
 * @property type the message type discriminator (e.g., "payment.completed").
 * @property payload serialized message payload.
 * @property status current dispatch status.
 * @property createdAt timestamp when the message was created.
 */
public data class OutboxMessage(
    val id: String,
    val aggregateId: String,
    val type: String,
    val payload: ByteArray,
    val status: OutboxMessageStatus,
    val createdAt: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OutboxMessage) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
