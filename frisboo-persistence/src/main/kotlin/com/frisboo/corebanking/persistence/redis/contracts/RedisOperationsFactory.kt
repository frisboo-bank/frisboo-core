package com.frisboo.corebanking.persistence.redis.contracts

import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer

public interface RedisOperationsFactory {

    public suspend fun <K : Any, V : Any> create(
        prefix: String,
        keySerializer: PersistenceSerializer<K>,
        valueSerializer: PersistenceSerializer<V>,
    ): RedisOperations<K, V>
}
