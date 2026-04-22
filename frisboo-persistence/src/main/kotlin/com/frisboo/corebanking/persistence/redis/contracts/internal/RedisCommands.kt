package com.frisboo.corebanking.persistence.redis.contracts.internal

/**
 * Composite Redis protocol interface. Extends all sub-interfaces for callers
 * that need the full Redis command set.
 */
internal interface RedisCommands :
    RedisReadCommands,
    RedisWriteCommands,
    RedisAtomicCommands,
    RedisManageCommands,
    RedisLockCommands
