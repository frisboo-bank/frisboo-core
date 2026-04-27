package com.frisboo.corebanking.observability.audit

import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAudit
import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAuditAdapter
import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAuditBaggageFilter
import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditEntry
import kotlin.uuid.ExperimentalUuidApi

public class ObservabilityAuditImpl(
    public val delegate: ObservabilityAuditAdapter,
    public val baggageFilter: ObservabilityAuditBaggageFilter,
) : ObservabilityAudit {
    @OptIn(ExperimentalUuidApi::class)
    override fun record(entry: ObservabilityAuditEntry) {
        val filteredBaggage = baggageFilter.filter(entry.baggage)
        delegate.record(
            entry.copy(baggage = filteredBaggage),
        )
    }
}
