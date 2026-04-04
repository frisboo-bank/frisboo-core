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

import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MAX_FAILURE_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MAX_SLOW_CALL_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_FAILURE_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_MINIMUM_CALLS
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_SLIDING_WINDOW_SIZE
import com.frisboo.corebanking.resilience.circuitbreaker.model.CircuitBreakerConfig.Companion.MIN_SLOW_CALL_RATE
import com.frisboo.corebanking.resilience.circuitbreaker.testutils.createCircuitBreakerConfigArb
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.duration
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class CircuitBreakerConfigTest : StringSpec(
    {

        fun validBaseConfig() = CircuitBreakerConfig(
            failureRateThreshold = 50f,
            slowCallDurationThreshold = 2.seconds,
            slowCallRateThreshold = 80f,
            waitDurationInOpenState = 30.seconds,
            slidingWindowSize = 100,
            minimumNumberOfCalls = 10,
            permittedNumberOfCallsInHalfOpenState = 5,
            maxWaitDurationInHalfOpenState = Duration.ZERO,
        )

        "rejects any failureRateThreshold below 1" {
            checkAll(Arb.float(Float.MIN_VALUE, MIN_FAILURE_RATE - 0.0001f)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(failureRateThreshold = bad)
                }
            }
        }

        "rejects any failureRateThreshold above 100" {
            checkAll(Arb.float(MAX_FAILURE_RATE + 0.0001f, 1_000f)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(failureRateThreshold = bad)
                }
            }
        }

        "rejects any slowCallRateThreshold below 1" {
            checkAll(Arb.float(Float.MIN_VALUE, MIN_SLOW_CALL_RATE - 0.0001f)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(slowCallRateThreshold = bad)
                }
            }
        }

        "rejects any slowCallRateThreshold above 100" {
            checkAll(Arb.float(MAX_SLOW_CALL_RATE + 0.0001f, 1_000f)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(slowCallRateThreshold = bad)
                }
            }
        }

        "rejects any non-positive slowCallDurationThreshold" {
            checkAll(Arb.duration((-1_000_000).milliseconds..0.milliseconds)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(slowCallDurationThreshold = bad)
                }
            }
        }

        "rejects any non-positive waitDurationInOpenState" {
            checkAll(Arb.duration((-1_000_000).milliseconds..0.milliseconds)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(waitDurationInOpenState = bad)
                }
            }
        }

        "rejects any slidingWindowSize below 10" {
            checkAll(Arb.int(-1_000, MIN_SLIDING_WINDOW_SIZE - 1)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(
                        slidingWindowSize = bad,
                        minimumNumberOfCalls = MIN_MINIMUM_CALLS,
                    )
                }
            }
        }

        "rejects any minimumNumberOfCalls below 1" {
            checkAll(Arb.int(-1_000, 0)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(minimumNumberOfCalls = bad)
                }
            }
        }

        "rejects any minimumNumberOfCalls > slidingWindowSize" {
            checkAll(Arb.int(MIN_SLIDING_WINDOW_SIZE, 1000)) { windowSize ->
                val tooMany = Arb.int(windowSize + 1, windowSize + 1000)
                checkAll(tooMany) { bad ->
                    shouldThrow<IllegalArgumentException> {
                        validBaseConfig().copy(
                            slidingWindowSize = windowSize,
                            minimumNumberOfCalls = bad,
                        )
                    }
                }
            }
        }

        "rejects any permittedNumberOfCallsInHalfOpenState <= 0" {
            checkAll(Arb.int(-1_000, 0)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(permittedNumberOfCallsInHalfOpenState = bad)
                }
            }
        }

        "rejects any negative maxWaitDurationInHalfOpenState" {
            checkAll(Arb.duration((-1_000_000).milliseconds..(-1).milliseconds)) { bad ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(maxWaitDurationInHalfOpenState = bad)
                }
            }
        }

        "rejects overlapping recordExceptions and ignoreExceptions" {
            checkAll(Arb.element(IOException::class.java, RuntimeException::class.java)) { exc ->
                shouldThrow<IllegalArgumentException> {
                    validBaseConfig().copy(
                        recordExceptions = setOf(exc),
                        ignoreExceptions = setOf(exc),
                    )
                }
            }
        }

        "accepts any valid configuration generated by the arb" {
            checkAll(createCircuitBreakerConfigArb()) { config ->
                val result = shouldNotThrow<IllegalArgumentException> { config }
                result shouldBe config
            }
        }

        "accepts minimumNumberOfCalls equal to slidingWindowSize" {
            val config = validBaseConfig().copy(slidingWindowSize = 10, minimumNumberOfCalls = 10)
            val result = shouldNotThrow<IllegalArgumentException> { config }
            result shouldBe config
        }

        "accepts zero maxWaitDurationInHalfOpenState" {
            val config = validBaseConfig().copy(maxWaitDurationInHalfOpenState = Duration.ZERO)
            val result = shouldNotThrow<IllegalArgumentException> { config }
            result shouldBe config
        }

        "accepts both sliding window types" {
            val countBased = validBaseConfig().copy(slidingWindowType = SlidingWindowType.COUNT_BASED)
            val timeBased = validBaseConfig().copy(slidingWindowType = SlidingWindowType.TIME_BASED)
            shouldNotThrow<IllegalArgumentException> { countBased } shouldBe countBased
            shouldNotThrow<IllegalArgumentException> { timeBased } shouldBe timeBased
        }

        "accepts writableStackTraceEnabled true and false" {
            val enabled = validBaseConfig().copy(writableStackTraceEnabled = true)
            val disabled = validBaseConfig().copy(writableStackTraceEnabled = false)
            shouldNotThrow<IllegalArgumentException> { enabled } shouldBe enabled
            shouldNotThrow<IllegalArgumentException> { disabled } shouldBe disabled
        }

        "accepts non-overlapping recordExceptions and ignoreExceptions" {
            val config = validBaseConfig().copy(
                recordExceptions = setOf(IOException::class.java),
                ignoreExceptions = setOf(RuntimeException::class.java),
            )
            val result = shouldNotThrow<IllegalArgumentException> { config }
            result shouldBe config
        }

        "accepts empty recordExceptions and ignoreExceptions" {
            val config = validBaseConfig().copy(recordExceptions = emptySet(), ignoreExceptions = emptySet())
            val result = shouldNotThrow<IllegalArgumentException> { config }
            result shouldBe config
        }
    },
)
