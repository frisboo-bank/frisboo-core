package com.frisboo.corebanking.observability.audit.adapters.kafka

import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAuditAdapter
import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditEntry

internal class ObservabilityAuditKafkaAdapter : ObservabilityAuditAdapter {
    override fun record(entry: ObservabilityAuditEntry) {
        TODO("Not yet implemented")
    }
}
