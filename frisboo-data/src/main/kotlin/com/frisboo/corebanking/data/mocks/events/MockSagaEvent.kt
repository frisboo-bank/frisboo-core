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

public sealed class MockSagaEvent<Data> : MockDomainEvent<Data> {
    override val topic: String = "saga-events"
    override val aggregateType: String = "SagaEvent"
}

public data class MockSagaDraftCreatedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaDraftCreated"
}

public data class MockSagaPendingApprovalEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaPendingApproval"
}

public data class MockSagaApprovedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaApproved"
}

public data class MockSagaPendingExecutionEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaPendingExecution"
}

public data class MockSagaExecutingEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaExecuting"
}

public data class MockSagaCompletedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaCompleted"
}

public data class MockSagaFailedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaFailed"
}

public data class MockSagaTimedOutEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaTimedOut"
}

public data class MockSagaAbortedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaAborted"
}

public data class MockSagaOnHoldEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaOnHold"
}

public data class MockSagaRetryingEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaRetrying"
}

public data class MockSagaCompensatingEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaCompensating"
}

public data class MockSagaCompensatedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaCompensated"
}

public data class MockSagaCompensationFailedEvent<Data>(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: Data,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockSagaEvent<Data>() {
    override val eventType: String = "SagaCompensationFailed"
}
