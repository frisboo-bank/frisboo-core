package com.frisboo.corebanking.persistence.redis.contracts.internal

internal interface RedisManageCommands {
    suspend fun ping(): Boolean
    suspend fun pexpire(key: ByteArray, ttlMs: Long): Boolean
}
