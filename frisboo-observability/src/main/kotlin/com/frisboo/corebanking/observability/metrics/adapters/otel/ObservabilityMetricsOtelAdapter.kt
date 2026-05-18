package com.frisboo.corebanking.observability.metrics.adapters.otel

import com.frisboo.corebanking.observability.metrics.contracts.ObservabilityMetricsAdapter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import kotlin.time.Duration
import kotlin.time.toJavaDuration

public class ObservabilityMetricsOtelAdapter(
    public val meterRegistry: MeterRegistry,
) : ObservabilityMetricsAdapter {
    override fun incrementCounter(name: String, tags: Map<String, String>) {
        meterRegistry.counter(name, Tags.of(tags.map { (k, v) -> Tag.of(k, v) })).increment()
    }

    override fun recordGauge(
        name: String,
        value: Double,
        tags: Map<String, String>,
    ) {
        meterRegistry.gauge(name, Tags.of(tags.map { (k, v) -> Tag.of(k, v) }), value)
    }

    override fun recordDuration(
        name: String,
        duration: Duration,
        tags: Map<String, String>,
    ) {
        meterRegistry.timer(name, Tags.of(tags.map { (k, v) -> Tag.of(k, v) })).record(duration.toJavaDuration())
    }
}
