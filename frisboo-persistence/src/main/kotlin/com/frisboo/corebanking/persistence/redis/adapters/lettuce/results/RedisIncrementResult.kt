package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisIncrementResult

internal fun RedisIncrementResult.Companion.fromLettuceResult(result: Long?): RedisIncrementResult = when (result) {
    null -> RedisIncrementResult.Failed("Unexpected null result from increment")
    else -> RedisIncrementResult.Success(result)
}
