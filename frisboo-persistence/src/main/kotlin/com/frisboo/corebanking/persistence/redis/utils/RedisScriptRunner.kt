package com.frisboo.corebanking.persistence.redis.utils

import arrow.core.Either
import arrow.core.raise.either
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_OPERATION_TIMEOUT
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlin.time.Duration

/** Status-byte convention: first byte = status (0 = existing, 1 = created), rest = value payload. */
public data class RedisScriptResult(
    val status: Byte,
    val value: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RedisScriptResult) return false
        return status == other.status && value.contentEquals(other.value)
    }

    override fun hashCode(): Int = 31 * status.hashCode() + value.contentHashCode()
}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
public suspend fun RedisCoroutinesCommands<ByteArray, ByteArray>.evalScript(
    script: String,
    keys: Array<ByteArray>,
    vararg args: ByteArray,
    timeout: Duration = REDIS_DEFAULT_OPERATION_TIMEOUT,
    errorContext: String = "Lua script execution failed",
): Either<PersistenceError, ByteArray> = either {
    executeWithTimeout(
        timeout = timeout,
        block = {
            eval<ByteArray>(
                script,
                ScriptOutputType.VALUE,
                keys,
                *args,
            )
        },
        mapError = { e -> PersistenceError.ConnectionFailed(errorContext, e) },
    ).bind() ?: raise(PersistenceError.ConnectionFailed("$errorContext: script returned null"))
}

public fun parseStatusResult(raw: ByteArray): RedisScriptResult =
    RedisScriptResult(
        status = raw[0],
        value = raw.copyOfRange(1, raw.size),
    )
