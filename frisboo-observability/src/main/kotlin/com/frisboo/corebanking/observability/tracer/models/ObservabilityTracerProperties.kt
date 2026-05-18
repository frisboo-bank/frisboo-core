package com.frisboo.corebanking.observability.tracer.models

public data class ObservabilityTracerProperties(
    val type: ObservabilityTracerType = ObservabilityTracerType.NOOP,
)
