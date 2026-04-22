package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisDeleteResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfAbsentResult

internal interface RedisWriteCommands {
    suspend fun setGet(key: ByteArray, value: ByteArray): ByteArray?
    suspend fun setGet(key: ByteArray, value: ByteArray, ttlMs: Long): ByteArray?
    suspend fun setIfAbsent(key: ByteArray, value: ByteArray): Boolean
    suspend fun setIfAbsent(key: ByteArray, value: ByteArray, ttlMs: Long): Boolean
    suspend fun del(key: ByteArray): ByteArray?
}
