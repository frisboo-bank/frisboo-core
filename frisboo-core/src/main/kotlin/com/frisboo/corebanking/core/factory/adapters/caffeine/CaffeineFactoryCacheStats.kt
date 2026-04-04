package com.frisboo.corebanking.core.factory.adapters.caffeine

import com.frisboo.corebanking.core.factory.models.FactoryCacheStats
import com.github.benmanes.caffeine.cache.stats.CacheStats

internal fun FactoryCacheStats.Companion.from(stats: CacheStats): FactoryCacheStats = FactoryCacheStats(
    hitCount = stats.hitCount(),
    missCount = stats.missCount(),
    loadSuccessCount = stats.loadSuccessCount(),
    loadFailureCount = stats.loadFailureCount(),
    totalLoadTime = stats.totalLoadTime(),
    evictionCount = stats.evictionCount(),
    evictionWeight = stats.evictionWeight(),
)
