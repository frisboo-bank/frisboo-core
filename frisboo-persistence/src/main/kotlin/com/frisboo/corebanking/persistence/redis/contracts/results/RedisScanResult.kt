package com.frisboo.corebanking.persistence.redis.contracts.results

public sealed class RedisScanResult {
    public data class Success(val keys: List<String>, val cursor: String) : RedisScanResult()
    public data class Failed(val message: String) : RedisScanResult()

    public companion object
}
