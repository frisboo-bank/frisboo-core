package com.frisboo.corebanking.core.coroutines

import arrow.core.Either
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

public suspend fun <E, T> executeWithTimeout(
    timeout: Duration,
    block: suspend () -> T,
    mapError: (Throwable) -> E,
): Either<E, T> = try {
    Either.Right(
        withTimeout(timeout) {
            block()
        },
    )
} catch (e: TimeoutCancellationException) {
    Either.Left(mapError(e))
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Either.Left(mapError(e))
}

