package com.frisboo.corebanking.persistence.redis.contracts.internal

internal interface RedisGetCommands {
    suspend fun get(key: ByteArray): ByteArray?
}
