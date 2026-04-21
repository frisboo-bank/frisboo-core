package com.frisboo.corebanking.persistence.redis

import com.frisboo.corebanking.persistence.core.contracts.PersistenceSerializer
import com.frisboo.corebanking.persistence.redis.adapters.lettuce.RedisLettuceConnectionImpl
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_LOCK_TTL
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_OPERATION_TIMEOUT
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperations
import com.frisboo.corebanking.persistence.redis.contracts.RedisOperationsFactory
import com.frisboo.corebanking.persistence.redis.models.RedisCodec
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.support.AsyncPool
import kotlin.time.Duration

public class RedisOperationsFactoryImpl(
    private val connectionPool: AsyncPool<StatefulRedisConnection<ByteArray, ByteArray>>,
    private val operationTimeout: Duration = REDIS_DEFAULT_OPERATION_TIMEOUT,
    private val lockTtl: Duration = REDIS_DEFAULT_LOCK_TTL,
) : RedisOperationsFactory {
    private val sharedConnection by lazy { RedisLettuceConnectionImpl(connectionPool) }

    override suspend fun <K : Any, V : Any> create(
        prefix: String,
        keySerializer: PersistenceSerializer<K>,
        valueSerializer: PersistenceSerializer<V>,
    ): RedisOperations<K, V> {
        return RedisOperationsImpl(
            connection = sharedConnection,
            operationTimeout = operationTimeout,
            lockTtl = lockTtl,
            codec = RedisCodec(
                prefix = prefix,
                lockPrefix = "lock",
                keySerializer = keySerializer,
                valueSerializer = valueSerializer,
            ),
        )
    }
}
