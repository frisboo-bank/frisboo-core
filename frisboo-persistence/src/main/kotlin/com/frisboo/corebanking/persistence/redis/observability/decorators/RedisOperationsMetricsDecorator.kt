package com.frisboo.corebanking.persistence.redis.observability.decorators

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
import com.frisboo.corebanking.persistence.redis.contracts.observability.RedisOperationsMetrics
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDeleteResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisExistsResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisScanPageResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.persistence.redis.models.observability.RedisOperationsOperation

internal class RedisOperationsMetricsDecorator<K : Any, V : Any>(
    private val delegate: RedisOperations<K, V>,
    private val metrics: RedisOperationsMetrics,
) : RedisOperations<K, V> {
    override suspend fun ping(): Either<PersistenceError, Boolean> =
        meter(RedisOperationsOperation.PING) { delegate.ping() }

    override suspend fun get(key: K): Either<PersistenceError, RedisGetResult<V?>> =
        meter(RedisOperationsOperation.GET) { delegate.get(key) }

    override suspend fun exists(key: K): Either<PersistenceError, RedisExistsResult> =
        meter(RedisOperationsOperation.EXISTS) { delegate.exists(key) }

    override suspend fun scanCount(batchSize: Long?): Either<PersistenceError, Long> =
        meter(RedisOperationsOperation.SCAN_COUNT) { delegate.scanCount(batchSize) }

    override suspend fun scanPage(
        cursor: RedisScanCursor?,
        limit: Int,
    ): Either<PersistenceError, RedisScanPageResult<K>> =
        meter(RedisOperationsOperation.SCAN_PAGE) { delegate.scanPage(cursor, limit) }

    override suspend fun set(
        key: K,
        value: V,
    ): Either<PersistenceError, RedisSetResult<V?>> = meter(RedisOperationsOperation.SET) { delegate.set(key, value) }

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisSetResult<V?>> =
        meter(RedisOperationsOperation.SET_WITH_TTL) { delegate.set(key, value, ttlMs) }

    override suspend fun del(key: K): Either<PersistenceError, RedisDeleteResult<V?>> =
        meter(RedisOperationsOperation.DEL) { delegate.del(key) }

    override suspend fun pexpire(
        key: K,
        ttlMs: Long,
    ): Either<PersistenceError, Boolean> = meter(RedisOperationsOperation.PEXPIRE) { delegate.pexpire(key, ttlMs) }

    override suspend fun set(
        key: K,
        value: V,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> =
        meter(RedisOperationsOperation.SET_WITH_LOCK) { delegate.set(key, value, lock) }

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> =
        meter(RedisOperationsOperation.SET_WITH_TTL_AND_LOCK) { delegate.set(key, value, ttlMs, lock) }

    override suspend fun acquireLock(
        key: K,
        ttlMs: Long,
    ): Either<PersistenceError, RedisLockAcquireResult> = meter(RedisOperationsOperation.ACQUIRE_LOCK) {
        delegate.acquireLock(key, ttlMs)
    }.onRight {
        if (it is RedisLockAcquireResult.AlreadyHeld) metrics.recordAcquireLockConflict()
    }

    override suspend fun releaseLock(
        key: K,
        lock: ByteArray,
    ): Either<PersistenceError, RedisLockReleaseResult> =
        meter(RedisOperationsOperation.RELEASE_LOCK) { delegate.releaseLock(key, lock) }

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> =
        meter(RedisOperationsOperation.COMPARE_AND_SET) { delegate.compareAndSet(key, expected, value) }.onRight {
            if (it is RedisCompareAndSetResult.Mismatch) metrics.recordCompareAndSetConflict()
        }

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> =
        meter(RedisOperationsOperation.COMPARE_AND_SET_WITH_TTL) {
            delegate.compareAndSet(key, expected, value, ttlMs)
        }.onRight {
            if (it is RedisCompareAndSetResult.Mismatch) metrics.recordCompareAndSetConflict()
        }

    override suspend fun increment(
        key: K,
        delta: Long,
    ): Either<PersistenceError, Long> = meter(RedisOperationsOperation.INCREMENT) { delegate.increment(key, delta) }

    override suspend fun decrement(
        key: K,
        delta: Long,
    ): Either<PersistenceError, Long> = meter(RedisOperationsOperation.DECREMENT) { delegate.decrement(key, delta) }

    private suspend fun <T> meter(
        operationName: RedisOperationsOperation,
        block: suspend () -> Either<PersistenceError, T>,
    ): Either<PersistenceError, T> {
        val startTime = System.nanoTime()
        val result = block()
        val durationMs = (System.nanoTime() - startTime) / 1_000_000.0

        metrics.recordOperationLatency(operationName, durationMs, result.isRight())
        return result
    }
}
