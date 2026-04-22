package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult

internal interface RedisAtomicCommands {
    suspend fun compareAndSet(key: ByteArray, expected: ByteArray?, value: ByteArray): Long?
    suspend fun compareAndSet(
        key: ByteArray,
        expected: ByteArray?,
        value: ByteArray,
        ttlMs: Long,
    ): Long?

    suspend fun increment(key: ByteArray, delta: Long): Long?
    suspend fun decrement(key: ByteArray, delta: Long): Long?
}
