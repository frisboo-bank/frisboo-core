package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor

internal interface RedisManageCommands {
    suspend fun del(key: ByteArray): Long?
    suspend fun exists(key: ByteArray): Long?
    suspend fun pexpire(key: ByteArray, ttlMs: Long): Boolean?
    suspend fun scan(
        cursor: RedisScanCursor?,
        count: Long,
        pattern: ByteArray,
    ): Pair<RedisScanCursor, List<ByteArray>>
}
