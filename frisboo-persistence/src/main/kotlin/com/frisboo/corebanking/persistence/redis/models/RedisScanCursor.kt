package com.frisboo.corebanking.persistence.redis.models

public class RedisScanCursor private constructor(
    public val value: String,
    public val isFinished: Boolean,
) {
    public companion object {
        public val INITIAL: RedisScanCursor = RedisScanCursor("0", isFinished = false)
        public val FINISHED: RedisScanCursor = RedisScanCursor("0", isFinished = true)

        public fun of(cursor: String?): RedisScanCursor = when (cursor) {
            null -> INITIAL
            "0" -> FINISHED
            else -> RedisScanCursor(cursor, isFinished = false)
        }
    }
}
