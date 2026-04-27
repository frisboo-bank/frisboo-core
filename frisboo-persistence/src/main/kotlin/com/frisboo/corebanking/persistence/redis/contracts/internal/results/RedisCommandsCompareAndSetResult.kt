package com.frisboo.corebanking.persistence.redis.contracts.internal.results

internal interface RedisCommandsCompareAndSetResult {
    data class Success(val previousValue: ByteArray?) : RedisCommandsCompareAndSetResult {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            return previousValue.contentEquals(other.previousValue)
        }

        override fun hashCode(): Int {
            return previousValue?.contentHashCode() ?: 0
        }
    }

    data object KeyNotFound : RedisCommandsCompareAndSetResult
    data object Mismatch : RedisCommandsCompareAndSetResult
}
