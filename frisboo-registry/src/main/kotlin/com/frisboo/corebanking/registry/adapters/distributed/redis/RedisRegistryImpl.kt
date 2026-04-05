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

import com.frisboo.corebanking.registry.contracts.DistributedRegistry
import com.frisboo.corebanking.registry.contracts.RegistrySerializer
import com.frisboo.corebanking.registry.errors.RegistryError
import com.frisboo.corebanking.registry.models.EvictResult
import com.frisboo.corebanking.registry.models.GetOrPutResult
import com.frisboo.corebanking.registry.models.MAX_PAGE_SIZE
import com.frisboo.corebanking.registry.models.PutResult
import com.frisboo.corebanking.registry.models.RegistryPage
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.registry.models.SetTtlResult
import com.frisboo.corebanking.registry.models.validateOptionalTtl
import com.frisboo.corebanking.registry.models.validateTtl
import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

private val DEFAULT_OPERATION_TIMEOUT = 5.seconds

/**
 * Upper bound on Redis SCAN iterations to prevent runaway loops
 * in degraded network conditions or enormous keyspaces.
 */
private const val MAX_SCAN_ITERATIONS = 10_000

/**
 * Maximum number of keys returned by [RedisRegistryImpl.keys] to prevent
 * unbounded heap allocation. Callers needing more should use [RedisRegistryImpl.keysPage].
 */
private const val MAX_KEYS_RESULT_SIZE = 100_000

/**
 * Lua: GET existing → return it; otherwise SET new value (with optional PSETEX).
 */
private const val GET_OR_PUT_SCRIPT = """
    local existing = redis.call('GET', KEYS[1])
    if existing then
        return existing
    end
    if #ARGV == 2 then
        redis.call('PSETEX', KEYS[1], ARGV[2], ARGV[1])
    else
        redis.call('SET', KEYS[1], ARGV[1])
    end
    return nil
"""

/**
 * Lua: check EXISTS → SET/PSETEX → return 1 if existed, 0 if new.
 */
private const val PUT_SCRIPT = """
    local existed = redis.call('EXISTS', KEYS[1])
    if #ARGV == 2 then
        redis.call('PSETEX', KEYS[1], ARGV[2], ARGV[1])
    else
        redis.call('SET', KEYS[1], ARGV[1])
    end
    return existed
"""

/**
 * Lock-free Redis-backed registry; Lua scripts guarantee server-side atomicity,
 * [getOrPut] may invoke factory speculatively under cross-JVM races.
 */
public class RedisRegistryImpl<K : Any, V : Any>(
    public val scope: RegistryScope,
    private val connection: StatefulRedisConnection<ByteArray, ByteArray>,
    private val keySerializer: RegistrySerializer<K>,
    private val valueSerializer: RegistrySerializer<V>,
    private val operationTimeout: Duration = DEFAULT_OPERATION_TIMEOUT,
) : DistributedRegistry<K, V> {
    private val commands get() = connection.async()
    private val codec = RedisKeyCodec(scope, keySerializer)
    private val timeoutMs = operationTimeout.inWholeMilliseconds.coerceAtLeast(1)

    init {
        require(operationTimeout > ZERO) {
            "operationTimeout must be positive, got: $operationTimeout"
        }
    }

    override suspend fun isHealthy(): Boolean =
        try {
            withTimeout(timeoutMs) {
                connection.async().ping().await() == "PONG"
            }
        } catch (_: TimeoutCancellationException) {
            false
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }

    override suspend fun get(key: K): V? =
        withTimeout(timeoutMs) {
            val raw =
                commands.get(codec.encode(key)).await()
                    ?: return@withTimeout null
            valueSerializer.deserialize(raw)
        }

    override suspend fun getOrPut(
        key: K,
        ttl: Duration?,
        factory: suspend () -> V,
    ): GetOrPutResult<V> {
        validateOptionalTtl(ttl)?.let { return GetOrPutResult.Failed(it) }
        val ttlMs = ttl?.inWholeMilliseconds
        if (ttlMs != null && ttlMs < 1L) {
            return GetOrPutResult.Failed(
                RegistryError.InvalidTtl("Distributed TTL must be >= 1ms, got: $ttl"),
            )
        }
        val rk = codec.encode(key)

        // Fast-path: check if key exists without invoking factory.
        val cached = withTimeout(timeoutMs) { commands.get(rk).await() }
        if (cached != null) return GetOrPutResult.Found(valueSerializer.deserialize(cached))

        // Slow-path: invoke factory (outside any lock), then atomically set via Lua.
        val value = factory()
        val serialized = valueSerializer.serialize(value)

        val luaResult =
            withTimeout(timeoutMs) {
                commands
                    .eval<ByteArray>(
                        GET_OR_PUT_SCRIPT,
                        ScriptOutputType.VALUE,
                        arrayOf(rk),
                        *RedisKeyCodec.luaArgs(serialized, ttlMs),
                    ).await()
            }

        // Lua returns existing value if another writer won the race, null if we stored ours.
        return if (luaResult != null) {
            GetOrPutResult.Found(valueSerializer.deserialize(luaResult))
        } else {
            GetOrPutResult.Created(value)
        }
    }

    override suspend fun contains(key: K): Boolean =
        withTimeout(timeoutMs) {
            commands.exists(codec.encode(key)).await() > 0
        }

    override suspend fun size(): Long =
        withTimeout(timeoutMs) {
            var count = 0L
            var cursor = ScanCursor.of("0")
            var iterations = 0
            do {
                val result = commands.scan(cursor, codec.scopedScanArgs()).await()
                count += result.keys.size
                cursor = result
                iterations++
            } while (!cursor.isFinished && iterations < MAX_SCAN_ITERATIONS)
            count
        }

    override suspend fun put(
        key: K,
        value: V,
        ttl: Duration?,
    ): PutResult {
        validateOptionalTtl(ttl)?.let { return PutResult.Failed(it) }
        val ttlMs = ttl?.inWholeMilliseconds
        if (ttlMs != null && ttlMs < 1L) {
            return PutResult.Failed(
                RegistryError.InvalidTtl("Distributed TTL must be >= 1ms, got: $ttl"),
            )
        }
        val rk = codec.encode(key)
        val serialized = valueSerializer.serialize(value)

        val existed =
            withTimeout(timeoutMs) {
                commands
                    .eval<Long>(
                        PUT_SCRIPT,
                        ScriptOutputType.INTEGER,
                        arrayOf(rk),
                        *RedisKeyCodec.luaArgs(serialized, ttlMs),
                    ).await()
            }

        return if (existed == 1L) PutResult.Updated else PutResult.Created
    }

    override suspend fun evict(key: K): EvictResult {
        val removed =
            withTimeout(timeoutMs) {
                commands.del(codec.encode(key)).await()
            }
        return if (removed > 0) EvictResult.Evicted else EvictResult.NotFound
    }

    override suspend fun keysPage(
        cursor: String?,
        limit: Int,
    ): RegistryPage<K> =
        withTimeout(timeoutMs) {
            val safeLimit = limit.coerceIn(1, MAX_PAGE_SIZE)
            val scanCursor = ScanCursor.of(cursor ?: "0")
            val args = ScanArgs.Builder.matches(codec.scanPattern()).limit(safeLimit.toLong())
            val result = commands.scan(scanCursor, args).await()
            val keys =
                result.keys.map { redisKeyBytes ->
                    keySerializer.deserialize(codec.stripPrefix(redisKeyBytes))
                }
            RegistryPage(
                items = keys,
                nextCursor = if (result.isFinished) null else result.cursor,
            )
        }

    override suspend fun setTTL(
        key: K,
        ttl: Duration,
    ): SetTtlResult {
        validateTtl(ttl)?.let { return SetTtlResult.Failed(it) }
        val ttlMs = ttl.inWholeMilliseconds
        if (ttlMs < 1L) {
            return SetTtlResult.Failed(
                RegistryError.InvalidTtl("Distributed TTL must be >= 1ms, got: $ttl"),
            )
        }
        val applied =
            withTimeout(timeoutMs) {
                commands.pexpire(codec.encode(key), ttlMs).await()
            }
        return if (applied) SetTtlResult.Applied else SetTtlResult.KeyNotFound
    }
}
