package com.frisboo.corebanking.persistence.redis.utils

import arrow.core.Either
import arrow.core.raise.either
import com.frisboo.corebanking.core.coroutines.executeWithTimeout
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_OPERATION_TIMEOUT
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_PAGE_SIZE
import com.frisboo.corebanking.persistence.redis.constants.REDIS_DEFAULT_SCAN_BATCH_SIZE
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.KeyScanCursor
import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlin.time.Duration

public data class RedisScanPage<K>(
    val keys: List<K>,
    val nextCursor: String?,
)

/** Full keyspace scan — O(N). Avoid on large datasets or hot paths. */
@OptIn(ExperimentalLettuceCoroutinesApi::class)
public suspend fun RedisCoroutinesCommands<ByteArray, ByteArray>.scanCount(
    pattern: ByteArray,
    timeout: Duration = REDIS_DEFAULT_OPERATION_TIMEOUT,
    batchSize: Long = REDIS_DEFAULT_SCAN_BATCH_SIZE,
): Either<PersistenceError, Long> = either {
    var cursor = "0"
    var totalCount = 0L

    do {
        val scanResult = executeScan(pattern, cursor, batchSize, timeout).bind()
        totalCount += scanResult.keys.size
        cursor = scanResult.cursor

        if (scanResult.isFinished) break
    } while (cursor != "0")

    totalCount
}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
public suspend fun RedisCoroutinesCommands<ByteArray, ByteArray>.scanPage(
    pattern: ByteArray,
    cursor: String? = null,
    limit: Int = REDIS_DEFAULT_PAGE_SIZE,
    timeout: Duration = REDIS_DEFAULT_OPERATION_TIMEOUT,
): Either<PersistenceError, RedisScanPage<ByteArray>> = either {
    val result = executeScan(pattern, cursor ?: "0", limit.toLong(), timeout).bind()

    RedisScanPage(
        keys = result.keys,
        nextCursor = if (result.isFinished) null else result.cursor,
    )
}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
private suspend fun RedisCoroutinesCommands<ByteArray, ByteArray>.executeScan(
    pattern: ByteArray,
    cursor: String,
    limit: Long,
    timeout: Duration,
): Either<PersistenceError, KeyScanCursor<ByteArray>> = either {
    executeWithTimeout(
        timeout = timeout,
        block = {
            scan(
                ScanCursor.of(cursor),
                ScanArgs.Builder
                    .matches(pattern)
                    .limit(limit),
            )
        },
        mapError = { e -> PersistenceError.ConnectionFailed("Failed to scan keys in Redis", e) },
    ).bind() ?: raise(PersistenceError.ConnectionFailed("SCAN returned null"))
}
