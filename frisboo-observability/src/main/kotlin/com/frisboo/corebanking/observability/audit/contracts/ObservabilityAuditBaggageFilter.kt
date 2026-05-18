package com.frisboo.corebanking.observability.audit.contracts

import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditBaggage

public fun interface ObservabilityAuditBaggageFilter {
    public fun filter(baggage: ObservabilityAuditBaggage): ObservabilityAuditBaggage
}
