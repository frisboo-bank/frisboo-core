package com.frisboo.corebanking.observability.autoconfigure

import com.frisboo.corebanking.observability.audit.ObservabilityAuditImpl
import com.frisboo.corebanking.observability.audit.adapters.kafka.ObservabilityAuditKafkaAdapter
import com.frisboo.corebanking.observability.audit.adapters.noop.ObservabilityAuditNoOpAdapter
import com.frisboo.corebanking.observability.audit.adapters.otel.ObservabilityAuditOtelAdapter
import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAudit
import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAuditBaggageFilter
import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditType
import com.frisboo.corebanking.observability.metrics.ObservabilityMetricsImpl
import com.frisboo.corebanking.observability.metrics.adapters.noop.ObservabilityMetricsNoOpAdapter
import com.frisboo.corebanking.observability.metrics.adapters.otel.ObservabilityMetricsOtelAdapter
import com.frisboo.corebanking.observability.metrics.contracts.ObservabilityMetrics
import com.frisboo.corebanking.observability.metrics.models.ObservabilityMetricsType
import com.frisboo.corebanking.observability.otel.OtelSpanEmitterImpl
import com.frisboo.corebanking.observability.otel.contracts.OtelSpanEmitter
import com.frisboo.corebanking.observability.tracer.ObservabilityTracerImpl
import com.frisboo.corebanking.observability.tracer.adapters.noop.ObservabilityTracerNoOpAdapter
import com.frisboo.corebanking.observability.tracer.adapters.otel.ObservabilityTracerOtelAdapter
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracer
import com.frisboo.corebanking.observability.tracer.models.ObservabilityTracerType
import io.micrometer.core.instrument.MeterRegistry
import io.opentelemetry.api.OpenTelemetry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.util.Optional

@AutoConfiguration
@ConditionalOnProperty(prefix = "frisboo.corebanking.observability", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(ObservabilityProperties::class)
public open class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnBean(OpenTelemetry::class)
    public fun otelSpanEmitter(otel: OpenTelemetry): OtelSpanEmitter = OtelSpanEmitterImpl(
        otel = otel,
        tracerName = "com.frisboo.corebanking",
    )

    @Bean
    public fun tracer(
        properties: ObservabilityProperties,
        spanEmitter: Optional<OtelSpanEmitter>,
    ): ObservabilityTracer {
        val delegate = when (properties.tracer.type) {
            ObservabilityTracerType.NOOP -> ObservabilityTracerNoOpAdapter()
            ObservabilityTracerType.OTEL -> {
                val emitter =
                    spanEmitter.orElseThrow {
                        IllegalStateException("OtelSpanEmitter bean is required for OTEL tracer adapter")
                    }
                ObservabilityTracerOtelAdapter(emitter)
            }
        }
        return ObservabilityTracerImpl(delegate)
    }

    @Bean
    @ConditionalOnMissingBean
    public fun auditBaggageFilter(): ObservabilityAuditBaggageFilter = ObservabilityAuditBaggageFilter { it }

    @Bean
    public fun audit(
        properties: ObservabilityProperties,
        baggageFilter: ObservabilityAuditBaggageFilter,
        spanEmitter: Optional<OtelSpanEmitter>,
    ): ObservabilityAudit {
        val delegate = when (properties.audit.type) {
            ObservabilityAuditType.KAFKA -> ObservabilityAuditKafkaAdapter()
            ObservabilityAuditType.NOOP -> ObservabilityAuditNoOpAdapter()
            ObservabilityAuditType.OTEL -> {
                val emitter =
                    spanEmitter.orElseThrow {
                        IllegalStateException("OtelSpanEmitter bean is required for OTEL audit adapter")
                    }
                ObservabilityAuditOtelAdapter(emitter)
            }
        }
        return ObservabilityAuditImpl(delegate, baggageFilter)
    }

    @Bean
    public fun metrics(
        properties: ObservabilityProperties,
        meterRegistry: Optional<MeterRegistry>,
    ): ObservabilityMetrics {
        val delegate = when (properties.metrics.type) {
            ObservabilityMetricsType.NOOP -> ObservabilityMetricsNoOpAdapter()
            ObservabilityMetricsType.OTEL -> {
                val registry =
                    meterRegistry.orElseThrow {
                        IllegalStateException("MeterRegistry bean is required for OTEL metrics adapter")
                    }
                ObservabilityMetricsOtelAdapter(meterRegistry = registry)
            }
        }
        return ObservabilityMetricsImpl(delegate)
    }
}
