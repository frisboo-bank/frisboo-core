package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisLockReleaseResult

internal fun RedisLockReleaseResult.Companion.fromLettuceResult(result: Long?): RedisLockReleaseResult = when (result) {
    1L -> RedisLockReleaseResult.Released
    0L -> RedisLockReleaseResult.Mismatch
    else -> RedisLockReleaseResult.Failed("Unexpected result from release lock script: $result")
}
