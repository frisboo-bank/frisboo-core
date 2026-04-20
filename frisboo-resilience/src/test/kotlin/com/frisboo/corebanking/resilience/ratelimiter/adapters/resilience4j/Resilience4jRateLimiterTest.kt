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
package com.frisboo.corebanking.resilience.ratelimiter.adapters.resilience4j

import arrow.core.Either
import com.frisboo.corebanking.statemanager.adapters.local.inmemory.InMemoryRegistryImpl
import com.frisboo.corebanking.statemanager.contracts.StateManager
import com.frisboo.corebanking.statemanager.models.RegistryScope
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterResult
import com.frisboo.corebanking.resilience.ratelimiter.errors.RateLimiterError
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterConfig
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterPersistenceContext
import com.frisboo.corebanking.resilience.ratelimiter.testutils.createRateLimiterConfigArb
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import io.github.resilience4j.ratelimiter.RateLimiterConfig as R4jRateLimiterConfig

internal class Resilience4jRateLimiterTest : StringSpec(
    {

        fun testRegistry(): StateManager<String, String> = InMemoryRegistryImpl(
            scope = RegistryScope(name = "rl-test", team = "resilience"),
        )

        val limiterNameArb = Arb.string(minSize = 1, maxSize = 50)

        suspend fun createLimiter(
            name: String,
            config: RateLimiterConfig,
            stateManager: StateManager<String, String> = testRegistry(),
            scope: CoroutineScope,
        ): Resilience4jRateLimiter = createResilience4jLimiter(
            name = name,
            config = config,
            persistence = RateLimiterPersistenceContext(stateManager, scope),
        )

        "successful call returns Success with value" {
            runTest {
                val testScope = this
                checkAll(limiterNameArb, createRateLimiterConfigArb()) { name, config ->
                    val rl = createLimiter(name, config, scope = testScope)
                    val result = rl.executeSuspend { "hello" }
                    result.shouldBeInstanceOf<RateLimiterResult.Success<String>>()
                    result.value shouldBe "hello"
                }
            }
        }

        "failed call returns Failure with original exception" {
            runTest {
                val testScope = this
                checkAll(limiterNameArb, createRateLimiterConfigArb()) { name, config ->
                    val rl = createLimiter(name, config, scope = testScope)
                    val result = rl.executeSuspend { throw RuntimeException("boom") }
                    result.shouldBeInstanceOf<RateLimiterResult.Failure>()
                    result.cause.shouldBeInstanceOf<RuntimeException>()
                    result.cause.message shouldBe "boom"
                }
            }
        }

        "exhausted permits returns Rejected with LimitExceeded error" {
            runTest {
                val testScope = this
                val config = RateLimiterConfig(
                    limitForPeriod = 1,
                    limitRefreshPeriod = 60.seconds,
                    timeoutDuration = kotlin.time.Duration.ZERO,
                )
                val rl = createLimiter("exhaust-test", config, scope = testScope)

                val first = rl.executeSuspend { "ok" }
                first.shouldBeInstanceOf<RateLimiterResult.Success<String>>()

                val second = rl.executeSuspend { "never" }
                second.shouldBeInstanceOf<RateLimiterResult.Rejected>()
                second.error.requestedPermits shouldBe 1
            }
        }

        "acquire returns Right on success and Left on rejection" {
            runTest {
                val testScope = this
                val config = RateLimiterConfig(
                    limitForPeriod = 1,
                    limitRefreshPeriod = 60.seconds,
                    timeoutDuration = kotlin.time.Duration.ZERO,
                )
                val rl = createLimiter("acquire-test", config, scope = testScope)

                val success = rl.acquire(1)
                success.shouldBeInstanceOf<Either.Right<Unit>>()

                val rejection = rl.acquire(1)
                rejection.shouldBeInstanceOf<Either.Left<RateLimiterError.LimitExceeded>>()
                rejection.value.requestedPermits shouldBe 1
            }
        }

        "metrics reflect call outcomes" {
            runTest {
                val testScope = this
                val config = RateLimiterConfig(
                    limitForPeriod = 10,
                    limitRefreshPeriod = 60.seconds,
                    timeoutDuration = kotlin.time.Duration.ZERO,
                )
                val rl = createLimiter("metrics-test", config, scope = testScope)

                rl.executeSuspend { "ok" }
                rl.executeSuspend { throw RuntimeException("fail") }
                rl.metrics.successfulCalls shouldBe 1
                rl.metrics.availablePermits shouldBeGreaterThanOrEqual 0
            }
        }

        "config returns the domain config supplied at creation" {
            runTest {
                val testScope = this
                checkAll(limiterNameArb, createRateLimiterConfigArb()) { name, config ->
                    val rl = createLimiter(name, config, scope = testScope)
                    rl.config shouldBe config
                }
            }
        }

        "internalConfig returns the Resilience4j native config" {
            runTest {
                val testScope = this
                checkAll(limiterNameArb, createRateLimiterConfigArb()) { name, config ->
                    val rl = createLimiter(name, config, scope = testScope)
                    val internal = rl.internalConfig
                    internal.shouldBeInstanceOf<R4jRateLimiterConfig>()
                    internal.limitForPeriod shouldBe config.limitForPeriod
                    internal.limitRefreshPeriod shouldBe config.limitRefreshPeriod.toJavaDuration()
                    internal.timeoutDuration shouldBe config.timeoutDuration.toJavaDuration()
                }
            }
        }
    },
)
