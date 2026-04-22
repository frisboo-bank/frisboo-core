package com.frisboo.corebanking.persistence.redis.contracts.internal

internal interface RedisCommands :
    RedisReadCommands,
    RedisWriteCommands,
    RedisAtomicCommands,
    RedisManageCommands,
    RedisLockCommands
