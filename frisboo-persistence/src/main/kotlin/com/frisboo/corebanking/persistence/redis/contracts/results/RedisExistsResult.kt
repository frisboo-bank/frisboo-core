package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisExistsResult {
    public data object Exists : RedisExistsResult

    public data object NotFound : RedisExistsResult
}
