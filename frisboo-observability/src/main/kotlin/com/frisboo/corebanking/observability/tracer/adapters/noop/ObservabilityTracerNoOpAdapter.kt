package com.frisboo.corebanking.observability.tracer.adapters.noop

import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerAdapter
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerOperation

public class ObservabilityTracerNoOpAdapter : ObservabilityTracerAdapter {
    override fun <T> withOperation(
        name: String,
        attributes: Map<String, String>,
        block: (ObservabilityTracerOperation) -> T,
    ): T = block(ObservabilityTracerNoopOperation)
}
