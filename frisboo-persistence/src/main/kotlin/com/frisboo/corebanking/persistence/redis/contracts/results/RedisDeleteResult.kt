package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisDeleteResult {

    public data object Deleted : RedisDeleteResult

    public data object KeyNotFound : RedisDeleteResult
}
