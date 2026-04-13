package com.frisboo.corebanking.persistence.redis.models

public data class RedisScanPage<K>(
    val keys: List<K>,
    val nextCursor: RedisScanCursor,
)
