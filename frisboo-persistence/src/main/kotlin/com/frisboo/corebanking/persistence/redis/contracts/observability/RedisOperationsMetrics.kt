package com.frisboo.corebanking.persistence.redis.contracts.observability

import com.frisboo.corebanking.persistence.redis.models.observability.RedisOperationsOperation

public interface RedisOperationsMetrics {
    public fun recordOperationLatency(operationName: RedisOperationsOperation, durationMs: Double, success: Boolean)
    public fun recordAcquireLockConflict()
    public fun recordCompareAndSetConflict()
}
