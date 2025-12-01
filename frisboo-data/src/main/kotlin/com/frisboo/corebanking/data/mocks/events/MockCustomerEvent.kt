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

public sealed class MockCustomerEvent<Data> : MockDomainEvent<Data> {
    override val topic: String = "customer-events"
    override val aggregateType: String = "CustomerEvent"
}

public data class MockCustomerCreatedPayload(
    val customerId: String,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val createdAt: String,
)

public data class MockCustomerCreatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockCustomerCreatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockCustomerEvent<MockCustomerCreatedPayload>() {
    override val eventType: String = "CustomerCreated"
}

public data class MockCustomerUpdatedPayload(
    val customerId: String,
    val updatedFields: Map<String, Any>,
    val updatedAt: String,
)

public data class MockCustomerUpdatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockCustomerUpdatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockCustomerEvent<MockCustomerUpdatedPayload>() {
    override val eventType: String = "CustomerUpdated"
}

public data class MockCustomerDeletedPayload(
    val customerId: String,
    val deletedAt: String,
)

public data class MockCustomerDeletedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockCustomerDeletedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockCustomerEvent<MockCustomerDeletedPayload>() {
    override val eventType: String = "CustomerDeleted"
}

public data class MockCustomerLockedPayload(
    val customerId: String,
    val lockedAt: String,
)

public data class MockCustomerLockedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockCustomerLockedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockCustomerEvent<MockCustomerLockedPayload>() {
    override val eventType: String = "CustomerLocked"
}

public data class MockCustomerUnlockedPayload(
    val customerId: String,
    val unlockedAt: String,
)

public data class MockCustomerUnlockedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockCustomerUnlockedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long,
) : MockCustomerEvent<MockCustomerUnlockedPayload>() {
    override val eventType: String = "CustomerUnlocked"
}
