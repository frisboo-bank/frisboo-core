package com.frisboo.corebanking.persistence.redis.contracts.internal

internal interface RedisConnection {
    suspend fun <T> withCommands(block: suspend (RedisCommands) -> T): T
}
