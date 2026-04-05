package com.frisboo.corebanking.core.factory.adapters.noop

import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import com.frisboo.corebanking.core.factory.models.FactoryCacheStats

internal class NoOpFactoryCache<K : Any, V : Any> : FactoryCache<K, V> {

    override suspend fun get(key: K, mappingFunction: suspend (K) -> V): V =
        mappingFunction(key)

    override suspend fun getIfPresent(key: K): V? = null

    override suspend fun put(key: K, value: V): Unit = Unit

    override suspend fun invalidate(key: K): Unit = Unit

    override suspend fun estimatedSize(): Long = 0

    override suspend fun stats(): FactoryCacheStats? = null

    override fun setEvictionListener(listener: (suspend (K?, V?) -> Unit)?): Unit = Unit
}
