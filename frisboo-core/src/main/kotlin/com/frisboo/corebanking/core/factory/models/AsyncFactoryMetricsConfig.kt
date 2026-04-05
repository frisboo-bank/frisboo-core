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
package com.frisboo.corebanking.core.factory.models

public data class AsyncFactoryMetricsConfig(
    val enabled: Boolean = true,
    val metricPrefix: String = "async",
) {
    init {
        require(metricPrefix.matches(METRIC_PREFIX_PATTERN)) {
            "metricPrefix must match pattern '$METRIC_PREFIX_PATTERN', got '$metricPrefix'"
        }
    }

    public companion object {
        public val METRIC_PREFIX_PATTERN: Regex = Regex("^[a-zA-Z][a-zA-Z0-9._-]*$")
    }
}
