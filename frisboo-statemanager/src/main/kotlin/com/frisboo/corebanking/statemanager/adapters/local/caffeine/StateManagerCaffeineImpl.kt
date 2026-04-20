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
package com.frisboo.corebanking.statemanager.adapters.local.caffeine

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.frisboo.corebanking.statemanager.contracts.LocalStateManager
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.REGISTRY_DEFAULT_MAX_PAGE_SIZE
import com.frisboo.corebanking.statemanager.models.StateManagerContainsResult
import com.frisboo.corebanking.statemanager.models.StateManagerEvictResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetOrPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetResult
import com.frisboo.corebanking.statemanager.models.StateManagerPage
import com.frisboo.corebanking.statemanager.models.StateManagerPutResult
import com.frisboo.corebanking.persistence.core.models.StateManagerScope
import com.frisboo.corebanking.statemanager.models.StateManagerSetTtlResult
import com.frisboo.corebanking.statemanager.models.StateManagerSizeResult
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import com.github.benmanes.caffeine.cache.RemovalCause
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

private const val DEFAULT_MAXIMUM_SIZE = 10_000L

public class StateManagerCaffeineImpl<K : Any, V : Any>(
    public val scope: StateManagerScope,
    private val maximumSize: Long = DEFAULT_MAXIMUM_SIZE,
) : LocalStateManager<K, V> {

    private data class Key<out K>(val scope: String, val userKey: K)
    private data class Value<out V>(val value: V, val ttlNs: Long)

    private val expiredCount = AtomicInteger(0)

    // In-flight factory computations for getOrPut
    private val inflight = ConcurrentHashMap<K, Deferred<StateManagerGetOrPutResult<V>>>()

    private val cache: Cache<Key<K>, Value<V>> =
        Caffeine
            .newBuilder()
            .maximumSize(maximumSize)
            .expireAfter(
                object : Expiry<Key<K>, Value<V>> {
                    override fun expireAfterCreate(key: Key<K>, value: Value<V>, currentTime: Long): Long = value.ttlNs
                    override fun expireAfterUpdate(
                        key: Key<K>,
                        value: Value<V>,
                        currentTime: Long,
                        currentDuration: Long,
                    ): Long = value.ttlNs

                    override fun expireAfterRead(
                        key: Key<K>,
                        value: Value<V>,
                        currentTime: Long,
                        currentDuration: Long,
                    ): Long = currentDuration
                },
            )
            .removalListener<Key<K>, Value<V>> { _, _, cause ->
                if (cause == RemovalCause.EXPIRED) expiredCount.incrementAndGet()
            }
            .build()

    override suspend
    fun cleanupExpired(): Int {
        cache.cleanUp()
        return expiredCount.getAndSet(0)
    }

    override suspend
    fun get(key: K): Either<StateManagerError, StateManagerGetResult<V?>> = either {
        val entry = cache.getIfPresent(scoped(key)) ?: return@either StateManagerGetResult.NotFound
        StateManagerGetResult.Found(entry.value)
    }

    override suspend
    fun contains(key: K): Either<StateManagerError, StateManagerContainsResult> = either {
        when (cache.getIfPresent(scoped(key))) {
            null -> StateManagerContainsResult.NotFound
            else -> StateManagerContainsResult.Found
        }
    }

    override suspend
    fun size(): Either<StateManagerError, StateManagerSizeResult> = either {
        StateManagerSizeResult.Size(cache.estimatedSize())
    }

    override suspend fun put(
        key: K,
        value: V,
    ): Either<StateManagerError, StateManagerPutResult<V?>> = either {
        val ttlNs = Long.MAX_VALUE
        val timed = Value(value, ttlNs)
        val cacheKey = scoped(key)

        when (val previous = cache.asMap().put(cacheKey, timed)) {
            null -> StateManagerPutResult.Created(value)
            else -> StateManagerPutResult.Updated(previous.value, value)
        }
    }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerPutResult<V?>> = either {
        val ttlNs = doValidateTtl(ttl).bind()
        val timed = Value(value, ttlNs)
        val cacheKey = scoped(key)

        when (val previous = cache.asMap().put(cacheKey, timed)) {
            null -> StateManagerPutResult.Created(value)
            else -> StateManagerPutResult.Updated(previous.value, value)
        }
    }

    override suspend fun getOrPut(
        key: K,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> = either {
        val cacheKey = scoped(key)

        // Fast path: already in cache
        cache.getIfPresent(cacheKey)?.let {
            return@either StateManagerGetOrPutResult.Found(it.value)
        }

        // Use inflight map to deduplicate concurrent loads
        val deferred = CompletableDeferred<StateManagerGetOrPutResult<V>>()
        val existing = inflight.putIfAbsent(key, deferred)
        if (existing != null) {
            // Another coroutine is already loading this key
            return@either existing.await()
        }

        try {
            // Double-check cache after acquiring "lock"
            cache.getIfPresent(cacheKey)?.let {
                return@either StateManagerGetOrPutResult.Found(it.value)
            }

            val newValue = try {
                factory()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                raise(StateManagerError.ComputationFailed("Factory failed for key $key", e))
            }

            val ttlNs = Long.MAX_VALUE
            val timed = Value(newValue, ttlNs)
            cache.put(cacheKey, timed)
            val result = StateManagerGetOrPutResult.Created(newValue)
            deferred.complete(result)
            result
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            inflight.remove(key)
        }
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>>  = either{
        val cacheKey = scoped(key)

        // Fast path: already in cache
        cache.getIfPresent(cacheKey)?.let {
            return@either StateManagerGetOrPutResult.Found(it.value)
        }

        // Use inflight map to deduplicate concurrent loads
        val deferred = CompletableDeferred<StateManagerGetOrPutResult<V>>()
        val existing = inflight.putIfAbsent(key, deferred)
        if (existing != null) {
            // Another coroutine is already loading this key
            return@either existing.await()
        }

        try {
            // Double-check cache after acquiring "lock"
            cache.getIfPresent(cacheKey)?.let {
                return@either StateManagerGetOrPutResult.Found(it.value)
            }

            val newValue = try {
                factory()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                raise(StateManagerError.ComputationFailed("Factory failed for key $key", e))
            }

            val ttlNs = doValidateTtl(ttl).bind()
            val timed = Value(newValue, ttlNs)
            cache.put(cacheKey, timed)
            val result = StateManagerGetOrPutResult.Created(newValue)
            deferred.complete(result)
            result
        } catch (e: Exception) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            inflight.remove(key)
        }
    }

    override suspend
    fun evict(key: K): Either<StateManagerError, StateManagerEvictResult> = either {
        when (cache.asMap().remove(scoped(key))) {
            null -> StateManagerEvictResult.NotFound
            else -> StateManagerEvictResult.Evicted(1)
        }
    }

    override suspend
    fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<StateManagerError, StateManagerPage<K>> = either {
        ensure(limit in 1..REGISTRY_DEFAULT_MAX_PAGE_SIZE) {
            StateManagerError.InvalidArgument(
                "limit",
                "Page size must be between 1 and $REGISTRY_DEFAULT_MAX_PAGE_SIZE, got: $limit",
            )
        }

        val allKeys = cache.asMap().keys.map { it.userKey }.toList()
        val startIndex = cursor?.toIntOrNull() ?: 0

        ensure(startIndex >= 0) {
            StateManagerError.InvalidArgument("cursor", "Invalid cursor")
        }

        val pageKeys = allKeys.drop(startIndex).take(limit)
        val nextCursor = if (startIndex + limit < allKeys.size) (startIndex + limit).toString() else null

        StateManagerPage(
            items = pageKeys,
            nextCursor = nextCursor,
        )
    }

    override suspend
    fun setTTL(
        key: K,
        ttl: Duration,
    ): Either<StateManagerError, StateManagerSetTtlResult> = either {
        val ttlNs = doValidateTtl(ttl).bind() ?: Long.MAX_VALUE
        val cacheKey = scoped(key)
        val timed = cache.getIfPresent(cacheKey) ?: return@either StateManagerSetTtlResult.NotFound
        val updated = timed.copy(ttlNs = ttlNs)

        cache.put(cacheKey, updated)
        StateManagerSetTtlResult.Applied
    }

    private fun scoped(key: K): Key<K> = Key(scope.prefix, key)

    private fun doValidateTtl(ttl: Duration): Either<StateManagerError, Long> = either {
        ttl.inWholeNanoseconds.also { ns ->
            ensure(ns >= 1_000_000) { StateManagerError.InvalidArgument("ttl", "TTL must be >= 1ms, got: $ttl") }
        }
    }
}
