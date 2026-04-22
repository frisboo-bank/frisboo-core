package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisDecrementResult {
    public data class Success(val value: Long) : RedisDecrementResult
    public data class Failed(val message: String) : RedisDecrementResult

    public companion object
}
