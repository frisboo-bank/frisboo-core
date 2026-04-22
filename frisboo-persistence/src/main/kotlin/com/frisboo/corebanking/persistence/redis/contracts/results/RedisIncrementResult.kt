package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisIncrementResult {
    public data class Success(val value: Long) : RedisIncrementResult
    public data class Failed(val message: String) : RedisIncrementResult

    public companion object
}
