package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import io.lettuce.core.KeyScanCursor

internal interface RedisReadCommands {
    suspend fun get(key: ByteArray): ByteArray?

    suspend fun exists(key: ByteArray): Boolean

    suspend fun scan(
        cursor: RedisScanCursor?,
        count: Long,
        pattern: ByteArray,
    ): KeyScanCursor<ByteArray>?
}
