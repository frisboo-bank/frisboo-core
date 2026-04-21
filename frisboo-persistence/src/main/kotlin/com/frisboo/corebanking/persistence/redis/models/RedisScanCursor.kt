package com.frisboo.corebanking.persistence.redis.models

/**
 * Cursor for Redis SCAN operations.
 *
 * @property value The string value of the cursor.
 * @property isFinished Indicates whether the scan operation is finished (cursor is "0").
 */
public class RedisScanCursor private constructor(
    public val value: String,
    public val isFinished: Boolean,
) {
    public companion object {
        public val INITIAL: RedisScanCursor = RedisScanCursor("0", isFinished = false)
        public val FINISHED: RedisScanCursor = RedisScanCursor("0", isFinished = true)

        public fun of(cursor: String?): RedisScanCursor = when {
            cursor.isNullOrBlank() -> INITIAL
            cursor == "0" -> FINISHED
            else -> RedisScanCursor(cursor, isFinished = false)
        }
    }
}
