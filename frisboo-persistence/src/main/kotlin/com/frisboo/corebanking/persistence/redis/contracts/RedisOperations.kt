package com.frisboo.corebanking.persistence.redis.contracts

import arrow.core.Either
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import com.frisboo.corebanking.persistence.redis.models.RedisScanPage
import kotlin.time.Duration

public interface RedisOperations<K : Any, V : Any> {
    public suspend fun ping(): Either<PersistenceError, String>
    public suspend fun set(key: K, value: V): Either<PersistenceError, V?>
    public suspend fun set(key: K, value: V, ttlMs: Long): Either<PersistenceError, V?>
    public suspend fun get(key: K): Either<PersistenceError, V?>
    public suspend fun del(key: K): Either<PersistenceError, Boolean>
    public suspend fun exists(key: K): Either<PersistenceError, Boolean>
    public suspend fun pexpire(key: K, ttlMs: Long): Either<PersistenceError, Boolean>
    public suspend fun scanCount(batchSize: Long? = null): Either<PersistenceError, Long>
    public suspend fun scanPage(cursor: RedisScanCursor?, limit: Int): Either<PersistenceError, RedisScanPage<K>>
    // Atomic operations for locking
    public suspend fun set(key: K, value: V, lock: ByteArray): Either<PersistenceError, V?>
    public suspend fun set(key: K, value: V, ttlMs: Long, lock: ByteArray): Either<PersistenceError, V?>
    public suspend fun acquireLock(key: K): Either<PersistenceError, ByteArray>
    public suspend fun releaseLock(key: K, lock: ByteArray): Either<PersistenceError, Unit>
}
