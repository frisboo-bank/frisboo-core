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
package com.frisboo.corebanking.registry.adapters.local.caffeine

import com.frisboo.corebanking.registry.contracts.LocalRegistry
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.MAX_PAGE_SIZE
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.SetTtlResult
import com.frisboo.corebanking.registry.models.buildPage
import com.frisboo.corebanking.registry.models.validateOptionalTtl
import com.frisboo.corebanking.registry.models.validateTtl
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.benmanes.caffeine.cache.Scheduler
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

private const val DEFAULT_MAX_SIZE = 10_000L

/**
 * Caffeine-backed local registry; all operations are lock-free via Caffeine's thread-safe [Cache].
 *
 * [cleanupExpired] returns a best-effort count: concurrent SIZE evictions between
 * the counter reset and read may slightly inflate the reported number.
 */
public class CaffeineRegistryImpl<K : Any, V : Any>(
    public val scope: RegistryScope,
    private val maximumSize: Long = DEFAULT_MAX_SIZE,
    private val defaultTtl: Duration? = null,
) : LocalRegistry<K, V> {
    private val expiredCounter = AtomicInteger(0)

    private val cache: Cache<K, TimedValue<V>> =
        Caffeine
            .newBuilder()
            .maximumSize(maximumSize)
            .scheduler(Scheduler.systemScheduler())
            .expireAfter(PerEntryExpiry<K, V>())
            .removalListener<K, TimedValue<V>> { _, _, cause ->
                if (cause == RemovalCause.EXPIRED) expiredCounter.incrementAndGet()
            }.build()

    override suspend fun get(key: K): V? = cache.getIfPresent(key)?.value

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): GetOrPutResult<V> {
        validateOptionalTtl(ttl)?.let { return GetOrPutResult.Failed(it) }
        var factoryInvoked = false
        val timedValue =
            cache.get(key) {
                factoryInvoked = true
                TimedValue(kotlinx.coroutines.runBlocking { factory() }, ttlNanos(ttl))
            }
        return if (factoryInvoked) {
            GetOrPutResult.Created(timedValue.value)
        } else {
            GetOrPutResult.Found(timedValue.value)
        }
    }

    override suspend fun contains(key: K): Boolean = cache.getIfPresent(key) != null

    override suspend fun size(): Long {
        cache.cleanUp()
        return cache.estimatedSize()
    }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): PutResult {
        validateOptionalTtl(ttl)?.let { return PutResult.Failed(it) }
        val existed = cache.getIfPresent(key) != null
        cache.put(key, TimedValue(value, ttlNanos(ttl)))
        return if (existed) PutResult.Updated else PutResult.Created
    }

    override suspend fun evict(key: K): EvictResult {
        val existed = cache.getIfPresent(key) != null
        cache.invalidate(key)
        return if (existed) EvictResult.Evicted else EvictResult.NotFound
    }

    override suspend fun keys(): Set<K> {
        cache.cleanUp()
        return cache.asMap().keys.toSet()
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): RegistryPage<K> {
        cache.cleanUp()
        val allKeys = cache.asMap().keys.sortedBy { it.toString() }
        return buildPage(allKeys, cursor, limit, MAX_PAGE_SIZE)
    }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): SetTtlResult {
        validateTtl(ttl)?.let { return SetTtlResult.Failed(it) }
        val existing = cache.getIfPresent(key) ?: return SetTtlResult.KeyNotFound
        cache.put(key, TimedValue(existing.value, ttl.inWholeNanoseconds))
        return SetTtlResult.Applied
    }

    override suspend fun cleanupExpired(): Int {
        expiredCounter.set(0)
        cache.cleanUp()
        return expiredCounter.get()
    }

    private fun ttlNanos(ttl: Duration?): Long {
        val effective = ttl ?: defaultTtl
        return effective?.inWholeNanoseconds ?: Long.MAX_VALUE
    }
}
