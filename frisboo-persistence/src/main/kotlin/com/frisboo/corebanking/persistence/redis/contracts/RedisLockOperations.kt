package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult

public interface RedisLockOperations<K: Any, V: Any> {
    public suspend fun set(key: K, value: V, lock: ByteArray): Either<PersistenceError, RedisSetResult>
    public suspend fun set(key: K, value: V, ttlMs: Long, lock: ByteArray): Either<PersistenceError, RedisSetResult>
    public suspend fun acquireLock(key: K): Either<PersistenceError, RedisLockAcquireResult>
    public suspend fun releaseLock(key: K, lock: ByteArray): Either<PersistenceError, RedisLockReleaseResult>
}
