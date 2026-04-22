package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisLockAcquireResult {
    public data object Acquired : RedisLockAcquireResult
    public data object AlreadyExists : RedisLockAcquireResult
    public data class Failed(val message: String) : RedisLockAcquireResult

    public companion object
}
