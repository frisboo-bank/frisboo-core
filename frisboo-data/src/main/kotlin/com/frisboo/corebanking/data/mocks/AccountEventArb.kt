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
package com.frisboo.corebanking.data.mocks

import com.frisboo.corebanking.data.mocks.events.MockAccountActivatedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountActivatedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountBalanceUpdatedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountBalanceUpdatedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountClosedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountClosedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountCreatedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountCreatedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountDeletedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountDeletedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountFrozenEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountFrozenPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountOverdraftLimitUpdatedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountOverdraftLimitUpdatedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountOwnerChangedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountOwnerChangedPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountUnfrozenEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountUnfrozenPayload
import com.frisboo.corebanking.data.mocks.events.MockAccountUpdatedEvent
import com.frisboo.corebanking.data.mocks.events.MockAccountUpdatedPayload
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.uuid

public val AccountClosedReasonArb: Arb<String> =
    Arb.element(
        listOf(
            "Customer Request",
            "Fraudulent Activity",
            "Account Inactivity",
            "Regulatory Compliance",
            "Other",
        ),
    )
public val AccountFrozenReasonArb: Arb<String> =
    Arb.element(
        listOf(
            "Suspicious Activity",
            "Legal Hold",
            "Overdraft Protection",
            "Regulatory Requirement",
            "Other",
        ),
    )
public val AccountUnfrozenReasonArb: Arb<String> =
    Arb.element(
        listOf(
            "Issue Resolved",
            "Customer Request",
            "Regulatory Clearance",
            "Account Review Completed",
            "Other",
        ),
    )
public val AccountDeletedReasonArb: Arb<String> =
    Arb.element(
        listOf(
            "Customer Request",
            "Duplicate Account",
            "Fraudulent Activity",
            "Regulatory Compliance",
            "Other",
        ),
    )
public val AccountOwnerChangeReasonArb: Arb<String> =
    Arb.element(
        listOf(
            "Customer Request",
            "Account Merger",
            "Regulatory Requirement",
            "Fraud Prevention",
            "Other",
        ),
    )

public val AccountEventArb: Arb<MockAccountEvent<out Any>> =
    arbitrary {
        val accountClosedReason = AccountClosedReasonArb.bind()
        val accountClosureReason = AccountClosedReasonArb.bind()
        val accountDeletedReason = AccountDeletedReasonArb.bind()
        val accountFrozenReason = AccountFrozenReasonArb.bind()
        val accountOwnerChangeReason = AccountOwnerChangeReasonArb.bind()
        val accountUnfrozenReason = AccountUnfrozenReasonArb.bind()

        val (createdAt, updatedAt, processedAt) = timestampArb().bind()
        val (oldLimit, newLimit) = accountOverdraftLimitsArb().bind()
        val account = accountArb().bind()
        val correlationId = Arb.uuid().bind()
        val customer = customerArb().bind()
        val eventId = Arb.uuid().bind()
        val newBalance = moneyAmountArb().bind()
        val newCustomer = customerArb().bind()
        val oldBalance = moneyAmountArb().bind()

        Arb
            .of(
                listOf(
                    MockAccountCreatedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description =
                            "New ${account.accountType} ${account.currency.code} " +
                                "account ${account.accountNumber} created for customer ${customer.customerId}",
                        data =
                            MockAccountCreatedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                accountType = account.accountType,
                                accountNumber = account.accountNumber,
                                accountCurrency = account.currency,
                                createdAt,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountUpdatedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} updated",
                        data =
                            MockAccountUpdatedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                updatedFields =
                                    mapOf(
                                        "account.accountType" to account.accountType.name,
                                        "accountCurrency" to account.currency.code,
                                    ),
                                updatedAt,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountClosedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} closed. Reason: $accountClosedReason",
                        data =
                            MockAccountClosedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                closedAt = updatedAt,
                                reason = accountClosureReason,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountDeletedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} deleted. Reason: $accountDeletedReason",
                        data =
                            MockAccountDeletedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                deletedAt = updatedAt,
                                reason = accountDeletedReason,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountFrozenEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} frozen. Reason: $accountFrozenReason",
                        data =
                            MockAccountFrozenPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                frozenAt = updatedAt,
                                reason = accountFrozenReason,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountUnfrozenEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} unfrozen. Reason: $accountUnfrozenReason",
                        data =
                            MockAccountUnfrozenPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                unfrozenAt = updatedAt,
                                reason = accountUnfrozenReason,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountActivatedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} activated",
                        data =
                            MockAccountActivatedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                activatedAt = updatedAt,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountBalanceUpdatedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} balance updated",
                        data =
                            MockAccountBalanceUpdatedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                oldBalance,
                                newBalance,
                                updatedAt,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountOwnerChangedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description =
                            "Account ${account.accountNumber} owner " +
                                "changed from customer: ${customer.customerId} to new customer: ${newCustomer.customerId}. Reason: $accountOwnerChangeReason",
                        data =
                            MockAccountOwnerChangedPayload(
                                accountId = account.accountId,
                                oldCustomerId = customer.customerId,
                                newCustomerId = newCustomer.customerId,
                                changedAt = updatedAt,
                                reason = accountOwnerChangeReason,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                    MockAccountOverdraftLimitUpdatedEvent(
                        eventId,
                        aggregateId = account.accountId,
                        description = "Account ${account.accountNumber} overdraft limit updated",
                        data =
                            MockAccountOverdraftLimitUpdatedPayload(
                                accountId = account.accountId,
                                customerId = customer.customerId,
                                oldLimit,
                                newLimit,
                                updatedAt,
                            ),
                        metaData =
                            mapOf(
                                "correlationId" to correlationId.toString(),
                            ),
                        processedAt,
                        createdAt,
                        updatedAt,
                    ),
                ),
            ).bind()
    }
