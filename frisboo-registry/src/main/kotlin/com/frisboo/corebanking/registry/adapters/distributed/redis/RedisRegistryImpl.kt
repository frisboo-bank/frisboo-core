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
package com.frisboo.corebanking.registry.adapters.distributed.redis

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_SCAN_BATCH_SIZE
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_SCAN_MAX_RESPONSE_BYTES
import com.frisboo.corebanking.persistence.redis.utils.RedisCodec
import com.frisboo.corebanking.persistence.redis.utils.checkHealth
import com.frisboo.corebanking.persistence.redis.utils.evalScript
import com.frisboo.corebanking.persistence.redis.utils.scanCount
import com.frisboo.corebanking.persistence.redis.utils.scanPage
import com.frisboo.corebanking.registry.contracts.DistributedRegistry
import com.frisboo.corebanking.registry.contracts.RegistrySerializer
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.REGISTRY_DEFAULT_MAX_PAGE_SIZE
import com.frisboo.corebanking.registry.models.REGISTRY_DEFAULT_OPERATION_TIMEOUT
import com.frisboo.corebanking.registry.models.RegistryContainsResult
import com.frisboo.corebanking.registry.models.RegistryEvictResult
import com.frisboo.corebanking.registry.models.RegistryGetOrPutResult
import com.frisboo.corebanking.registry.models.RegistryGetResult
import com.frisboo.corebanking.registry.models.RegistryIsHealthyResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryPutResult
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.RegistrySetTtlResult
import com.frisboo.corebanking.registry.models.RegistrySizeResult
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

@OptIn(ExperimentalLettuceCoroutinesApi::class)
public class RedisRegistryImpl<K : Any, V : Any>(
    public val scope: RegistryScope,
    connection: StatefulRedisConnection<ByteArray, ByteArray>,
    private val operationTimeout: Duration = REGISTRY_DEFAULT_OPERATION_TIMEOUT,
    keySerializer: RegistrySerializer<K>,
    valueSerializer: RegistrySerializer<V>,
) : DistributedRegistry<K, V> {

    private val commands = connection.coroutines()

    private val codec = RedisCodec<K, V>(
        prefix = scope.prefix,
        keySerializer = keySerializer,
        valueSerializer = valueSerializer,
    )

    /**
     * Map to track in-flight getOrPut operations to prevent thundering herd on cache misses.
     */
    private val inflight = ConcurrentHashMap<K, Deferred<Either<RegistryError, RegistryGetOrPutResult<V>>>>()

    init {
        require(operationTimeout > ZERO) {
            "operationTimeout must be positive, got: $operationTimeout"
        }
    }

    override suspend fun isHealthy(): Either<RegistryError, RegistryIsHealthyResult> =
        commands.checkHealth(timeout = operationTimeout).fold(
            ifLeft = { error ->
                RegistryIsHealthyResult.Down(
                    when (error) {
                        is PersistenceError.ConnectionFailed -> error.message
                        is PersistenceError.OperationTimeout ->
                            "Operation timed out after ${error.timeoutMillis} ms"

                        else -> "Unexpected error: $error"
                    },
                ).right()
            },
            ifRight = { healthy ->
                if (healthy) RegistryIsHealthyResult.Up.right()
                else RegistryIsHealthyResult.Down("Redis PING returned unexpected response").right()
            },
        )

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): Either<RegistryError, RegistryPutResult<V?>> = either {
        val serializedKey = codec.serializeKey(key).mapLeft { RegistryError.fromPersistenceError(it) }.bind()
        val serializedValue = codec.serializeValue(value).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val result = doPutUpsert(serializedKey, serializedValue, ttl).bind()

        when (result.statusByte) {
            0.toByte() -> RegistryPutResult.Updated(previousValue = result.previousValue, newValue = value)
            1.toByte() -> RegistryPutResult.Created(value)
            else -> raise(RegistryError.ConnectionFailed("Unexpected result"))
        }
    }

    override suspend fun get(key: K): Either<RegistryError, RegistryGetResult<V?>> = either {
        val serializedKey = codec.serializeKey(key).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        when (val result = doGet(serializedKey).bind()) {
            null -> RegistryGetResult.NotFound
            else -> RegistryGetResult.Found(result)
        }
    }

    override suspend fun contains(key: K): Either<RegistryError, RegistryContainsResult> = either {
        val serializedKey = codec.serializeKey(key).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val exists = executeWithTimeout(
            timeout = operationTimeout,
            block = {
                commands.exists(serializedKey)
            },
            mapError = { e -> RegistryError.ConnectionFailed("Failed to check key existence in Redis", e) },
        ).bind() ?: raise(RegistryError.ConnectionFailed("EXISTS command returned null"))

        when {
            exists == 0L -> RegistryContainsResult.NotFound
            exists > 0L -> RegistryContainsResult.Found
            else -> raise(RegistryError.ConnectionFailed("Unexpected contains response: $exists"))
        }
    }

    override suspend fun size(): Either<RegistryError, RegistrySizeResult> = either {
        val count = commands.scanCount(
            pattern = codec.scanPattern,
            timeout = operationTimeout,
            batchSize = REDIS_DEFAULT_SCAN_BATCH_SIZE,
        ).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        RegistrySizeResult.Size(count)
    }

    /**
     * Implements getOrPut with a Redis Lua script to ensure atomicity and prevent thundering herd on cache misses.
     * Uses an in-memory map to track in-flight operations for the same key and avoid redundant Redis calls.
     */
    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): Either<RegistryError, RegistryGetOrPutResult<V>> {
        inflight[key]?.let { return it.await() }

        val deferred = coroutineScope {
            val candidate = async(start = CoroutineStart.LAZY) {
                doGetOrPut(key, ttl, factory)
            }

            val winner = inflight.putIfAbsent(key, candidate)
            if (winner != null) {
                candidate.cancel()
                return@coroutineScope winner
            }

            candidate.start()
            candidate
        }

        return try {
            deferred.await()
        } finally {
            inflight.remove(key, deferred)
        }
    }

    private suspend fun doGetOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): Either<RegistryError, RegistryGetOrPutResult<V>> = either {
        val serializedKey = codec.serializeKey(key).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        doGet(serializedKey).bind()?.let { existingValue ->
            return@either RegistryGetOrPutResult.Found(existingValue)
        }

        val newValue =
            Either.catch { factory() }.mapLeft {
                RegistryError.ComputationFailed("Factory function for key `$key` failed", it)
            }.bind()

        val serializedValue = codec.serializeValue(newValue).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val (statusByte, resultValue) = doPutAtomic(
            serializedKey,
            serializedValue,
            ttl,
        ).bind()

        when (statusByte) {
            0.toByte() -> {
                val existingValue =
                    resultValue ?: raise(RegistryError.ConnectionFailed("Lua returned Found status but no value"))
                RegistryGetOrPutResult.Found(existingValue)
            }

            1.toByte() -> RegistryGetOrPutResult.Created(newValue)
            else -> raise(RegistryError.ConnectionFailed("Unexpected Lua script result"))
        }
    }

    override suspend fun evict(key: K): Either<RegistryError, RegistryEvictResult> = either {
        val serializedKey = codec.serializeKey(key).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val deletedCount = executeWithTimeout(
            timeout = operationTimeout,
            block = { commands.del(serializedKey) },
            mapError = { e -> RegistryError.ConnectionFailed("Failed to evict key from Redis", e) },
        ).bind() ?: raise(RegistryError.ConnectionFailed("DEL command returned null"))

        when (deletedCount) {
            0L -> RegistryEvictResult.NotFound
            1L -> RegistryEvictResult.Evicted(deletedCount)
            else -> raise(RegistryError.ConnectionFailed("Unexpected deleted count: $deletedCount"))
        }
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): Either<RegistryError, RegistryPage<K>> = either {
        val safeLimit = limit.coerceIn(1, REGISTRY_DEFAULT_MAX_PAGE_SIZE)
        ensure(limit == safeLimit) {
            RegistryError.InvalidArgument(
                "limit",
                "Page size must be between 1 and $REGISTRY_DEFAULT_MAX_PAGE_SIZE, got: $limit",
            )
        }

        val page = commands.scanPage(
            pattern = codec.scanPattern,
            cursor = cursor,
            limit = safeLimit,
            timeout = operationTimeout,
        ).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val responseBytes = page.keys.sumOf { it.size }
        ensure(responseBytes <= REDIS_DEFAULT_SCAN_MAX_RESPONSE_BYTES) {
            RegistryError.ResponseTooLarge(sizeBytes = responseBytes)
        }

        val keys = page.keys.map {
            codec.deserializeKey(it).mapLeft { e -> RegistryError.fromPersistenceError(e) }.bind()
        }

        RegistryPage(
            items = keys,
            nextCursor = page.nextCursor,
        )
    }

    override suspend fun setTTL(key: K, ttl: Duration): Either<RegistryError, RegistrySetTtlResult> = either {
        val ttlMs = ttl.inWholeMilliseconds
        ensure(ttlMs > 0) {
            RegistryError.InvalidArgument("ttl", "TTL must be >= 1ms, got: $ttl")
        }

        val serializedKey = codec.serializeKey(key).mapLeft { e -> RegistryError.fromPersistenceError(e) }.bind()

        val result = executeWithTimeout(
            timeout = operationTimeout,
            block = { commands.pexpire(serializedKey, ttlMs) },
            mapError = { e -> RegistryError.ConnectionFailed("Failed to set TTL in Redis", e) },
        ).bind() ?: raise(RegistryError.ConnectionFailed("PEXPIRE returned null"))

        when (result) {
            false -> RegistrySetTtlResult.NotFound
            true -> RegistrySetTtlResult.Applied
        }
    }

    private suspend fun doGet(serializedKey: ByteArray): Either<RegistryError, V?> = either {
        val result = executeWithTimeout(
            timeout = operationTimeout,
            block = { commands.get(serializedKey) },
            mapError = { e -> RegistryError.ConnectionFailed("Failed to get value from Redis", e) },
        ).bind() ?: return@either null

        codec.deserializeValue(result).mapLeft { e -> RegistryError.fromPersistenceError(e) }.bind()
    }

    private data class DoPutUpsertResult<V>(
        val statusByte: Byte,
        val previousValue: V?,
    )

    private suspend fun doPutUpsert(
        serializedKey: ByteArray,
        serializedValue: ByteArray,
        ttl: Duration?,
    ): Either<RegistryError, DoPutUpsertResult<V>> = either {
        val ttlMs = ttl?.inWholeMilliseconds
        ensure(ttlMs == null || ttlMs > 0) {
            RegistryError.InvalidArgument("ttl", "TTL must be >= 1ms, got: $ttl")
        }

        val script = """
            local existing = redis.call('GET', KEYS[1])
            if #ARGV == 2 then
                redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
            else
                redis.call('SET', KEYS[1], ARGV[1])
            end
            if existing then
                return string.char(0) .. existing
            else
                return string.char(1)
            end
        """.trimIndent()

        val scriptArgs = if (ttlMs != null) {
            arrayOf(serializedValue, ttlMs.toString().toByteArray())
        } else {
            arrayOf(serializedValue)
        }

        val luaResult = commands.evalScript(
            script = script,
            keys = arrayOf(serializedKey),
            args = scriptArgs,
            timeout = operationTimeout,
            errorContext = "Failed to execute upsert operation",
        ).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val statusByte = luaResult[0]

        var previousValue: V? = null
        if (statusByte == 0.toByte() && luaResult.size > 1) {
            previousValue = codec.deserializeValue(luaResult.copyOfRange(1, luaResult.size))
                .mapLeft { RegistryError.fromPersistenceError(it) }.bind()
        }

        DoPutUpsertResult(
            statusByte = statusByte,
            previousValue = previousValue,
        )
    }

    private suspend fun doPutAtomic(
        serializedKey: ByteArray,
        serializedValue: ByteArray,
        ttl: Duration?,
    ): Either<RegistryError, Pair<Byte, V?>> = either {
        val ttlMs = ttl?.inWholeMilliseconds
        ensure(ttlMs == null || ttlMs > 0) {
            RegistryError.InvalidArgument("ttl", "TTL must be >= 1ms, got: $ttl")
        }

        val script = """
            local existing = redis.call('GET', KEYS[1])
            if existing then
                return string.char(0) .. existing
            end
            if #ARGV == 2 then
                redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[2])
            else
                redis.call('SET', KEYS[1], ARGV[1])
            end
            return string.char(1)
        """.trimIndent()

        val scriptArgs = if (ttlMs != null) {
            arrayOf(serializedValue, ttlMs.toString().toByteArray())
        } else {
            arrayOf(serializedValue)
        }

        val luaResult = commands.evalScript(
            script = script,
            keys = arrayOf(serializedKey),
            args = scriptArgs,
            timeout = operationTimeout,
            errorContext = "Failed to execute atomic put operation",
        ).mapLeft { RegistryError.fromPersistenceError(it) }.bind()

        val statusByte = luaResult[0]
        val existingValue = if (luaResult.size > 1) {
            codec.deserializeValue(luaResult.copyOfRange(1, luaResult.size))
                .mapLeft { e -> RegistryError.fromPersistenceError(e) }.bind()
        } else {
            null
        }

        Pair(statusByte, existingValue)
    }
}
