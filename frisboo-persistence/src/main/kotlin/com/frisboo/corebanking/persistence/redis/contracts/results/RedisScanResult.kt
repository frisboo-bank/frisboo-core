package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed interface RedisScanResult {
    public data class Success(
        val keys: List<String>,
        val cursor: String,
    ) : RedisScanResult
}
