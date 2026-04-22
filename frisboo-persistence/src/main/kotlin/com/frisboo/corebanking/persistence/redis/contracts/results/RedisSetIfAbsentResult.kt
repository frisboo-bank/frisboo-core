package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisSetIfAbsentResult {
    public data object Success : RedisSetIfAbsentResult
    public data object AlreadyExists : RedisSetIfAbsentResult
    public data class Failed(val message: String) : RedisSetIfAbsentResult

    public companion object
}
