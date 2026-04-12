package com.frisboo.corebanking.persistence.redis.utils

import arrow.core.Either
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_OPERATION_TIMEOUT
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlin.time.Duration

/**
 * Reusable Redis health check via PING command.
 * Returns `true` if Redis responds with PONG, `false` otherwise.
 */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
public suspend fun RedisCoroutinesCommands<*, *>.checkHealth(
    timeout: Duration = REDIS_DEFAULT_OPERATION_TIMEOUT,
): Either<PersistenceError, Boolean> =
    executeWithTimeout(
        timeout = timeout,
        block = { ping() },
        mapError = { e -> PersistenceError.ConnectionFailed("Failed to ping Redis", e) },
    ).map { it == "PONG" }
