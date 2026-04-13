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
package com.frisboo.corebanking.statemanager.adapters.distributed.redis

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_LOCK_TTL
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_WAIT_MAX_DELAY
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.statemanager.contracts.DistributedStateManager
import com.frisboo.corebanking.statemanager.errors.StateManagerError
import com.frisboo.corebanking.statemanager.models.REGISTRY_DEFAULT_MAX_PAGE_SIZE
import com.frisboo.corebanking.statemanager.models.StateManagerContainsResult
import com.frisboo.corebanking.statemanager.models.StateManagerEvictResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetOrPutResult
import com.frisboo.corebanking.statemanager.models.StateManagerGetResult
import com.frisboo.corebanking.statemanager.models.StateManagerIsHealthyResult
import com.frisboo.corebanking.statemanager.models.StateManagerPage
import com.frisboo.corebanking.statemanager.models.StateManagerPutResult
import com.frisboo.corebanking.persistence.core.models.StateManagerScope
import com.frisboo.corebanking.statemanager.models.StateManagerSetTtlResult
import com.frisboo.corebanking.statemanager.models.StateManagerSizeResult
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.TimeSource

@OptIn(ExperimentalLettuceCoroutinesApi::class)
public class StateManagerRedisImpl<K : Any, V : Any>(
    public val scope: StateManagerScope,
    private val redisOperations: RedisOperations<K, V>,
) : DistributedStateManager<K, V> {

    // Map to track in-flight getOrPut operations to prevent thundering herd on cache misses.
    private val inflight = ConcurrentHashMap<K, Deferred<Either<StateManagerError, StateManagerGetOrPutResult<V>>>>()

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun isHealthy(): Either<StateManagerError, StateManagerIsHealthyResult> =
        redisOperations.ping().mapLeft(StateManagerError::fromPersistenceError).map {
            when (it) {
                "PONG" -> StateManagerIsHealthyResult.Up
                else -> StateManagerIsHealthyResult.Down("Unexpected PING response: $it")
            }
        }

    override suspend fun put(key: K, value: V): Either<StateManagerError, StateManagerPutResult<V>> = either {
        val previousValue =
            redisOperations.set(key = key, value = value).mapLeft(StateManagerError::fromPersistenceError).bind()

        when (previousValue) {
            null -> StateManagerPutResult.Created(value)
            else -> StateManagerPutResult.Updated(previousValue, value)
        }
    }

    override suspend fun put(key: K, value: V, ttl: Duration): Either<StateManagerError, StateManagerPutResult<V?>> =
        either {
            val ttlMs = doValidateTtl(ttl).bind()
            val previousValue = redisOperations.set(key = key, value = value, ttlMs = ttlMs)
                .mapLeft(StateManagerError::fromPersistenceError).bind()

            when (previousValue) {
                null -> StateManagerPutResult.Created(value)
                else -> StateManagerPutResult.Updated(previousValue = previousValue, newValue = value)
            }
        }

    override suspend fun get(key: K): Either<StateManagerError, StateManagerGetResult<V?>> = either {
        when (val result = redisOperations.get(key = key).mapLeft(StateManagerError::fromPersistenceError).bind()) {
            null -> StateManagerGetResult.NotFound
            else -> StateManagerGetResult.Found(result)
        }
    }

    override suspend fun contains(key: K): Either<StateManagerError, StateManagerContainsResult> = either {
        val exists = redisOperations.exists(key).mapLeft(StateManagerError::fromPersistenceError).bind()

        when (exists) {
            true -> StateManagerContainsResult.Found
            false -> StateManagerContainsResult.NotFound
        }
    }

    override suspend fun size(): Either<StateManagerError, StateManagerSizeResult> = either {
        val count = redisOperations.scanCount(batchSize = 1000).mapLeft(StateManagerError::fromPersistenceError).bind()

        StateManagerSizeResult.Size(count)
    }

    override suspend fun getOrPut(
        key: K,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> {
        val existing = inflight[key]
        if (existing != null) {
            return existing.await()
        }

        val deferred = backgroundScope.async {
            doGetOrPut(key = key, factory = factory, ttl = null)
        }

        val previous = inflight.putIfAbsent(key, deferred)
        if (previous != null) {
            deferred.cancel()
            return previous.await()
        }

        deferred.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                inflight.remove(key, deferred)
            }
        }

        return try {
            deferred.await()
        } finally {
            inflight.remove(key, deferred)
        }
    }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration,
        factory: suspend () -> V,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> {
        val existing = inflight[key]
        if (existing != null) {
            return existing.await()
        }

        val deferred = backgroundScope.async {
            doGetOrPut(key = key, factory = factory, ttl = ttl)
        }

        val previous = inflight.putIfAbsent(key, deferred)
        if (previous != null) {
            deferred.cancel()
            return previous.await()
        }

        deferred.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                inflight.remove(key, deferred)
            }
        }

        return try {
            deferred.await()
        } finally {
            inflight.remove(key, deferred)
        }
    }

    override suspend fun evict(key: K): Either<StateManagerError, StateManagerEvictResult> = either {
        val evicted = redisOperations.del(key = key).mapLeft(StateManagerError::fromPersistenceError).bind()

        when (evicted) {
            true -> StateManagerEvictResult.Evicted(1)
            false -> StateManagerEvictResult.NotFound
        }
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<StateManagerError, StateManagerPage<K>> = either {
        ensure(limit in 1..REGISTRY_DEFAULT_MAX_PAGE_SIZE) {
            StateManagerError.InvalidArgument(
                "limit",
                "Page size must be between 1 and $REGISTRY_DEFAULT_MAX_PAGE_SIZE, got: $limit",
            )
        }

        val page = redisOperations.scanPage(cursor = RedisScanCursor.of(cursor), limit = limit)
            .mapLeft(StateManagerError::fromPersistenceError).bind()

        StateManagerPage(items = page.keys, nextCursor = page.nextCursor)
    }

    override suspend fun setTTL(key: K, ttl: Duration): Either<StateManagerError, StateManagerSetTtlResult> = either {
        val ttlMs: Long = doValidateTtl(ttl).bind()

        val applied = redisOperations.pexpire(key = key, ttlMs = ttlMs)
            .mapLeft(StateManagerError::fromPersistenceError).bind()

        when (applied) {
            true -> StateManagerSetTtlResult.Applied
            false -> StateManagerSetTtlResult.NotFound
        }
    }

    // ---------- Internal helpers ----------
    private suspend inline fun doGetOrPut(
        key: K,
        factory: suspend () -> V,
        ttl: Duration?,
    ): Either<StateManagerError, StateManagerGetOrPutResult<V>> = either {
        val deadline = TimeSource.Monotonic.markNow() + REDIS_DEFAULT_LOCK_TTL
        var delayMs = 1L
        var quickRetries = 5

        val ttlMs = ttl?.let { doValidateTtl(it).bind() }

        while (true) {
            redisOperations.get(key).mapLeft(StateManagerError::fromPersistenceError).bind()?.let {
                return@either StateManagerGetOrPutResult.Found(it)
            }

            val lock = redisOperations.acquireLock(key)
                .mapLeft(StateManagerError::fromPersistenceError).bind()

            if (lock != null) {
                try {
                    redisOperations.get(key).mapLeft(StateManagerError::fromPersistenceError).bind()?.let {
                        return@either StateManagerGetOrPutResult.Found(it)
                    }

                    val newValue = factory()

                    val previousValue = if (ttlMs != null) {
                        redisOperations.set(key, newValue, ttlMs, lock)
                    } else {
                        redisOperations.set(key, newValue, lock)
                    }.mapLeft(StateManagerError::fromPersistenceError).bind()

                    return@either when (previousValue) {
                        null -> StateManagerGetOrPutResult.Created(newValue)
                        else -> StateManagerGetOrPutResult.Updated(previousValue, newValue)
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> throw e
                        else -> raise(StateManagerError.ComputationFailed("Factory failed for key `$key`", e))
                    }
                } finally {
                    redisOperations.releaseLock(key, lock)
                }
            }

            if (TimeSource.Monotonic.markNow() > deadline) {
                raise(
                    StateManagerError.OperationTimeout(
                        "Timed out waiting for lock on key '$key'",
                        REDIS_DEFAULT_WAIT_MAX_DELAY,
                    ),
                )
            }

            when {
                quickRetries-- > 0 -> yield()
                else -> {
                    delay(delayMs)
                    delayMs = (delayMs * 2).coerceAtMost(REDIS_DEFAULT_WAIT_MAX_DELAY.inWholeMilliseconds)
                }
            }
        }
        raise(StateManagerError.OperationFailed("Unreachable"))
    }

    private fun doValidateTtl(ttl: Duration): Either<StateManagerError, Long> = either {
        ttl.inWholeMilliseconds.also { ms ->
            ensure(ms > 0) { StateManagerError.InvalidArgument("ttl", "TTL must be >= 1ms, got: $ttl") }
        }
    }
}
