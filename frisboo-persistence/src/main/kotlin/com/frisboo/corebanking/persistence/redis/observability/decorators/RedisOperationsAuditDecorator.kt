package com.frisboo.corebanking.persistence.redis.observability.decorators

import arrow.core.Either
import com.frisboo.corebanking.observability.audit.contracts.ObservabilityAudit
import com.frisboo.corebanking.observability.tracer.contracts.ObservabilityTracer
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
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
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal class RedisOperationsAuditDecorator<K : Any, V : Any>(
    private val delegate: RedisOperations<K, V>,
    private val audit: ObservabilityAudit,
    private val trace: ObservabilityTracer,
    private val clock: Clock.System,
) : RedisOperations<K, V> {
    override suspend fun ping(): Either<PersistenceError, Boolean> =
        audit(RedisOperationsOperation.PING) { delegate.ping() }

    override suspend fun get(key: K): Either<PersistenceError, RedisGetResult<V?>> =
        audit(RedisOperationsOperation.GET, listOf(key)) { delegate.get(key) }

    override suspend fun exists(key: K): Either<PersistenceError, RedisExistsResult> =
        audit(RedisOperationsOperation.EXISTS, listOf(key)) { delegate.exists(key) }

    override suspend fun scanCount(batchSize: Long?): Either<PersistenceError, Long> =
        audit(RedisOperationsOperation.SCAN_COUNT, listOfNotNull(batchSize)) {
            delegate.scanCount(batchSize)
        }

    override suspend fun scanPage(
        cursor: RedisScanCursor?,
        limit: Int,
    ): Either<PersistenceError, RedisScanPageResult<K>> =
        audit(RedisOperationsOperation.SCAN_PAGE, listOfNotNull(cursor, limit)) {
            delegate.scanPage(
                cursor,
                limit,
            )
        }

    override suspend fun set(
        key: K,
        value: V,
    ): Either<PersistenceError, RedisSetResult<V?>> =
        audit(RedisOperationsOperation.SET, listOf(key)) { delegate.set(key, value) }

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisSetResult<V?>> = audit(RedisOperationsOperation.SET_WITH_TTL, listOf(key, ttlMs)) {
        delegate.set(
            key,
            value,
            ttlMs,
        )
    }

    override suspend fun del(key: K): Either<PersistenceError, RedisDeleteResult<V?>> =
        audit(RedisOperationsOperation.DEL, listOf(key)) { delegate.del(key) }

    override suspend fun pexpire(
        key: K,
        ttlMs: Long,
    ): Either<PersistenceError, Boolean> =
        audit(RedisOperationsOperation.PEXPIRE, listOf(key, ttlMs)) { delegate.pexpire(key, ttlMs) }

    override suspend fun set(
        key: K,
        value: V,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> =
        audit(RedisOperationsOperation.SET_WITH_LOCK, listOf(key)) { delegate.set(key, value, lock) }

    override suspend fun set(
        key: K,
        value: V,
        ttlMs: Long,
        lock: ByteArray,
    ): Either<PersistenceError, RedisSetResult<V?>> =
        audit(RedisOperationsOperation.SET_WITH_TTL_AND_LOCK, listOf(key, ttlMs)) {
            delegate.set(
                key,
                value,
                ttlMs,
                lock,
            )
        }

    override suspend fun acquireLock(
        key: K,
        ttlMs: Long,
    ): Either<PersistenceError, RedisLockAcquireResult> =
        audit(RedisOperationsOperation.ACQUIRE_LOCK, listOf(key, ttlMs)) {
            delegate.acquireLock(
                key,
                ttlMs,
            )
        }

    override suspend fun releaseLock(
        key: K,
        lock: ByteArray,
    ): Either<PersistenceError, RedisLockReleaseResult> =
        audit(RedisOperationsOperation.RELEASE_LOCK, listOf(key)) { delegate.releaseLock(key, lock) }

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> = audit(
        RedisOperationsOperation.COMPARE_AND_SET,
        listOf(key),
    ) { delegate.compareAndSet(key, expected, value) }

    override suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
        ttlMs: Long,
    ): Either<PersistenceError, RedisCompareAndSetResult<V?>> = audit(
        RedisOperationsOperation.COMPARE_AND_SET_WITH_TTL,
        listOf(key, ttlMs),
    ) { delegate.compareAndSet(key, expected, value) }

    override suspend fun increment(
        key: K,
        delta: Long,
    ): Either<PersistenceError, Long> =
        audit(RedisOperationsOperation.INCREMENT, listOf(key, delta)) { delegate.increment(key, delta) }

    override suspend fun decrement(
        key: K,
        delta: Long,
    ): Either<PersistenceError, Long> =
        audit(RedisOperationsOperation.DECREMENT, listOf(key, delta)) { delegate.decrement(key, delta) }

    private suspend fun <T> audit(
        operationName: RedisOperationsOperation,
        params: List<Any> = emptyList(),
        block: suspend () -> Either<PersistenceError, T>,
    ): Either<PersistenceError, T> {
        val result = block()

//        audit.record(
//            entry = ObservabilityAuditEntry.create(
//                action = "redis.${operationName.name.lowercase()}",
//                details = params.mapIndexed { index, param -> "param.$index" to param.toString() }.toMap(),
//                outcome = when (result) {
//                    is Either.Left -> ObservabilityAuditOutcome.FAILURE
//                    is Either.Right -> ObservabilityAuditOutcome.SUCCESS
//                },
//            ),
//        ))
//        audit.record(
//            entry = ObservabilityAuditEntry.(
//                auditId = TODO(),
//                principalId = TODO(),
//                principalType = TODO(),
//                action = TODO(),
//                resourceType = TODO(),
//                resourceId = TODO(),
//                outcome = TODO(),
//                correlationId = TODO(),
//                traceId = TODO(),
//                clientIp = TODO(),
//                details = params.mapIndexed { index, param -> "param.$index" to param.toString() }.toMap(),
//                occurredAt = clock.now(),
//            ),
//        )

        return result
    }
}
