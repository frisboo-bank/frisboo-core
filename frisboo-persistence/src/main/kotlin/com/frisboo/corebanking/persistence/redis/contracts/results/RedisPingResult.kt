package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisPingResult {
    public data object Success : RedisPingResult
    public data class Failed(val message: String) : RedisPingResult

    public companion object
}
