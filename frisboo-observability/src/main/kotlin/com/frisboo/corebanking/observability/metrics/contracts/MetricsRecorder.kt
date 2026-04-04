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
package com.frisboo.corebanking.observability.metrics.contracts

import kotlin.time.Duration

/**
 * Contract for recording application metrics.
 *
 * Abstracts the underlying metrics library (Micrometer, OpenTelemetry Metrics, etc.).
 * Implementations wire to the configured metrics backend (Prometheus, CloudWatch, etc.).
 */
public interface MetricsRecorder {
    public fun incrementCounter(
        name: String,
        tags: Map<String, String> = emptyMap(),
    )

    public fun recordGauge(
        name: String,
        value: Double,
        tags: Map<String, String> = emptyMap(),
    )

    public fun recordDuration(
        name: String,
        duration: Duration,
        tags: Map<String, String> = emptyMap(),
    )

    /**
     * Times the execution of [block] and records the duration under [name].
     *
     * @return the result of [block].
     */
    public suspend fun <T> recordTimed(
        name: String,
        tags: Map<String, String> = emptyMap(),
        block: suspend () -> T,
    ): T
}
