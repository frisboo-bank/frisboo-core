package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisSetWithLockResult {
    public data object Success : RedisSetWithLockResult

    public data object LockMissing : RedisSetWithLockResult

    public data object LockMismatch : RedisSetWithLockResult
}
