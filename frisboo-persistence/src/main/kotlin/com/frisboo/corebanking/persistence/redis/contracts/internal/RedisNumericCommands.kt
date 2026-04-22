package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDecrementResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisIncrementResult

internal interface RedisNumericCommands {
    suspend fun increment(key: ByteArray, delta: Long): RedisIncrementResult
    suspend fun decrement(key: ByteArray, delta: Long): Long?
}
