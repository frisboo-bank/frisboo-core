package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult

internal fun RedisSetResult.Companion.fromLettuceResult(result: ByteArray?): RedisSetResult =
    when (result) {
        null -> RedisSetResult.Failed("Unexpected null result from set")
        else -> RedisSetResult.Success(result)
    }
