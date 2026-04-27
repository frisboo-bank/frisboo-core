package com.frisboo.corebanking.observability.tracer

import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracer
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerAdapter
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerOperation

public class ObservabilityTracerImpl(
    public val delegate: ObservabilityTracerAdapter,
) : ObservabilityTracer {
    override fun <T> withOperation(
        name: String,
        attributes: Map<String, String>,
        block: (ObservabilityTracerOperation) -> T,
    ): T = delegate.withOperation(name, attributes, block)

}
