package com.frisboo.corebanking.observability.metrics

import com.frisboo.corebanking.observability.metrics.contracts.ObservabilityMetrics
import com.frisboo.corebanking.observability.metrics.contracts.ObservabilityMetricsAdapter
import kotlin.time.Duration

public class ObservabilityMetricsImpl(
    private val delegate: ObservabilityMetricsAdapter,
) : ObservabilityMetrics {
    override fun incrementCounter(name: String, tags: Map<String, String>): Unit = delegate.incrementCounter(name, tags)

    override fun recordGauge(
        name: String,
        value: Double,
        tags: Map<String, String>,
    ): Unit = delegate.recordGauge(name, value, tags)

    override fun recordDuration(
        name: String,
        duration: Duration,
        tags: Map<String, String>,
    ): Unit = delegate.recordDuration(name, duration, tags)
}
