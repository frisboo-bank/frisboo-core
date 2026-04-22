package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisSetResult<out V> {

    public val previous: V?

    public data class Success<V>(
        override val previous: V?,
    ) : RedisSetResult<V>

}
