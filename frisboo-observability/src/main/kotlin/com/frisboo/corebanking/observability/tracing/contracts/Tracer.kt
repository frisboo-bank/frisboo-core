/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.observability.tracing.contracts

/**
 * Contract for distributed tracing.
 *
 * Abstracts the underlying tracing implementation (OpenTelemetry, Micrometer Observation, etc.)
 * so service code depends only on this interface. Implementations handle span lifecycle,
 * context propagation across gRPC/HTTP boundaries, and export to the configured backend.
 */
public interface Tracer {
    /**
     * Starts a new span as a child of the current context.
     *
     * @param name the span name, typically the operation being traced.
     * @param attributes initial key-value attributes to attach to the span.
     * @return a [Span] handle for recording events and completing the span.
     */
    public fun startSpan(
        name: String,
        attributes: Map<String, String> = emptyMap(),
    ): Span

    /**
     * Executes [block] within a new span, automatically closing the span on completion or failure.
     *
     * @param name the span name.
     * @param attributes initial key-value attributes.
     * @param block the suspend function to execute within the span.
     * @return the result of [block].
     */
    public suspend fun <T> withSpan(
        name: String,
        attributes: Map<String, String> = emptyMap(),
        block: suspend () -> T,
    ): T

    /**
     * Returns the current trace context, or `null` if no trace is active.
     */
    public fun currentContext(): TraceContext?

    /**
     * Injects the current trace context into a mutable carrier map
     * for cross-process propagation (e.g., gRPC metadata, HTTP headers).
     *
     * @param carrier the mutable map to inject trace headers into.
     */
    public fun inject(carrier: MutableMap<String, String>)

    /**
     * Extracts a trace context from an incoming carrier map
     * and makes it the current context.
     *
     * @param carrier the map containing trace headers (e.g., W3C traceparent).
     * @return the extracted [TraceContext], or `null` if no valid context was found.
     */
    public fun extract(carrier: Map<String, String>): TraceContext?
}

/**
 * A handle to an active span. Must be closed when the operation completes.
 */
public interface Span : AutoCloseable {
    public fun setAttribute(
        key: String,
        value: String,
    )

    public fun recordException(throwable: Throwable)

    public fun setStatus(status: SpanStatus)
}

public enum class SpanStatus {
    OK,
    ERROR,
}

/**
 * Opaque trace context carrying trace and span identifiers.
 */
public interface TraceContext {
    public val traceId: String
    public val spanId: String
}
