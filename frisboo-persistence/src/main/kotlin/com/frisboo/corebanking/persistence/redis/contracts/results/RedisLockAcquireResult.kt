package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisLockAcquireResult {
    public data class Acquired(val token: ByteArray) : RedisLockAcquireResult
    public data object AlreadyHeld : RedisLockAcquireResult

    public companion object
}
