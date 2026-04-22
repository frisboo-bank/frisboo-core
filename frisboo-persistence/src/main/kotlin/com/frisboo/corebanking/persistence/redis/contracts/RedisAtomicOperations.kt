package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
// increment/decrement return Long via Either; removed RedisIncrementResult/RedisDecrementResult

public interface RedisAtomicOperations<K : Any, V : Any> {
    public suspend fun compareAndSet(
        key: K,
        expected: V?,
        value: V,
    ): Either<PersistenceError, RedisCompareAndSetResult>
    public suspend fun increment(key: K, delta: Long = 1): Either<PersistenceError, Long>
    public suspend fun decrement(key: K, delta: Long = 1): Either<PersistenceError, Long>
}
