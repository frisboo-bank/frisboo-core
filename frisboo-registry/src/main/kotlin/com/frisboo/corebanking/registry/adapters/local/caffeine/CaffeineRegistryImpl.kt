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

import arrow.core.Either
import com.frisboo.corebanking.registry.contracts.LocalRegistry
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.RegistryContainsResult
import com.frisboo.corebanking.registry.models.RegistryEvictResult
import com.frisboo.corebanking.registry.models.RegistryGetOrPutResult
import com.frisboo.corebanking.registry.models.RegistryGetResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryPutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.RegistrySetTtlResult
import com.frisboo.corebanking.registry.models.RegistrySizeResult
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

    override suspend fun cleanupExpired(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun get(key: K): Either<RegistryError, RegistryGetResult<V?>> {
        TODO("Not yet implemented")
    }

    override suspend fun contains(key: K): Either<RegistryError, RegistryContainsResult> {
        TODO("Not yet implemented")
    }

    override suspend fun size(): Either<RegistryError, RegistrySizeResult> {
        TODO("Not yet implemented")
    }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): Either<RegistryError, RegistryPutResult<V?>> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): Either<RegistryError, RegistryGetOrPutResult<V>> {
        TODO("Not yet implemented")
    }

    override suspend fun evict(key: K): Either<RegistryError, RegistryEvictResult> {
        TODO("Not yet implemented")
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<RegistryError, RegistryPage<K>> {
        TODO("Not yet implemented")
    }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<RegistryError, RegistrySetTtlResult> {
        TODO("Not yet implemented")
    }
//
//    override suspend fun get(key: K): V? = cache.getIfPresent(key)?.value
//
//    override suspend fun getOrPut(
//        key: K,
//        ttl: Duration?,
//        factory: suspend () -> V,
//    ): RegistryGetOrPutResult<V> {
//        validateOptionalTtl(ttl)?.let { return RegistryGetOrPutResult.Failed(it) }
//        var factoryInvoked = false
//        val timedValue =
//            cache.get(key) {
//                factoryInvoked = true
//                TimedValue(kotlinx.coroutines.runBlocking { factory() }, ttlNanos(ttl))
//            }
//        return if (factoryInvoked) {
//            RegistryGetOrPutResult.Created(timedValue.value)
//        } else {
//            RegistryGetOrPutResult.Found(timedValue.value)
//        }
//    }
//
//    override suspend fun contains(key: K): Boolean = cache.getIfPresent(key) != null
//
//    override suspend fun size(): Long {
//        cache.cleanUp()
//        return cache.estimatedSize()
//    }
//
//    override suspend fun put(
//        key: K,
//        value: V,
//        ttl: Duration?,
//    ): RegistryPutResult {
//        validateOptionalTtl(ttl)?.let { return RegistryPutResult.Failed(it) }
//        val existed = cache.getIfPresent(key) != null
//        cache.put(key, TimedValue(value, ttlNanos(ttl)))
//        return if (existed) RegistryPutResult.Updated else RegistryPutResult.Created
//    }
//
//    override suspend fun evict(key: K): RegistryEvictResult {
//        val existed = cache.getIfPresent(key) != null
//        cache.invalidate(key)
//        return if (existed) RegistryEvictResult.Evicted else RegistryEvictResult.NotFound
//    }
//
//    override suspend fun keysPage(
//        cursor: String?,
//        limit: Int,
//    ): RegistryPage<K> {
//        cache.cleanUp()
//        val allKeys = cache.asMap().keys.sortedBy { it.toString() }
//        return buildPage(allKeys, cursor, limit, MAX_PAGE_SIZE)
//    }
//
//    override suspend fun setTTL(
//        key: K,
//        ttl: Duration,
//    ): RegistrySetTtlResult {
//        validateTtl(ttl)?.let { return RegistrySetTtlResult.Failed(it) }
//        val existing = cache.getIfPresent(key) ?: return RegistrySetTtlResult.NotFound
//        cache.put(key, TimedValue(existing.value, ttl.inWholeNanoseconds))
//        return RegistrySetTtlResult.Applied
//    }
//
//    override suspend fun cleanupExpired(): Int {
//        expiredCounter.set(0)
//        cache.cleanUp()
//        return expiredCounter.get()
//    }
//
//    private fun ttlNanos(ttl: Duration?): Long {
//        val effective = ttl ?: defaultTtl
//        return effective?.inWholeNanoseconds ?: Long.MAX_VALUE
//    }
}
