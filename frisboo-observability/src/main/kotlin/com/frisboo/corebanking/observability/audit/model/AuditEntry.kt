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
package com.frisboo.corebanking.observability.audit.model

import java.util.UUID
import kotlin.time.Instant

/**
 * Immutable record of a security-relevant or compliance-relevant action.
 *
 * Banking regulations (PCI-DSS 10.x, SOC 2 CC6/CC7) require tamper-evident audit trails
 * with actor identity, action, target resource, outcome, and timestamps.
 * This model captures those fields in a single, serializable envelope.
 */
public data class AuditEntry(
    val auditId: UUID,
    val principalId: String,
    val principalType: String,
    val action: String,
    val resourceType: String,
    val resourceId: String?,
    val outcome: AuditOutcome,
    val correlationId: String?,
    val traceId: String?,
    val clientIp: String?,
    val details: Map<String, String>,
    val occurredAt: Instant,
)

public enum class AuditOutcome {
    SUCCESS,
    FAILURE,
    DENIED,
}
