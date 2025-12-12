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

import com.frisboo.corebanking.data.mocks.events.MockSagaDraftCreatedEvent
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.uuid

public val SagaEventArb: Arb<MockSagaDraftCreatedEvent<Any>> =
    arbitrary {
        val (createdAt, updatedAt, processedAt) = timestampArb().bind()
        val aggregateId = Arb.uuid().bind()
        val event = AccountEventArb.bind()
        val eventId = Arb.uuid().bind()
        val version = versionArb.bind()

        Arb
            .of(
                listOf(
                    MockSagaDraftCreatedEvent(
                        eventId,
                        aggregateId,
                        description = "New saga draft created for aggregate $aggregateId",
                        data = event.data,
                        metaData = event.metaData,
                        processedAt,
                        createdAt,
                        updatedAt,
                        version,
                    ),
//                MockSagaPendingApprovalEvent(),
//                MockSagaApprovedEvent(),
//                MockSagaPendingExecutionEvent(),
//                MockSagaExecutingEvent(),
//                MockSagaCompletedEvent(),
//                MockSagaFailedEvent(),
//                MockSagaTimedOutEvent(),
//                MockSagaAbortedEvent(),
//                MockSagaOnHoldEvent(),
//                MockSagaRetryingEvent(),
//                MockSagaCompensatingEvent(),
//                MockSagaCompensatedEvent(),
//                MockSagaCompensationFailedEvent(),
                ),
            ).bind()
    }

//    public fun sagaEvent(): Arb<MockSagaEvent<MockSagaPayload>> {
//        Arb.of(
//        )
//    }
