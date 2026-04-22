package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDecrementResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisIncrementResult

internal interface RedisAtomicCommands {
    suspend fun compareAndSet(key: ByteArray, expected: ByteArray, value: ByteArray): RedisCompareAndSetResult
    suspend fun compareAndSet(
        key: ByteArray,
        expected: ByteArray,
        value: ByteArray,
        ttlMs: Long,
    ): RedisCompareAndSetResult

    suspend fun increment(key: ByteArray, delta: Long): RedisIncrementResult
    suspend fun decrement(key: ByteArray, delta: Long): RedisDecrementResult
}
