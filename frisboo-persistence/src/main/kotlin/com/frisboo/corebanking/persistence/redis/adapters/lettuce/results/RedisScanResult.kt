package com.frisboo.corebanking.persistence.redis.adapters.lettuce.results

import com.frisboo.corebanking.persistence.redis.contracts.results.RedisScanResult
import com.frisboo.corebanking.persistence.redis.models.RedisScanCursor
import io.lettuce.core.KeyScanCursor

internal fun RedisScanResult.Companion.fromLettuceResult(
    result: KeyScanCursor<ByteArray>?,
): Pair<RedisScanCursor, List<ByteArray>> {
    val cursor = result?.cursor
    val keys = result?.keys?.toList() ?: emptyList()
    return RedisScanCursor.of(cursor) to keys
}
