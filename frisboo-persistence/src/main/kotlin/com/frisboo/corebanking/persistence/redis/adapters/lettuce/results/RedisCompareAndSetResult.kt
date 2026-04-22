package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisCompareAndSetResult

internal fun RedisCompareAndSetResult.Companion.fromLettuceResult(result: Long?): RedisCompareAndSetResult =
    when (result) {
        1L -> RedisCompareAndSetResult.Success
        -1L -> RedisCompareAndSetResult.KeyNotFound
        -2L -> RedisCompareAndSetResult.Mismatch
        else -> RedisCompareAndSetResult.Failed("Unexpected result from compare and set: $result")
    }
