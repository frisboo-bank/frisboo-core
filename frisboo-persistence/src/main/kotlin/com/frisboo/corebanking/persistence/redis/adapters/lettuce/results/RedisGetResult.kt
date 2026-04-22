package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisGetResult

internal fun RedisGetResult.Companion.fromLettuceResult(result: ByteArray?): RedisGetResult =
    when {
        result == null -> RedisGetResult.KeyNotFound
        else -> RedisGetResult.Success(result)
    }
