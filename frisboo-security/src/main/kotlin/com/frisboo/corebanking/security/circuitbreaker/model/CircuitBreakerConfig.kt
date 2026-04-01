package com.frisboo.corebanking.security.circuitbreaker.model

import java.time.Duration

public data class CircuitBreakerConfig(
    val failureRateThreshold: Float,
    val maxWaitDurationInHalfOpenState: Duration,
    val minimumNumberOfCalls: Int,
    val permittedNumberOfCallsInHalfOpenState: Int,
    val priorityTier: CircuitBreakerPriorityTier,
    val slidingWindowSize: Int,
    val slowCallDurationThreshold: Duration,
    val slowCallRateThreshold: Float,
    val waitDurationInOpenState: Duration,
) {

    public companion object {
        public const val MIN_FAILURE_RATE: Float = 1f
        public const val MAX_FAILURE_RATE: Float = 100f
        public const val MIN_SLOW_CALL_RATE: Float = 1f
        public const val MAX_SLOW_CALL_RATE: Float = 100f
        public const val MIN_SLIDING_WINDOW_SIZE: Int = 10
        public const val MIN_MINIMUM_CALLS: Int = 1

        private fun requireInRange(value: Float, min: Float, max: Float, name: String) {
            require(value in min..max) { "$name must be between $min and $max, got $value" }
        }
    }

    init {
        validate()
    }

    private fun validate() {
        requireInRange(failureRateThreshold, MIN_FAILURE_RATE, MAX_FAILURE_RATE, "failureRateThreshold")
        requireInRange(slowCallRateThreshold, MIN_SLOW_CALL_RATE, MAX_SLOW_CALL_RATE, "slowCallRateThreshold")
        require(!slowCallDurationThreshold.isNegative && !slowCallDurationThreshold.isZero) {
            "slowCallDurationThreshold must be positive"
        }
        require(!waitDurationInOpenState.isNegative && !waitDurationInOpenState.isZero) {
            "waitDurationInOpenState must be positive"
        }
        require(slidingWindowSize >= MIN_SLIDING_WINDOW_SIZE) {
            "slidingWindowSize must be >= $MIN_SLIDING_WINDOW_SIZE, got $slidingWindowSize"
        }
        require(minimumNumberOfCalls in MIN_MINIMUM_CALLS..slidingWindowSize) {
            "minimumNumberOfCalls must be between $MIN_MINIMUM_CALLS and slidingWindowSize ($slidingWindowSize), got $minimumNumberOfCalls"
        }
        require(permittedNumberOfCallsInHalfOpenState > 0) {
            "permittedNumberOfCallsInHalfOpenState must be positive, got $permittedNumberOfCallsInHalfOpenState"
        }
        require(!maxWaitDurationInHalfOpenState.isNegative) {
            "maxWaitDurationInHalfOpenState must not be negative"
        }
    }
}
