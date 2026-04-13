package com.frisboo.corebanking.persistence.redis.contracts

import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor

public interface RedisCommands {
    public suspend fun ping(): String
    public suspend fun get(key: ByteArray): ByteArray?
    public suspend fun set(key: ByteArray, value: ByteArray): ByteArray?
    public suspend fun set(key: ByteArray, value: ByteArray, ttlMs: Long): ByteArray?
    public suspend fun del(key: ByteArray): Long?
    public suspend fun exists(key: ByteArray): Boolean
    public suspend fun exists(vararg keys: ByteArray): Long?
    public suspend fun pexpire(key: ByteArray, ttlMs: Long): Boolean?
    public suspend fun scan(
        cursor: RedisScanCursor?,
        count: Long,
        pattern: ByteArray,
    ): Pair<RedisScanCursor, List<ByteArray>>

    public suspend fun <T : Any> evalRecord(
        script: String,
        keys: Array<ByteArray>,
        args: Array<ByteArray>,
        valueType: Class<T>,
    ): Pair<Long, T?>

    public suspend fun setnx(key: ByteArray, value: ByteArray, ttlMs: Long): Boolean
}
