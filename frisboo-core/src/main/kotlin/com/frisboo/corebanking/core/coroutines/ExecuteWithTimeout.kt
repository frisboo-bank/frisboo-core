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
package com.frisboo.corebanking.core.coroutines

import arrow.core.Either
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

public suspend fun <E, T> executeWithTimeout(
    timeout: Duration,
    block: suspend () -> T,
    onTimeout: (Duration, TimeoutCancellationException) -> E,
    onError: (Throwable) -> E,
): Either<E, T> =
    try {
        Either.Right(
            withTimeout(timeout) {
                block()
            },
        )
    } catch (e: TimeoutCancellationException) {
        Either.Left(onTimeout(timeout, e))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Either.Left(onError(e))
    }
