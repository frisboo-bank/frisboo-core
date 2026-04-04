package com.frisboo.corebanking.core.factory.models

public data class FactoryCacheStats(
    val evictionCount: Long,
    val evictionWeight: Long,
    val hitCount: Long,
    val loadFailureCount: Long,
    val loadSuccessCount: Long,
    val missCount: Long,
    val totalLoadTime: Long,
) {
    public companion object
}
