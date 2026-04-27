package com.frisboo.corebanking.observability.otel

import com.frisboo.corebanking.observability.otel.contracts.OtelSpan
import com.frisboo.corebanking.observability.otel.contracts.OtelSpanEmitter
import io.opentelemetry.api.OpenTelemetry

public class OtelSpanEmitterImpl(otel: OpenTelemetry, tracerName: String) : OtelSpanEmitter {
    private val tracer = otel.getTracer(tracerName)

    override fun <T> withSpan(name: String, attributes: Map<String, String>, block: (OtelSpan) -> T): T {
        val span = tracer.spanBuilder(name).startSpan()

        attributes.forEach { (key, value) -> span.setAttribute(key, value) }

        try {
            return span.makeCurrent().use { _ ->
                block(OtelSpanImpl(span))
            }
        } finally {
            span.end()
        }
    }
}
