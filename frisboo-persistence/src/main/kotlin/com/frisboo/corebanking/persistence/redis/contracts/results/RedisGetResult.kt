package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisGetResult {
    public data class Success(val value: ByteArray) : RedisGetResult
    public data object KeyNotFound : RedisGetResult
    public data class Failed(val message: String) : RedisGetResult

    public companion object
}
