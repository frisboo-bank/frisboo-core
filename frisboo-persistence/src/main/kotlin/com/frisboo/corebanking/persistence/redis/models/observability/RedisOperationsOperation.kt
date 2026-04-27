package com.frisboo.corebanking.persistence.redis.models.observability

public enum class RedisOperationsOperation(public val operationName: String) {
    PING("redis.ping"),
    GET("redis.get"),
    EXISTS("redis.exists"),
    SCAN_COUNT("redis.scanCount"),
    SCAN_PAGE("redis.scanPage"),
    SET("redis.set"),
    SET_WITH_TTL("redis.setWithTtl"),
    DEL("redis.del"),
    PEXPIRE("redis.pexpire"),
    SET_WITH_LOCK("redis.setWithLock"),
    SET_WITH_TTL_AND_LOCK("redis.setWithTtlAndLock"),
    ACQUIRE_LOCK("redis.acquireLock"),
    RELEASE_LOCK("redis.releaseLock"),
    COMPARE_AND_SET("redis.compareAndSet"),
    COMPARE_AND_SET_WITH_TTL("redis.compareAndSetWithTtl"),
    INCREMENT("redis.increment"),
    DECREMENT("redis.decrement"),
}
