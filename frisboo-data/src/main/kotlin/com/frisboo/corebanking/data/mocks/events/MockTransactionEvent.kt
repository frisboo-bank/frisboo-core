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
package com.frisboo.corebanking.data.mocks.events

import java.util.UUID
import kotlin.time.Instant

public sealed class MockTransactionEvent<Data> : MockDomainEvent<Data> {
    override val topic: String = "transaction-events"
    override val aggregateType: String = "TransactionEvent"
}

public data class MockTransactionInitiatedPayload(
    val customerId: UUID,
    val transactionId: UUID,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val amount: Double,
    val currency: String,
    val initiatedAt: Instant,
)

public data class MockTransactionInitiatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockTransactionInitiatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<MockTransactionInitiatedPayload>() {
    override val eventType: String = "TransactionInitiated"
}

public data class MockTransactionApprovedPayload(
    val customerId: UUID,
    val transactionId: UUID,
    val approvedAt: Instant,
)

public data class MockTransactionApprovedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockTransactionApprovedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<MockTransactionApprovedPayload>() {
    override val eventType: String = "TransactionApproved"
}

public data class MockTransactionRejectedPayload(
    val customerId: UUID,
    val transactionId: UUID,
    val rejectedAt: Instant,
    val reason: String,
)

public data class MockTransactionRejectedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockTransactionRejectedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<MockTransactionRejectedPayload>() {
    override val eventType: String = "TransactionRejected"
}

public data class MockTransactionCompletedPayload(
    val customerId: UUID,
    val transactionId: UUID,
    val completedAt: Instant,
)

public data class MockTransactionCompletedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockTransactionCompletedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<MockTransactionCompletedPayload>() {
    override val eventType: String = "TransactionCompleted"
}
