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

import kotlin.time.Duration

public data class AsyncFactoryCachingConfig<C : Any, T : Any>(
    val enabled: Boolean = true,
    val maxSize: Long,
    val expireAfterAccess: Duration,
    val matchesConfig: (T, C) -> Boolean,
    val recordStats: Boolean = true,
) {
    init {
        require(maxSize in MIN_MAX_SIZE..MAX_MAX_SIZE) {
            "maxSize must be in $MIN_MAX_SIZE..$MAX_MAX_SIZE, got $maxSize"
        }
        require(expireAfterAccess.isPositive()) { "expireAfterAccess must be positive" }
    }

    public companion object {
        public const val MIN_MAX_SIZE: Long = 1
        public const val MAX_MAX_SIZE: Long = 10_000_000
    }
}
