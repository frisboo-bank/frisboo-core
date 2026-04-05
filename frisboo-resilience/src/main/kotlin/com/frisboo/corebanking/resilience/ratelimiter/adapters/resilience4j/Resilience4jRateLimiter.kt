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
import arrow.core.left
import arrow.core.right
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiter
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterMetrics
import com.frisboo.corebanking.resilience.ratelimiter.contracts.RateLimiterResult
import com.frisboo.corebanking.resilience.ratelimiter.errors.RateLimiterError
import com.frisboo.corebanking.resilience.ratelimiter.model.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiter as R4jRateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig as R4jRateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException

internal class Resilience4jRateLimiter(
    private val delegate: R4jRateLimiter,
    override val config: RateLimiterConfig,
) : RateLimiter<R4jRateLimiterConfig> {

    private val successfulCallsCounter = AtomicLong(0)
    private val rejectedCallsCounter = AtomicLong(0)

    override val metrics: RateLimiterMetrics =
        RateLimiterMetrics.from(delegate, successfulCallsCounter, rejectedCallsCounter)

    override val internalConfig: R4jRateLimiterConfig get() = delegate.rateLimiterConfig

    override suspend fun <T> executeSuspend(block: suspend () -> T): RateLimiterResult<T> {
        return try {
            if (!delegate.acquirePermission()) {
                recordRejection()
                return RateLimiterResult.Rejected(RateLimiterError.LimitExceeded(requestedPermits = 1))
            }
            val value = block()
            recordSuccess()
            RateLimiterResult.Success(value)
        } catch (_: RequestNotPermitted) {
            recordRejection()
            RateLimiterResult.Rejected(RateLimiterError.LimitExceeded(requestedPermits = 1))
        } catch (ce: CancellationException) {
            throw ce
        } catch (vme: VirtualMachineError) {
            throw vme
        } catch (error: Error) {
            throw error
        } catch (throwable: Throwable) {
            RateLimiterResult.Failure(throwable)
        }
    }

    override suspend fun acquire(permits: Int): Either<RateLimiterError, Unit> {
        return try {
            if (delegate.acquirePermission(permits)) {
                recordSuccess()
                Unit.right()
            } else {
                recordRejection()
                RateLimiterError.LimitExceeded(requestedPermits = permits).left()
            }
        } catch (_: RequestNotPermitted) {
            recordRejection()
            RateLimiterError.LimitExceeded(requestedPermits = permits).left()
        } catch (ce: CancellationException) {
            throw ce
        } catch (vme: VirtualMachineError) {
            throw vme
        } catch (error: Error) {
            throw error
        } catch (throwable: Throwable) {
            RateLimiterError.Unavailable(
                reason = throwable.message ?: "Rate limiter backend unavailable",
            ).left()
        }
    }

    override fun recordSuccess(): Unit {
        successfulCallsCounter.incrementAndGet()
    }

    override fun recordRejection(): Unit {
        rejectedCallsCounter.incrementAndGet()
    }
}
