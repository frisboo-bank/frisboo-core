package com.frisboo.corebanking.observability.otel.models

import io.opentelemetry.api.trace.StatusCode

public enum class OtelSpanStatus {
    OK,
    ERROR,
    ;

    public fun toOtelStatusCode(): StatusCode = when (this) {
        OK -> StatusCode.OK
        ERROR -> StatusCode.ERROR
    }
}
