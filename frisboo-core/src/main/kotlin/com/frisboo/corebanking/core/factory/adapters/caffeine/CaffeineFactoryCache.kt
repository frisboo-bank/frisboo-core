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

import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import com.frisboo.corebanking.core.factory.models.FactoryCacheStats
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalListener
import dev.hsbrysk.caffeine.CoroutineCache
import dev.hsbrysk.caffeine.buildCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.toJavaDuration

internal class CaffeineFactoryCache<K : Any, V : Any>(
    maxSize: Long,
    expireAfterAccess: Duration,
    recordStats: Boolean = true,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : FactoryCache<K, V> {
    init {
        require(maxSize > 0) { "maxSize must be greater than 0" }
        require(expireAfterAccess.isPositive()) { "expireAfterAccess must be positive" }
    }

    private val evictionListenerRef = AtomicReference<((suspend (K?, V?) -> Unit)?)>(null)

    private val cache: CoroutineCache<K, V> =
        Caffeine
            .newBuilder()
            .maximumSize(maxSize)
            .expireAfterAccess(expireAfterAccess.toJavaDuration())
            .let { if (recordStats) it.recordStats() else it }
            .evictionListener(
                RemovalListener<K, V> { key, value, _ ->
                    key?.let {
                        val listener = evictionListenerRef.get()
                        if (listener != null) {
                            coroutineScope.launch {
                                listener.invoke(key, value)
                            }
                        }
                    }
                },
            ).buildCoroutine()

    override suspend fun get(
        key: K,
        mappingFunction: suspend (K) -> V,
    ): V = cache.get(key, mappingFunction)

    override suspend fun getIfPresent(key: K): V? = cache.getIfPresent(key)

    override suspend fun put(
        key: K,
        value: V,
    ): Unit = cache.put(key, value)

    override suspend fun invalidate(key: K): Unit =
        withContext(Dispatchers.IO) {
            cache.synchronous().invalidate(key)
        }

    override suspend fun estimatedSize(): Long =
        withContext(Dispatchers.IO) {
            cache.synchronous().estimatedSize()
        }

    override suspend fun stats(): FactoryCacheStats =
        withContext(Dispatchers.IO) {
            FactoryCacheStats.from(cache.synchronous().stats())
        }

    override fun setEvictionListener(listener: (suspend (K?, V?) -> Unit)?) {
        evictionListenerRef.set(listener)
    }
}
