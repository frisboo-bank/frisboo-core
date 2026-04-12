package com.frisboo.corebanking.persistence.redis.contracts

public interface RedisKeyNamespace {
    public val prefix: String
    public val scanPattern: ByteArray get() = "$prefix:*".toByteArray()
}
