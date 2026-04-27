package com.frisboo.corebanking.observability.audit.models

public data class ObservabilityAuditProperties(
    val type: ObservabilityAuditType = ObservabilityAuditType.NOOP,
)
