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
package com.frisboo.corebanking.persistence.redis

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_LOCK_SIZE
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_MAX_SCAN_ITERATIONS
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_SCAN_BATCH_SIZE
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisConnection
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsAcquireLockResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsReleaseLockResult
import com.frisboo.corebanking.persistence.redis.contracts.internal.results.RedisCommandsSetWithLockResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDeleteResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisExistsResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisScanPageResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult
import com.frisboo.corebanking.persistence.redis.models.RedisCodec
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import java.util.concurrent.ThreadLocalRandom
import kotlin.time.Duration

internal class RedisOperationsImpl<K : Any, V : Any>(
    private val connection: RedisConnection,
    private val operationTimeout: Duration,
    private val codec: RedisCodec<K, V>,
) : RedisOperations<K, V> {
    init {
        require(operationTimeout.isPositive()) { "operationTimeout must be positive" }
    }

    override suspend fun ping(): Either<PersistenceError, Boolean> = doExecuteOperation("PING") { it.ping() }

    override suspend fun set(key: K, value: V): Either<PersistenceError, RedisSetResult<V?>> = doSet(key, value, null)

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisSetResult<V?>> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }
        return doSet(key, value, ttlMs)
    }

    override suspend fun get(key: K): Either<PersistenceError, RedisGetResult<V?>> = either {
        val k = serializeKey(key).bind()

        when (val rawResult = doExecuteOperation("GET") { it.get(key = k) }.bind()) {
            null -> RedisGetResult.NotFound
            else -> RedisGetResult.Found(deserializeValue(rawResult).bind())
        }
    }

    override suspend fun del(key: K): Either<PersistenceError, RedisDeleteResult<V?>> = either {
        val k = serializeKey(key).bind()

        when (val rawResult = doExecuteOperation("DELETE") { it.del(key = k) }.bind()) {
            null -> RedisDeleteResult.NotFound
            else -> RedisDeleteResult.Deleted(deserializeValue(rawResult).bind())
        }
    }

    override suspend fun exists(key: K): Either<PersistenceError, RedisExistsResult> = either {
        val k = serializeKey(key).bind()

        when (doExecuteOperation("EXISTS") { it.exists(key = k) }.bind()) {
            true -> RedisExistsResult.Exists
            false -> RedisExistsResult.NotFound
        }
    }

    override suspend fun pexpire(key: K, ttlMs: Long): Either<PersistenceError, Boolean> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }

        val k = serializeKey(key).bind()

        doExecuteOperation("PEXPIRE") { it.pexpire(key = k, ttlMs = ttlMs) }.bind()
    }

    override suspend fun scanCount(batchSize: Long?): Either<PersistenceError, Long> = either {
        var totalCount = 0L
        val effectiveBatchSize = batchSize?.coerceAtLeast(1L) ?: REDIS_DEFAULT_SCAN_BATCH_SIZE

        var cursor = RedisScanCursor.INITIAL
        var iterations = 0L

        do {
            val (nextCursor, keys) = doExecuteOperation("SCAN_COUNT") {
                it.scan(cursor = cursor, count = effectiveBatchSize, pattern = scanPattern())
            }.bind() ?: raise(PersistenceError.OperationFailed("SCAN returned null during count"))

            totalCount += keys?.size ?: 0
            iterations++

            ensure(iterations <= REDIS_DEFAULT_MAX_SCAN_ITERATIONS) {
                PersistenceError.OperationFailed("SCAN exceeded max iterations ($REDIS_DEFAULT_MAX_SCAN_ITERATIONS)")
            }

            cursor = nextCursor
        } while (!cursor.isFinished)
        totalCount
    }

    override suspend fun scanPage(
        cursor: RedisScanCursor?,
        limit: Int,
    ): Either<PersistenceError, RedisScanPageResult.Success<K>> = either {
        ensure(limit > 0) { PersistenceError.OperationFailed("limit must be positive") }

        val scanCursor = cursor ?: RedisScanCursor.INITIAL

        if (scanCursor.isFinished) {
            return@either RedisScanPageResult.Success(emptyList(), RedisScanCursor.FINISHED)
        }

        val (nextCursor, rawKeys) = doExecuteOperation("SCAN") {
            it.scan(cursor = scanCursor, count = limit.toLong(), pattern = scanPattern())
        }.bind() ?: raise(PersistenceError.OperationFailed("SCAN returned null"))

        val keys = rawKeys?.filterNotNull()?.map { deserializeKey(it).bind() }

        RedisScanPageResult.Success(
            keys = keys ?: emptyList(),
            nextCursor = nextCursor,
        )
    }

    override suspend fun acquireLock(key: K, ttlMs: Long): Either<PersistenceError, RedisLockAcquireResult> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }

        val lockKey = serializeLockKey(key).bind()
        val lockValue = ByteArray(REDIS_DEFAULT_LOCK_SIZE).also { ThreadLocalRandom.current().nextBytes(it) }

        val result = doExecuteOperation("ACQUIRE_LOCK") {
            it.acquireLock(key = lockKey, lock = lockValue, ttlMs = ttlMs)
        }.bind()

        when (result) {
            is RedisCommandsAcquireLockResult.Acquired -> RedisLockAcquireResult.Acquired(result.token)
            is RedisCommandsAcquireLockResult.AlreadyHeld -> RedisLockAcquireResult.AlreadyHeld
            else -> raise(PersistenceError.OperationFailed("acquireLock returned unexpected result: $result"))
        }
    }

    override suspend fun releaseLock(
        key: K,
        lock: ByteArray,
    ): Either<PersistenceError, RedisLockReleaseResult> = either {
        val lockKey = serializeLockKey(key).bind()

        val result = doExecuteOperation("RELEASE_LOCK") {
            it.releaseLock(key = lockKey, lock = lock)
        }.bind()

        when (result) {
            is RedisCommandsReleaseLockResult.Released -> RedisLockReleaseResult.Released
            is RedisCommandsReleaseLockResult.Mismatch -> RedisLockReleaseResult.Mismatch
            is RedisCommandsReleaseLockResult.NotHeld -> RedisLockReleaseResult.NotHeld
            else -> raise(PersistenceError.OperationFailed("releaseLock returned unexpected result: $result"))
        }
    }

    override suspend fun set(
        key: K,
        value: V,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> = doSetWithLock(key, value, null, lock)

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }
        return doSetWithLock(key, value, ttlMs, lock)
    }

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> = doCompareAndSet(key, expected, value, null)

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> = either {
        ensure(ttlMs > 0) { PersistenceError.OperationFailed("ttlMs must be positive") }
        return doCompareAndSet(key, expected, value, ttlMs)
    }

    override suspend fun increment(key: K, delta: Long): Either<PersistenceError, Long> = either {
        val k = serializeKey(key).bind()

        val result = doExecuteOperation("INCREMENTBY") { it.increment(key = k, delta = delta) }.bind()
        result ?: raise(PersistenceError.OperationFailed("INCRBY returned null"))
    }

    override suspend fun decrement(key: K, delta: Long): Either<PersistenceError, Long> = either {
        val k = serializeKey(key).bind()

        val result = doExecuteOperation("DECREMENTBY") { it.decrement(key = k, delta = delta) }.bind()
        result ?: raise(PersistenceError.OperationFailed("DECRBY returned null"))
    }

    // internal methods

    private suspend fun doSet(
        key: K,
        value: V,
        ttlMs: Long?,
    ): Either<PersistenceError, RedisSetResult<V?>> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()

        when (val rawResult = doExecuteOperation("SET") { it.setGet(key = k, value = v, ttlMs = ttlMs ?: 0L) }.bind()) {
            null -> RedisSetResult.Success(null)
            else -> RedisSetResult.Success(deserializeValue(rawResult).bind())
        }
    }

    private suspend fun doSetWithLock(
        key: K,
        value: V,
        ttlMs: Long?,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()
        val lockKey = serializeLockKey(key).bind()

        val result = doExecuteOperation("SET_WITH_LOCK") {
            it.setWithLock(lockKey = lockKey, dataKey = k, lock = lock, value = v, ttlMs = ttlMs ?: 0L)
        }.bind()

        when (result) {
            is RedisCommandsSetWithLockResult.Success -> {
                val previousValue = result.previousValue?.let { deserializeValue(it).bind() }
                RedisSetResult.Success(previousValue)
            }

            is RedisCommandsSetWithLockResult.LockMismatch -> RedisSetResult.LockNotHeld
            is RedisCommandsSetWithLockResult.LockMissing -> RedisSetResult.LockMissing
            else -> raise(PersistenceError.OperationFailed("set with lock returned unexpected result: $result"))
        }
    }

    private suspend fun doCompareAndSet(
        key: K,
        expected: V?,
        value: V,
        ttlMs: Long?,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> = either {
        val k = serializeKey(key).bind()
        val v = serializeValue(value).bind()
        val expectedBytes = expected?.let { serializeValue(it).bind() }

        val result = doExecuteOperation("EVAL compareAndSet") {
            it.compareAndSet(key = k, expected = expectedBytes, value = v, ttlMs = ttlMs ?: 0L)
        }.bind()

        when (result) {
            is RedisCommandsCompareAndSetResult.Success -> {
                val previousValue = result.previousValue?.let { deserializeValue(it).bind() }
                RedisCompareAndSetResult.Success(previousValue)
            }

            is RedisCommandsCompareAndSetResult.KeyNotFound -> RedisCompareAndSetResult.KeyNotFound
            is RedisCommandsCompareAndSetResult.Mismatch -> RedisCompareAndSetResult.Mismatch
            else -> raise(PersistenceError.OperationFailed("compareAndSet returned unexpected result: $result"))
        }
    }

    private suspend fun <T> doExecuteOperation(
        operationName: String,
        block: suspend (command: RedisCommands) -> T,
    ): Either<PersistenceError, T> = executeWithTimeout(
        timeout = operationTimeout,
        block = { connection.withCommands(block) },
        onTimeout = { duration, _ ->
            PersistenceError.OperationTimeout(
                "$operationName timed out after ${duration.inWholeMilliseconds} ms",
                duration = duration,
            )
        },
        onError = { e ->
            when (e) {
                is PersistenceError -> e
                else -> PersistenceError.OperationFailed("$operationName failed: ${e.message}", e)
            }
        },
    )

    private fun serializeKey(key: K): Either<PersistenceError, ByteArray> = codec.serializeKey(key)
    private fun deserializeKey(data: ByteArray): Either<PersistenceError, K> = codec.deserializeKey(data)

    private fun serializeValue(value: V): Either<PersistenceError, ByteArray> = codec.serializeValue(value)

    private fun deserializeValue(data: ByteArray): Either<PersistenceError, V> = codec.deserializeValue(data)

    private fun scanPattern(): ByteArray = codec.scanPattern
    private fun serializeLockKey(key: K): Either<PersistenceError, ByteArray> = codec.serializeLockKey(key)
}
