package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetWithLockResult

internal interface RedisLockCommands {
    suspend fun acquireLock(key: ByteArray, lock: ByteArray, ttlMs: Long): Long?
    suspend fun releaseLock(key: ByteArray, lock: ByteArray): Long?
    suspend fun setWithLock(
        lockKey: ByteArray,
        dataKey: ByteArray,
        lock: ByteArray,
        value: ByteArray,
    ): Long?

    suspend fun setWithLock(
        lockKey: ByteArray,
        dataKey: ByteArray,
        lock: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): Long?
}
