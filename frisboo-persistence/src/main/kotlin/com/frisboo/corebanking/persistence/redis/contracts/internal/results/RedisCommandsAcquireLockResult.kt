package com.frisboo.corebanking.persistence.redis.contracts.internal.results

internal interface RedisCommandsAcquireLockResult {
    data class Acquired(val token: ByteArray) : RedisCommandsAcquireLockResult {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Acquired
            return token.contentEquals(other.token)
        }

        override fun hashCode(): Int {
            return token.contentHashCode()
        }
    }

    data object AlreadyHeld : RedisCommandsAcquireLockResult
}
