package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfPresentResult

internal fun RedisSetIfPresentResult.Companion.fromLettuceResult(result: ByteArray?): RedisSetIfPresentResult =
    when {
        result == null -> RedisSetIfPresentResult.Failed("Failed to set existing key")
        else -> RedisSetIfPresentResult.Success(ByteArray(0))
    }
