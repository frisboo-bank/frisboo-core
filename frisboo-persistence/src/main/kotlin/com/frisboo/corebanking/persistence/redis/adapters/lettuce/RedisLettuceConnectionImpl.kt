package com.frisboo.corebanking.persistence.redis.adapters.lettuce

import com.frisboo.corebanking.persistence.redis.contracts.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.RedisConnection
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.support.AsyncPool
import kotlinx.coroutines.future.await

@OptIn(ExperimentalLettuceCoroutinesApi::class)
public class RedisLettuceConnectionImpl(
    private val connectionPool: AsyncPool<StatefulRedisConnection<ByteArray, ByteArray>>,
) : RedisConnection {

    override suspend fun <T> withCommands(block: suspend (RedisCommands) -> T): T {
        val connection = connectionPool.acquire().await()
        try {
            return block(RedisLettuceCommandsImpl(connection.coroutines()))
        } finally {
            connectionPool.release(connection).await()
        }
    }
}
