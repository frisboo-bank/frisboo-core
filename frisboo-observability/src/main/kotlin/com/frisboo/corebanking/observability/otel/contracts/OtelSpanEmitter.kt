package com.frisboo.corebanking.observability.otel.contracts

public interface OtelSpanEmitter {
    public fun <T> withSpan(
        name: String,
        attributes: Map<String, String>,
        block: (OtelSpan) -> T,
    ): T
}
