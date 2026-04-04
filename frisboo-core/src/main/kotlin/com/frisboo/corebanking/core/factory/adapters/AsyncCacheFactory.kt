package com.frisboo.corebanking.core.factory.adapters

import com.frisboo.corebanking.core.factory.adapters.caffeine.CaffeineFactoryCache
import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import kotlin.time.Duration

internal object AsyncCacheFactory {
    fun <K : Any, V : Any> createCaffeine(
        maxSize: Long,
        expireAfterAccess: Duration,
        recordStats: Boolean = true,
    ): FactoryCache<K, V> = CaffeineFactoryCache(maxSize, expireAfterAccess, recordStats)
}
