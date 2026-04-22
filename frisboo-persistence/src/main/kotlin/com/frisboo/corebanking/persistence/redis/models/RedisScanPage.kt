package com.frisboo.corebanking.persistence.redis.models

/**
 * A simple page returned by SCAN-based pagination.
 */
public data class RedisScanPage<K : Any>(
    public val keys: List<K>,
    public val nextCursor: RedisScanCursor,
)
