package com.frisboo.corebanking.observability.tracer.adapters.noop

import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracerOperation

public object ObservabilityTracerNoopOperation : ObservabilityTracerOperation {
    override fun setAttribute(key: String, value: String) {
        //
    }

    override fun recordException(throwable: Throwable) {
        //
    }

    override fun recordError(message: String) {
        //
    }
}
