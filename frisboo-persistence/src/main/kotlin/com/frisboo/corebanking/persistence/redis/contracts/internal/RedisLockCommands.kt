package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult

internal interface RedisLockCommands {
    suspend fun acquireLock(key: ByteArray, lock: ByteArray, ttlMs: Long): RedisLockAcquireResult
    suspend fun releaseLock(key: ByteArray, lock: ByteArray): RedisLockReleaseResult
}
