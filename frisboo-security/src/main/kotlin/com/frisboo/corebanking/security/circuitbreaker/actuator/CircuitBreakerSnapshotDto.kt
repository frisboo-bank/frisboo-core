package com.frisboo.corebanking.security.circuitbreaker.actuator

public data class CircuitBreakerSnapshotDto(
    val state: String,
    val failureRate: Float,
    val numberOfBufferedCalls: Int,
    val numberOfFailedCalls: Int,
    val numberOfSuccessfulCalls: Int,
)
