package com.frisboo.corebanking.persistence.redis.contracts.internal

internal interface RedisPingCommands {
    suspend fun ping(): Boolean
}
