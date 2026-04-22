package com.frisboo.corebanking.persistence.redis.contracts.internal

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfAbsentResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetIfPresentResult
import com.frisboo.corebanking.persistence.redis.contracts.results.RedisSetResult

internal interface RedisSetCommands {
    suspend fun set(key: ByteArray, value: ByteArray): RedisSetResult
    suspend fun set(key: ByteArray, value: ByteArray, ttlMs: Long): RedisSetResult
    suspend fun setIfAbsent(key: ByteArray, value: ByteArray): RedisSetIfAbsentResult
    suspend fun setIfAbsent(key: ByteArray, value: ByteArray, ttlMs: Long): RedisSetIfAbsentResult
    suspend fun setIfPresent(key: ByteArray, value: ByteArray): RedisSetIfPresentResult
    suspend fun setIfPresent(key: ByteArray, value: ByteArray, ttlMs: Long): RedisSetIfPresentResult
}
