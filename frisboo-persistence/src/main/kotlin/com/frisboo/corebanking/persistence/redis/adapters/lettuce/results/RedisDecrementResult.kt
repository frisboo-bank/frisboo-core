package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDecrementResult

internal fun RedisDecrementResult.Companion.fromLettuceResult(result: Long?): RedisDecrementResult = when (result) {
    null -> RedisDecrementResult.Failed("Unexpected null result from decrement")
    else -> RedisDecrementResult.Success(result)
}
