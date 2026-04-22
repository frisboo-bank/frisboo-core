package com.frisboo.corebanking.persistence.redis.contracts

public interface RedisOperations<K : Any, V : Any> :
    RedisReadOperations<K, V>,
    RedisWriteOperations<K, V>,
    RedisAtomicOperations<K, V>,
    RedisLockOperations<K, V>
