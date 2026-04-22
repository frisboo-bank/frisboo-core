package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDecrementResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisIncrementResult

public interface RedisAtomicOperations<K : Any, V : Any> {
    public suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
        ttlMs: Long? = null,
    ): Either<PersistenceError, RedisCompareAndSetResult>

    public suspend fun increment(key: K, delta: Long = 1): Either<PersistenceError, RedisIncrementResult>
    public suspend fun decrement(key: K, delta: Long = 1): Either<PersistenceError, RedisDecrementResult>
}
