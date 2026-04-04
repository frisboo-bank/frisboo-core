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
package com.frisboo.corebanking.registry.decorators

import com.frisboo.corebanking.registry.contracts.Registry
import com.frisboo.corebanking.registry.contracts.ResilienceExecutor
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.SetTtlResult
import kotlin.time.Duration

/**
 * Resilient [Registry] decorator: routes every call through [ResilienceExecutor], falling back on failure.
 *
 * @param primary preferred registry backend (e.g. Redis)
 * @param fallback used when [primary] fails (e.g. in-memory)
 * @param resilientExecutor controls circuit-breaking, retry and fallback policy
 */
public class ResilientRegistryImpl<K : Any, V : Any>(
    private val primary: Registry<K, V>,
    private val fallback: Registry<K, V>,
    private val resilientExecutor: ResilienceExecutor,
) : Registry<K, V> {
    private suspend fun <T> resilient(
        primaryOption: suspend () -> T,
        fallbackOption: suspend () -> T,
    ): T = resilientExecutor.execute(primaryOption, fallbackOption)

    override suspend fun get(key: K): V? =
        resilient(
            primaryOption = { primary.get(key) },
            fallbackOption = { fallback.get(key) },
        )

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): GetOrPutResult<V> {
        var factoryResult: V? = null
        val cachedFactory: suspend () -> V = {
            factoryResult ?: factory().also { factoryResult = it }
        }
        return resilient(
            primaryOption = { primary.getOrPut(key, ttl, cachedFactory) },
            fallbackOption = { fallback.getOrPut(key, ttl, cachedFactory) },
        )
    }

    override suspend fun contains(key: K): Boolean =
        resilient(
            primaryOption = { primary.contains(key) },
            fallbackOption = { fallback.contains(key) },
        )

    override suspend fun size(): Long =
        resilient(
            primaryOption = { primary.size() },
            fallbackOption = { fallback.size() },
        )

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): PutResult =
        resilient(
            primaryOption = { primary.put(key, value, ttl) },
            fallbackOption = { fallback.put(key, value, ttl) },
        )

    override suspend fun evict(key: K): EvictResult =
        resilient(
            primaryOption = { primary.evict(key) },
            fallbackOption = { fallback.evict(key) },
        )

    override suspend fun keys(): Set<K> =
        resilient(
            primaryOption = { primary.keys() },
            fallbackOption = { fallback.keys() },
        )

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): RegistryPage<K> =
        resilient(
            primaryOption = { primary.keysPage(cursor, limit) },
            fallbackOption = { fallback.keysPage(cursor, limit) },
        )

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): SetTtlResult =
        resilient(
            primaryOption = { primary.setTTL(key, ttl) },
            fallbackOption = { fallback.setTTL(key, ttl) },
        )
}
