package com.frisboo.corebanking.observability.audit.adapters.otel

import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditOutcome
import com.frisboo.corebanking.observability.otel.models.OtelSpanStatus

internal fun ObservabilityAuditOutcome.toOtelSpanStatus(): OtelSpanStatus = when (this) {
    ObservabilityAuditOutcome.SUCCESS,
    ObservabilityAuditOutcome.DENIED,
        -> OtelSpanStatus.OK

    ObservabilityAuditOutcome.FAILURE,
    ObservabilityAuditOutcome.TIMEOUT,
        -> OtelSpanStatus.ERROR
}
