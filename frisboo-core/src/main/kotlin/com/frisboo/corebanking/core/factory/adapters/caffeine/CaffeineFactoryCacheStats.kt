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
package com.frisboo.corebanking.core.factory.adapters.caffeine

import com.frisboo.corebanking.core.factory.models.FactoryCacheStats
import com.github.benmanes.caffeine.cache.stats.CacheStats

internal fun FactoryCacheStats.Companion.from(stats: CacheStats): FactoryCacheStats =
    FactoryCacheStats(
        hitCount = stats.hitCount(),
        missCount = stats.missCount(),
        loadSuccessCount = stats.loadSuccessCount(),
        loadFailureCount = stats.loadFailureCount(),
        totalLoadTime = stats.totalLoadTime(),
        evictionCount = stats.evictionCount(),
        evictionWeight = stats.evictionWeight(),
    )
