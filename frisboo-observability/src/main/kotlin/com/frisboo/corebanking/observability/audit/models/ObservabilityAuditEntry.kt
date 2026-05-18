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
package com.frisboo.corebanking.observability.audit.models

import com.frisboo.corebanking.core.context.RequestContext
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public data class ObservabilityAuditEntryProps(
    val action: String,
    val baggage: ObservabilityAuditBaggage = ObservabilityAuditBaggage(emptyMap()),
    val occurredAt: Instant = Clock.System.now(),
    val outcome: ObservabilityAuditOutcome,
    val principalId: String = "system",
    val principalType: String = "service",
    val requestContext: RequestContext? = null,
    val resourceId: String? = null,
    val resourceType: String = "unknown",
    val spanId: String? = null,
    val traceId: String? = null,
)

@OptIn(ExperimentalUuidApi::class)
public data class ObservabilityAuditEntry(
    val action: String,
    val auditId: Uuid,
    val baggage: ObservabilityAuditBaggage,
    val occurredAt: Instant,
    val outcome: ObservabilityAuditOutcome,
    val principalId: String,
    val principalType: String,
    val requestContext: RequestContext?,
    val resourceId: String?,
    val resourceType: String,
    val spanId: String?,
    val traceId: String?,
) {
    public companion object {
        public fun create(
            props: ObservabilityAuditEntryProps,
        ): ObservabilityAuditEntry = ObservabilityAuditEntry(
            action = props.action,
            auditId = Uuid.random(),
            baggage = props.baggage,
            occurredAt = props.occurredAt,
            outcome = props.outcome,
            principalId = props.principalId,
            principalType = props.principalType,
            requestContext = props.requestContext,
            resourceId = props.resourceId,
            resourceType = props.resourceType,
            spanId = props.spanId,
            traceId = props.traceId,
        )
    }
}

