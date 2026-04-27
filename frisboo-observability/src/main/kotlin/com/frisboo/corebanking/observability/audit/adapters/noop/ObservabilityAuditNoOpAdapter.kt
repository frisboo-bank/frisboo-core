package com.frisboo.corebanking.observability.audit.adapters.noop

import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAuditAdapter
import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditEntry

internal class ObservabilityAuditNoOpAdapter : ObservabilityAuditAdapter {
    override fun record(entry: ObservabilityAuditEntry) {
        //
    }
}
