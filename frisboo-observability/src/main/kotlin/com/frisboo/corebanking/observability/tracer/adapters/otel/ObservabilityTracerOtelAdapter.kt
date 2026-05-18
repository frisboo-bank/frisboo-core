package com.frisboo.corebanking.observability.tracer.adapters.otel

import com.frisboo.corebanking.observability.otel.contracts.OtelSpanEmitter
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerAdapter
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerOperation

public class ObservabilityTracerOtelAdapter(
    private val spanEmitter: OtelSpanEmitter,
) : ObservabilityTracerAdapter {
    override fun <T> withOperation(
        name: String,
        attributes: Map<String, String>,
        block: (ObservabilityTracerOperation) -> T,
    ): T {
        return spanEmitter.withSpan(name, attributes) { otelSpan ->
            block((otelSpan.toTracerOperation()))
        }
    }

}
