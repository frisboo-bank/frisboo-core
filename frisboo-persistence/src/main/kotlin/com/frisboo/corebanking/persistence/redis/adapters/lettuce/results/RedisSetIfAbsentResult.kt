package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfAbsentResult

internal fun RedisSetIfAbsentResult.Companion.fromLettuceResult(result: String?): RedisSetIfAbsentResult =
    when (result) {
        null -> RedisSetIfAbsentResult.AlreadyExists
        "OK" -> RedisSetIfAbsentResult.Success
        else -> RedisSetIfAbsentResult.Failed("Unexpected result from set if absent: $result")
    }
