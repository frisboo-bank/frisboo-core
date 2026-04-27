package com.frisboo.corebanking.persistence.redis.adapters.lettuce

import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import io.lettuce.core.ScanCursor

internal fun RedisScanCursor.toRedisLettuceScanCursor(): ScanCursor? = when {
    this.isFinished -> ScanCursor.FINISHED
    this.cursor == 0L -> ScanCursor.INITIAL
    else -> ScanCursor.of(this.cursor.toString())
}
