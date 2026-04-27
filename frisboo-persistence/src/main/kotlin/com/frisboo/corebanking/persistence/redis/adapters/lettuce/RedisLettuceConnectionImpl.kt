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
package com.frisboo.corebanking.persistence.redis.adapters.lettuce

import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisCommands
import com.frisboo.corebanking.persistence.redis.contracts.internal.RedisConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.support.AsyncPool
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalLettuceCoroutinesApi::class)
internal class RedisLettuceConnectionImpl(
    private val connectionPool: AsyncPool<StatefulRedisConnection<ByteArray, ByteArray>>,
    private val acquireTimeout: Duration = 5.seconds,
) : RedisConnection {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    override suspend fun <T> withCommands(block: suspend (RedisCommands) -> T): T {
        val connection = withTimeout(acquireTimeout) {
            connectionPool.acquire().await()
        }

        try {
            return block(RedisLettuceCommandsImpl(connection.coroutines()))
        } finally {
            try {
                connectionPool.release(connection).await()
            } catch (e: Exception) {
                logger.error(e) { "Failed to release Redis connection" }
            }
        }
    }
}
