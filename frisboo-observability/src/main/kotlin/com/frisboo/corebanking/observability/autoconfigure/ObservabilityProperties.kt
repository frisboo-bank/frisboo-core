package com.frisboo.corebanking.observability.autoconfigure

import com.frisboo.corebanking.observability.audit.models.ObservabilityAuditProperties
import com.frisboo.corebanking.observability.metrics.models.ObservabilityMetricsProperties
import com.frisboo.corebanking.observability.tracer.models.ObservabilityTracerProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "frisboo.corebanking.observability")
public data class ObservabilityProperties(
    val enabled: Boolean = false,
    val audit: ObservabilityAuditProperties = ObservabilityAuditProperties(),
    val tracer: ObservabilityTracerProperties = ObservabilityTracerProperties(),
    val metrics: ObservabilityMetricsProperties = ObservabilityMetricsProperties(),
)
