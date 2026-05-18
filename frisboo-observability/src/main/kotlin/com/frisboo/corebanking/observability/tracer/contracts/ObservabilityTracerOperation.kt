package com.frisboo.corebanking.observability.tracer.contracts

public interface ObservabilityTracerOperation {
    public fun setAttribute(key: String, value: String)
    public fun recordException(throwable: Throwable)
    public fun recordError(message: String)
}
