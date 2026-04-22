package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.persistence.redis.models.RedisScanPage

public interface RedisReadOperations<K : Any, V : Any> {
    public suspend fun ping(): Either<PersistenceError, Boolean>
    public suspend fun get(key: K): Either<PersistenceError, RedisGetResult<V?>>
    public suspend fun exists(key: K): Either<PersistenceError, Boolean>
    public suspend fun scanCount(batchSize: Long? = null): Either<PersistenceError, Long>
    public suspend fun scanPage(cursor: RedisScanCursor?, limit: Int): Either<PersistenceError, RedisScanPage<K>>
}
