package com.frisboo.corebanking.observability.metrics.adapters.noop

import com.frisboo.corebanking.observability.metrics.contracts.ObservabilityMetrics
import com.frisboo.corebanking.observability.metrics.contracts.ObservabilityMetricsAdapter
import kotlin.time.Duration

public class ObservabilityMetricsNoOpAdapter : ObservabilityMetricsAdapter {
    override fun incrementCounter(name: String, tags: Map<String, String>) {
        //
    }

    override fun recordGauge(
        name: String,
        value: Double,
        tags: Map<String, String>,
    ) {
        //
    }

    override fun recordDuration(
        name: String,
        duration: Duration,
        tags: Map<String, String>,
    ) {
        //
    }
}
