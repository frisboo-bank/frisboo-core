package com.frisboo.corebanking.observability.tracer.adapters.otel

import com.frisboo.corebanking.observability.otel.contracts.OtelSpan
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerOperation

internal fun OtelSpan.toTracerOperation(): ObservabilityTracerOperation {
    val span = this

    return object : ObservabilityTracerOperation {
        override fun setAttribute(key: String, value: String) {
            span.setAttribute(key, value)
        }

        override fun recordException(throwable: Throwable) {
            span.recordException(throwable)
        }

        override fun recordError(message: String) {
            span.recordError(message)
        }
    }
}
