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
package com.frisboo.corebanking.persistence.redis.testing

import com.frisboo.corebanking.persistence.core.constants.REDIS_LATEST_IMAGE
import com.redis.testcontainers.RedisContainer
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.support.AsyncConnectionPoolSupport
import io.lettuce.core.support.AsyncPool
import io.lettuce.core.support.BoundedPoolConfig
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class RedisPoolProps(
    val maxTotal: Int = 50,
    val maxIdle: Int = 10,
)

/**
 * This class serves as a marker for test fixtures related to Redis testing.
 * It can be used to group and organize Redis test utilities, setup, and teardown logic.
 */
class RedisTestFixture(
    redisImage: String = REDIS_LATEST_IMAGE,
    private val poolProps: RedisPoolProps = RedisPoolProps(),
) {
    private val container: RedisContainer = RedisContainer(redisImage)

    /**
     * Mutex to ensure thread-safe operations when starting and stopping the container.
     */
    private val mutex = Mutex()

    /**
     * Volatile flag to indicate whether the Redis container has been started.
     */
    @Volatile
    private var started: Boolean = false

    private lateinit var client: RedisClient
    private lateinit var pool: AsyncPool<StatefulRedisConnection<ByteArray, ByteArray>>

    /**
     * Starts the Redis container and initializes the Redis client and connection pool.
     */
    suspend fun start() =
        mutex.withLock {
            if (started) return

            container.start()
            client = RedisClient.create(container.redisURI)

            val uri = RedisURI.create(container.redisURI)
            pool =
                AsyncConnectionPoolSupport
                    .createBoundedObjectPoolAsync(
                        { client.connectAsync(ByteArrayCodec(), uri).toCompletableFuture() },
                        BoundedPoolConfig
                            .builder()
                            .maxTotal(poolProps.maxTotal)
                            .maxIdle(poolProps.maxIdle)
                            .build(),
                    ).await()
            started = true
        }

    /**
     * Stops the Redis container and closes the Redis client and connection pool.
     */
    suspend fun stop() =
        mutex.withLock {
            if (!started) return

            pool.closeAsync().await()
            client.shutdownAsync().await()
            container.stop()
            started = false
        }

    /**
     * Acquires a connection from the pool.
     *
     * @return A [io.lettuce.core.api.StatefulRedisConnection] to the Redis server.
     */
    suspend fun getConnection(): StatefulRedisConnection<ByteArray, ByteArray> {
        check(started) { "Fixture must be started before acquiring connection" }

        return pool.acquire().await()
    }

    /**
     * Returns the connection pool for use in constructing Redis-based components.
     * @throws IllegalStateException if fixture not started.
     */
    fun getPool(): AsyncPool<StatefulRedisConnection<ByteArray, ByteArray>> {
        check(started) { "Fixture must be started before accessing pool" }
        return pool
    }

    /**
     * Releases a connection back to the pool.
     *
     * @param connection The [io.lettuce.core.api.StatefulRedisConnection] to be released.
     */
    suspend fun releaseConnection(connection: StatefulRedisConnection<ByteArray, ByteArray>) {
        check(started) { "Fixture must be started before releasing connection" }

        pool.release(connection).await()
    }

    /**
     * Utility function to execute a block of code with a Redis connection from the pool.
     * The connection will be automatically released back to the pool after the block is executed.
     *
     * @param block The suspend function that takes a [io.lettuce.core.api.StatefulRedisConnection]
     * and returns a result of type [T].
     * @return The result of the block execution.
     */
    suspend fun <T> withConnection(block: suspend (StatefulRedisConnection<ByteArray, ByteArray>) -> T): T {
        check(started) { "Fixture must be started before using connection" }

        val connection = getConnection()
        try {
            return block(connection)
        } finally {
            releaseConnection(connection)
        }
    }

    /**
     * Flushes all data from the Redis server.
     * This is useful for cleaning up the database between tests.
     */
    suspend fun flushAll() {
        check(started) { "Fixture must be started before flush" }

        withConnection { connection ->
            connection.async().flushall().await()
        }
    }

    /**
     * Deletes all keys matching the given scope from the Redis server.
     *
     * @param scope The scope prefix to match keys for deletion.
     */
    suspend fun deleteAll(scope: String) {
        deleteKeys("$scope:*")
    }

    /**
     * Deletes all keys matching the given pattern from the Redis server.
     *
     * @param pattern The pattern to match keys for deletion (e.g., "scope:*").
     */
    suspend fun deleteKeys(pattern: String) {
        check(started) { "Fixture must be started before deleting keys" }

        withConnection { connection ->
            val keys = connection.async().keys(pattern).await()
            if (keys.isNotEmpty()) {
                connection.async().del(*keys.toTypedArray()).await()
            }
        }
    }
}
