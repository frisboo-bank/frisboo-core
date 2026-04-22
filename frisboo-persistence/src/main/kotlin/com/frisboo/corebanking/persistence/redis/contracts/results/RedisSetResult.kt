package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisSetResult {
    public data class Success(val previous: ByteArray?) : RedisSetResult
    public data class Failed(val message: String) : RedisSetResult

    public companion object
}
