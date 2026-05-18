package com.frisboo.corebanking.observability.audit.contracts

import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditEntry

public fun interface ObservabilityAuditShared {
    public fun record(entry: ObservabilityAuditEntry)
}
