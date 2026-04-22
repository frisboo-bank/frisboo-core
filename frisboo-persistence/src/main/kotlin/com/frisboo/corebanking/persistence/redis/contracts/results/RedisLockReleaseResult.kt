package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisLockReleaseResult {
    public data object Released : RedisLockReleaseResult
    public data object Mismatch : RedisLockReleaseResult
    public data class Failed(val message: String) : RedisLockReleaseResult

    public companion object
}
