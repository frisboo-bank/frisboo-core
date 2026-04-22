package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisPingResult

internal interface RedisReadCommands {
    suspend fun ping(): RedisPingResult
    suspend fun get(key: ByteArray): RedisGetResult
}
