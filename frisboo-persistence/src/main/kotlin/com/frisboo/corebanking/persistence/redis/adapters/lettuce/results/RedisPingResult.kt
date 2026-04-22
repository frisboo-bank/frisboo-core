package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisPingResult

internal fun RedisPingResult.Companion.fromLettuceResult(result: String?): RedisPingResult =
    when (result) {
        "PONG" -> RedisPingResult.Success
        else -> RedisPingResult.Failed("Unexpected ping response: $result")
    }
