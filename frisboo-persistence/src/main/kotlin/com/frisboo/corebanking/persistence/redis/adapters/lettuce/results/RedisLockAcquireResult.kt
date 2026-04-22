package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockAcquireResult

internal fun RedisLockAcquireResult.Companion.fromLettuceResult(result: Long?): RedisLockAcquireResult = when (result) {
    1L -> RedisLockAcquireResult.Acquired
    0L -> RedisLockAcquireResult.AlreadyExists
    else -> RedisLockAcquireResult.Failed("Unexpected result from acquire lock: $result")
}
