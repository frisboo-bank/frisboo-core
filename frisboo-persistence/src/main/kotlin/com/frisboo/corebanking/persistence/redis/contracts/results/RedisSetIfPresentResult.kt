package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisSetIfPresentResult {
    public data class Success(val previous: ByteArray) : RedisSetIfPresentResult
    public data object KeyNotFound : RedisSetIfPresentResult
    public data class Failed(val message: String) : RedisSetIfPresentResult

    public companion object
}
