package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisCompareAndSetResult {
    public data object Success : RedisCompareAndSetResult
    public data object Mismatch : RedisCompareAndSetResult
    public data object KeyNotFound : RedisCompareAndSetResult
    public data class Failed(val message: String) : RedisCompareAndSetResult

    public companion object
}
