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

import com.frisboo.corebanking.core.domain.valueobjects.AccountType
import com.frisboo.corebanking.core.domain.valueobjects.Currency
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Instant

public interface MockDomainEvent<Data> {
    public val eventId: UUID
    public val eventType: String
    public val aggregateId: UUID
    public val aggregateType: String
    public val topic: String
    public val description: String
    public val data: Data
    public val metaData: Map<String, String>
    public val processedAt: Instant?
    public val createdAt: Instant
    public val updatedAt: Instant
    public val version: Long
}

public sealed class MockAccountEvent<Data> : MockDomainEvent<Data> {
    override val topic: String = "account-events"
    override val aggregateType: String = "AccountEvent"
}

public data class MockAccountCreatedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val accountType: AccountType,
    val accountNumber: String,
    val accountCurrency: Currency,
    val createdAt: Instant,
)

public data class MockAccountCreatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountCreatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountCreatedPayload>() {
    override val eventType: String = "AccountCreated"
}

public data class MockAccountUpdatedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val updatedFields: Map<String, String>,
    val updatedAt: Instant,
)

public data class MockAccountUpdatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountUpdatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountUpdatedPayload>() {
    override val eventType: String = "AccountUpdated"
}

public data class MockAccountClosedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val closedAt: Instant,
    val reason: String?,
)

public data class MockAccountClosedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountClosedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountClosedPayload>() {
    override val eventType: String = "AccountClosed"
}

public data class MockAccountDeletedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val deletedAt: Instant,
    val reason: String?,
)

public data class MockAccountDeletedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountDeletedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountDeletedPayload>() {
    override val eventType: String = "AccountDeleted"
}

public data class MockAccountFrozenPayload(
    val accountId: UUID,
    val customerId: UUID,
    val frozenAt: Instant,
    val reason: String?,
)

public data class MockAccountFrozenEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountFrozenPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountFrozenPayload>() {
    override val eventType: String = "AccountFrozen"
}

public data class MockAccountUnfrozenPayload(
    val accountId: UUID,
    val customerId: UUID,
    val unfrozenAt: Instant,
    val reason: String?,
)

public data class MockAccountUnfrozenEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountUnfrozenPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountUnfrozenPayload>() {
    override val eventType: String = "AccountUnfrozen"
}

public data class MockAccountActivatedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val activatedAt: Instant,
)

public data class MockAccountActivatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountActivatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountActivatedPayload>() {
    override val eventType: String = "AccountActivated"
}

public data class MockAccountBalanceUpdatedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val oldBalance: BigDecimal,
    val newBalance: BigDecimal,
    val updatedAt: Instant,
)

public data class MockAccountBalanceUpdatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountBalanceUpdatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountBalanceUpdatedPayload>() {
    override val eventType: String = "AccountBalanceUpdated"
}

public data class MockAccountOwnerChangedPayload(
    val accountId: UUID,
    val oldCustomerId: UUID,
    val newCustomerId: UUID,
    val changedAt: Instant,
    val reason: String?,
)

public data class MockAccountOwnerChangedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountOwnerChangedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountOwnerChangedPayload>() {
    override val eventType: String = "AccountOwnerChanged"
}

public data class MockAccountOverdraftLimitUpdatedPayload(
    val accountId: UUID,
    val customerId: UUID,
    val oldLimit: BigDecimal,
    val newLimit: BigDecimal,
    val updatedAt: Instant,
)

public data class MockAccountOverdraftLimitUpdatedEvent(
    override val eventId: UUID,
    override val aggregateId: UUID,
    override val description: String,
    override val data: MockAccountOverdraftLimitUpdatedPayload,
    override val metaData: Map<String, String>,
    override val processedAt: Instant?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val version: Long = 1,
) : MockAccountEvent<MockAccountOverdraftLimitUpdatedPayload>() {
    override val eventType: String = "AccountOverdraftLimitUpdated"
}
