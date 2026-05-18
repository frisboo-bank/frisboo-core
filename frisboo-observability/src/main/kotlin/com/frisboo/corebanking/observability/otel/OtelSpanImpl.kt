package com.frisboo.corebanking.observability.otel

import com.frisboo.corebanking.observability.otel.contracts.OtelSpan
import com.frisboo.corebanking.observability.otel.models.OtelSpanStatus
import io.opentelemetry.api.trace.Span

internal class OtelSpanImpl(
    private val span: Span,
) : OtelSpan {
    override fun setAttribute(key: String, value: String): Span = span.setAttribute(key, value)
    override fun recordException(throwable: Throwable): Span = span.recordException(throwable)
    override fun recordError(message: String): Span = span.recordException(RuntimeException(message))
    override fun setStatus(status: OtelSpanStatus): Span = span.setStatus(status.toOtelStatusCode())
}
