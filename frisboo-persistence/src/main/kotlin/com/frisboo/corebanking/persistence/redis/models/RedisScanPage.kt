package com.frisboo.corebanking.persistence.redis.models

/**
 * Page of keys from Redis SCAN with next cursor.
 */
public data class RedisScanPage<K>(
    val keys: List<K>,
    val nextCursor: RedisScanCursor,
)
