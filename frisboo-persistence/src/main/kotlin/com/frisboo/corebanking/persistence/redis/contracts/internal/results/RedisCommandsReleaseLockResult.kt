package com.frisboo.corebanking.persistence.redis.contracts.internal.results

internal interface RedisCommandsReleaseLockResult {
    data object Released : RedisCommandsReleaseLockResult
    data object NotHeld : RedisCommandsReleaseLockResult
    data object Mismatch : RedisCommandsReleaseLockResult
}
