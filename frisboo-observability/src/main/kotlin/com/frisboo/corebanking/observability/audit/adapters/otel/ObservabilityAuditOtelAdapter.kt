package com.frisboo.corebanking.observability.audit.adapters.otel

import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAuditAdapter
import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditEntry
import com.frisboo.corebanking.observability.otel.contracts.OtelSpanEmitter
import com.frisboo.corebanking.observability.otel.models.OtelSpanStatus
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal class ObservabilityAuditOtelAdapter(
    private val spanEmitter: OtelSpanEmitter,
) : ObservabilityAuditAdapter {
    override fun record(entry: ObservabilityAuditEntry) {
        val attributes = mutableMapOf(
            "audit.id" to entry.auditId.toString(),
            "principal.id" to entry.principalId,
            "principal.type" to entry.principalType,
            "action" to entry.action,
            "resource.type" to entry.resourceType,
            "outcome" to entry.outcome.name,
        )

        entry.resourceId?.let { attributes["resource.id"] = it }
        entry.spanId?.let { attributes["span.id"] = it }
        entry.traceId?.let { attributes["trace.id"] = it }

        val ctx = entry.requestContext
        ctx?.requestId?.value?.let { attributes["request.id"] = it.toString() }
        ctx?.correlationId?.value?.let { attributes["correlation.id"] = it.toString() }
        ctx?.sessionId?.value?.let { attributes["session.id"] = it.toString() }
        ctx?.customerGuid?.value?.let { attributes["customer.guid"] = it.toString() }
        ctx?.tenantId?.value?.let { attributes["tenant.id"] = it }
        ctx?.deviceFingerprint?.value?.let { attributes["device.fingerprint"] = it }
        ctx?.idempotencyKey?.value?.let { attributes["idempotency.key"] = it }
        ctx?.clientIp?.value?.let { attributes["client.ip"] = it }
        ctx?.userAgent?.value?.let { attributes["user.agent"] = it }

        spanEmitter.withSpan("audit.${entry.action}", attributes = attributes) { span ->
            entry.baggage.toMap().forEach { (k, v) -> span.setAttribute(k, v) }

            val status = entry.outcome.toOtelSpanStatus()
            span.setStatus(status)
            if (status == OtelSpanStatus.ERROR) {
                span.recordError("Audit outcome: ${entry.outcome.name}")
            }
        }
    }
}


