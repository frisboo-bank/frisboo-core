package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisGetResult<out V> {

    public val value: V?

    public data class Found<V>(
        override val value: V,
    ) : RedisGetResult<V>

    public data object NotFound : RedisGetResult<Nothing> {
        override val value: Nothing? = null
    }
}
