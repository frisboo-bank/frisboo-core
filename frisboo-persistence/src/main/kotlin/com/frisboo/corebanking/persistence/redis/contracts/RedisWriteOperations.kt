package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult

public interface RedisWriteOperations<K : Any, V : Any> {
    public suspend fun set(key: K, value: V): Either<PersistenceError, RedisSetResult>
    public suspend fun set(key: K, value: V, ttlMs: Long): Either<PersistenceError, RedisSetResult>
    public suspend fun del(key: K): Either<PersistenceError, Boolean>
    public suspend fun pexpire(key: K, ttlMs: Long): Either<PersistenceError, Boolean>
}
