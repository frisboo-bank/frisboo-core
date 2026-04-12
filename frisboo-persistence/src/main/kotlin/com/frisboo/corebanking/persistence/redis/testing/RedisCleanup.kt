package com.frisboo.corebanking.persistence.redis.testing

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.ScanArgs
import io.lettuce.core.ScanCursor
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import kotlinx.coroutines.withTimeout
import org.jetbrains.annotations.VisibleForTesting
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalLettuceCoroutinesApi::class)
@VisibleForTesting
public suspend fun StatefulRedisConnection<ByteArray, ByteArray>.deleteAll(scope: String) {
    val commands = coroutines()
    val pattern = "$scope:*".toByteArray()
    var cursor = "0"
    val batchSize = 1000L

    withTimeout(30.seconds) {
        do {
            val result = commands.scan(
                ScanCursor.of(cursor),
                ScanArgs.Builder
                    .matches(pattern)
                    .limit(batchSize),
            ) ?: break

            if (result.keys.isNotEmpty()) {
                commands.del(*result.keys.toTypedArray())
            }

            cursor = result.cursor
        } while (!result.isFinished)
    }
}

@OptIn(ExperimentalLettuceCoroutinesApi::class)
@VisibleForTesting
public suspend fun StatefulRedisConnection<ByteArray, ByteArray>.expireNow(key: String) {
    val pastTimestamp = (System.currentTimeMillis() - 1000).toString()
    coroutines().expireat(key.toByteArray(), pastTimestamp.toLong())
}
