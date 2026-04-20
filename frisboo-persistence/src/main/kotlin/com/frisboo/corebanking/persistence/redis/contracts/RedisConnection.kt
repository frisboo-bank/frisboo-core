package com.frisboo.corebanking.persistence.redis.contracts

public interface RedisConnection {
    public suspend fun <T> withCommands(block: suspend (RedisCommands) -> T): T
}
