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
package com.frisboo.corebanking.core.factory.contracts

import com.frisboo.corebanking.core.factory.models.FactoryCacheStats

public interface FactoryCache<K : Any, V : Any> {
    /** Returns the value for [key], computing it via [mappingFunction] if absent. */
    public suspend fun get(
        key: K,
        mappingFunction: suspend (K) -> V,
    ): V

    /** Returns the value if present, otherwise null. */
    public suspend fun getIfPresent(key: K): V?

    /** Associates [value] with [key]. */
    public suspend fun put(
        key: K,
        value: V,
    )

    /** Removes the entry for [key]. */
    public suspend fun invalidate(key: K)

    /** Approximate number of entries. */
    public suspend fun estimatedSize(): Long

    /** Returns cache statistics, or null if not supported. */
    public suspend fun stats(): FactoryCacheStats?

    /** Registers a listener that is called when an entry is evicted */
    public fun setEvictionListener(listener: (suspend (K?, V?) -> Unit)?)
}
