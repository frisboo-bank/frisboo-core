package com.frisboo.corebanking.observability.otel.contracts

import com.frisboo.corebanking.observability.otel.models.OtelSpanStatus
import io.opentelemetry.api.trace.Span

public interface OtelSpan {
    public fun setAttribute(key: String, value: String): Span
    public fun recordException(throwable: Throwable): Span
    public fun recordError(message: String): Span
    public fun setStatus(status: OtelSpanStatus): Span
}
