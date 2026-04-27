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
package com.frisboo.corebanking.core.factory.adapters.noop

import com.frisboo.corebanking.core.factory.contracts.FactoryCache
import com.frisboo.corebanking.core.factory.models.FactoryCacheStats

internal class NoOpFactoryCache<K : Any, V : Any> : FactoryCache<K, V> {
    override suspend fun get(
        key: K,
        mappingFunction: suspend (K) -> V,
    ): V = mappingFunction(key)

    override suspend fun getIfPresent(key: K): V? = null

    override suspend fun put(
        key: K,
        value: V,
    ): Unit = Unit

    override suspend fun invalidate(key: K): Unit = Unit

    override suspend fun estimatedSize(): Long = 0

    override suspend fun stats(): FactoryCacheStats? = null

    override fun setEvictionListener(listener: (suspend (K?, V?) -> Unit)?): Unit = Unit
}
