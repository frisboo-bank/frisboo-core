/*
 * Copyright 2025 Frisboo Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.frisboo.corebanking.resilience.circuitbreaker.model

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class CircuitBreakerConfigTest :
    StringSpec({

        fun validConfig(
            failureRateThreshold: Float = 50f,
            slowCallDurationThreshold: Duration = 2.seconds,
            slowCallRateThreshold: Float = 80f,
            waitDurationInOpenState: Duration = 30.seconds,
            slidingWindowSize: Int = 100,
            slidingWindowType: SlidingWindowType = SlidingWindowType.COUNT_BASED,
            minimumNumberOfCalls: Int = 10,
            permittedNumberOfCallsInHalfOpenState: Int = 5,
            maxWaitDurationInHalfOpenState: Duration = Duration.ZERO,
            recordExceptions: Set<Class<out Throwable>> = emptySet(),
            ignoreExceptions: Set<Class<out Throwable>> = emptySet(),
            writableStackTraceEnabled: Boolean = true,
        ) = CircuitBreakerConfig(
            failureRateThreshold = failureRateThreshold,
            slowCallDurationThreshold = slowCallDurationThreshold,
            slowCallRateThreshold = slowCallRateThreshold,
            waitDurationInOpenState = waitDurationInOpenState,
            slidingWindowSize = slidingWindowSize,
            slidingWindowType = slidingWindowType,
            minimumNumberOfCalls = minimumNumberOfCalls,
            permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState,
            maxWaitDurationInHalfOpenState = maxWaitDurationInHalfOpenState,
            recordExceptions = recordExceptions,
            ignoreExceptions = ignoreExceptions,
            writableStackTraceEnabled = writableStackTraceEnabled,
        )

        "accepts valid configuration with all required parameters" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig()
            }
        }

        "accepts minimum boundary values" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(
                    failureRateThreshold = CircuitBreakerConfig.MIN_FAILURE_RATE,
                    slowCallRateThreshold = CircuitBreakerConfig.MIN_SLOW_CALL_RATE,
                    slidingWindowSize = CircuitBreakerConfig.MIN_SLIDING_WINDOW_SIZE,
                    minimumNumberOfCalls = CircuitBreakerConfig.MIN_MINIMUM_CALLS,
                    slowCallDurationThreshold = 1.milliseconds,
                    waitDurationInOpenState = 1.milliseconds,
                    permittedNumberOfCallsInHalfOpenState = 1,
                    maxWaitDurationInHalfOpenState = Duration.ZERO,
                )
            }
        }

        "accepts maximum boundary values" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(
                    failureRateThreshold = CircuitBreakerConfig.MAX_FAILURE_RATE,
                    slowCallRateThreshold = CircuitBreakerConfig.MAX_SLOW_CALL_RATE,
                    slidingWindowSize = 1000,
                    minimumNumberOfCalls = 1000,
                )
            }
        }

        "accepts TIME_BASED sliding window type" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(slidingWindowType = SlidingWindowType.TIME_BASED)
            }
        }

        "accepts writableStackTraceEnabled false" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(writableStackTraceEnabled = false)
            }
        }

        "rejects failureRateThreshold below minimum" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(failureRateThreshold = 0f)
            }
            error.message shouldContain "failureRateThreshold"
        }

        "rejects failureRateThreshold above maximum" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(failureRateThreshold = 101f)
            }
            error.message shouldContain "failureRateThreshold"
        }

        "rejects negative failureRateThreshold" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(failureRateThreshold = -1f)
            }
            error.message shouldContain "failureRateThreshold"
        }

        "rejects slowCallRateThreshold below minimum" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(slowCallRateThreshold = 0f)
            }
            error.message shouldContain "slowCallRateThreshold"
        }

        "rejects slowCallRateThreshold above maximum" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(slowCallRateThreshold = 101f)
            }
            error.message shouldContain "slowCallRateThreshold"
        }

        "rejects zero slowCallDurationThreshold" {
            shouldThrow<IllegalArgumentException> {
                validConfig(slowCallDurationThreshold = Duration.ZERO)
            }
        }

        "rejects negative slowCallDurationThreshold" {
            shouldThrow<IllegalArgumentException> {
                validConfig(slowCallDurationThreshold = (-1).seconds)
            }
        }

        "rejects zero waitDurationInOpenState" {
            shouldThrow<IllegalArgumentException> {
                validConfig(waitDurationInOpenState = Duration.ZERO)
            }
        }

        "rejects negative waitDurationInOpenState" {
            shouldThrow<IllegalArgumentException> {
                validConfig(waitDurationInOpenState = (-1).seconds)
            }
        }

        "rejects slidingWindowSize below minimum" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(slidingWindowSize = 9, minimumNumberOfCalls = 1)
            }
            error.message shouldContain "slidingWindowSize"
        }

        "rejects minimumNumberOfCalls below 1" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(minimumNumberOfCalls = 0)
            }
            error.message shouldContain "minimumNumberOfCalls"
        }

        "rejects minimumNumberOfCalls above slidingWindowSize" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(slidingWindowSize = 10, minimumNumberOfCalls = 11)
            }
            error.message shouldContain "minimumNumberOfCalls"
        }

        "accepts minimumNumberOfCalls equal to slidingWindowSize" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(slidingWindowSize = 10, minimumNumberOfCalls = 10)
            }
        }

        "rejects zero permittedNumberOfCallsInHalfOpenState" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(permittedNumberOfCallsInHalfOpenState = 0)
            }
            error.message shouldContain "permittedNumberOfCallsInHalfOpenState"
        }

        "rejects negative permittedNumberOfCallsInHalfOpenState" {
            val error = shouldThrow<IllegalArgumentException> {
                validConfig(permittedNumberOfCallsInHalfOpenState = -1)
            }
            error.message shouldContain "permittedNumberOfCallsInHalfOpenState"
        }

        "accepts zero maxWaitDurationInHalfOpenState" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(maxWaitDurationInHalfOpenState = Duration.ZERO)
            }
        }

        "accepts positive maxWaitDurationInHalfOpenState" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(maxWaitDurationInHalfOpenState = 10.seconds)
            }
        }

        "rejects negative maxWaitDurationInHalfOpenState" {
            shouldThrow<IllegalArgumentException> {
                validConfig(maxWaitDurationInHalfOpenState = (-1).seconds)
            }
        }

        "accepts non-overlapping recordExceptions and ignoreExceptions" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(
                    recordExceptions = setOf(java.io.IOException::class.java),
                    ignoreExceptions = setOf(IllegalArgumentException::class.java),
                )
            }
        }

        "rejects overlapping recordExceptions and ignoreExceptions" {
            shouldThrow<IllegalArgumentException> {
                validConfig(
                    recordExceptions = setOf(java.io.IOException::class.java),
                    ignoreExceptions = setOf(java.io.IOException::class.java),
                )
            }
        }

        "accepts empty recordExceptions and ignoreExceptions" {
            shouldNotThrow<IllegalArgumentException> {
                validConfig(
                    recordExceptions = emptySet(),
                    ignoreExceptions = emptySet(),
                )
            }
        }

        "two configs with same values are equal" {
            val a = validConfig()
            val b = validConfig()
            (a == b) shouldBe true
        }

        "two configs with different failureRateThreshold are not equal" {
            val a = validConfig(failureRateThreshold = 50f)
            val b = validConfig(failureRateThreshold = 60f)
            (a == b) shouldBe false
        }
    })
