/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
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
) : RedisOperationsFactory {
    private val sharedConnection by lazy { RedisLettuceConnectionImpl(connectionPool, operationTimeout) }

    override suspend fun <K : Any, V : Any> create(
        prefix: String,
        keySerializer: PersistenceSerializer<K>,
        valueSerializer: PersistenceSerializer<V>,
    ): RedisOperations<K, V> =
        RedisOperationsImpl(
            connection = sharedConnection,
            operationTimeout = operationTimeout,
            codec =
                RedisCodec(
                    prefix = prefix,
                    lockPrefix = "lock",
                    keySerializer = keySerializer,
                    valueSerializer = valueSerializer,
                ),
        )
}
