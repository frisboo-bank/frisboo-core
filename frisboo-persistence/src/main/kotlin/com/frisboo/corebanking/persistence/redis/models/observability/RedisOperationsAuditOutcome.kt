package com.frisboo.corebanking.persistence.redis.models.observability

public enum class RedisOperationsAuditOutcome {
    CONNECTION_FAILED,
    FAILURE,
    SUCCESS,
    TIMEOUT,
}
