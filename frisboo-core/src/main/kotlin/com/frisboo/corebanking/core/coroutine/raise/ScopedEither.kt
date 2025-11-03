package com.frisboo.corebanking.core.coroutine.raise

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.frisboo.corebanking.core.domain.errors.AppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// A default CoroutineScope with a Job and IO dispatcher.
private val scope = CoroutineScope(Job() + Dispatchers.IO)

/**
 * Represents a context that combines the `Raise` interface for error handling
 * with a `CoroutineScope` for coroutine execution.
 *
 * @param Left The type of the error that can be raised in this context.
 */
public interface EitherContext<Left> : Raise<Left>, CoroutineScope

/**
 * A concrete implementation of `EitherContext` that delegates error handling
 * to a `Raise` instance and coroutine execution to a `CoroutineScope`.
 *
 * @param L The type of the error that can be raised in this context.
 * @property raiseErr The `Raise` instance used for error handling.
 * @property scope The `CoroutineScope` used for coroutine execution.
 */
public class EitherCoroutine<L>(
    public val raiseErr: Raise<L>,
    public val scope: CoroutineScope,
) : Raise<L> by raiseErr, CoroutineScope by scope, EitherContext<L>

/**
 * Executes a block of code within a service-specific coroutine scope.
 *
 * @param context An optional `CoroutineContext` to be added to the scope.
 * @param block The suspendable block of code to execute.
 * @return The result of the block execution.
 */
public suspend fun <T> serviceScope(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T,
): T = block(scope + context)

/**
 * Executes a block of code within an `Either` context, allowing for error handling
 * and coroutine execution. This function combines the `Raise` interface for error handling
 * with a `CoroutineScope` for coroutine execution, enabling structured error management
 * within a coroutine context.
 *
 * @param L The type of the error that can be raised in this context.
 * @param R The type of the result produced by the block.
 * @param context An optional `CoroutineContext` to be added to the scope. Defaults to `EmptyCoroutineContext`.
 * @param block The suspendable block of code to execute within the `EitherContext`.
 *              This block has access to both error handling and coroutine execution capabilities.
 * @return An `Either` containing the result of the block execution (`Right`) or an error (`Left`).
 */
public suspend fun <L, R> scopedEither(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend EitherContext<L>.() -> R,
): Either<L, R> = either {
    serviceScope {
        block(EitherCoroutine(raiseErr = this@either, scope = this@serviceScope + context))
    }
}

/**
 * Executes a block of code within an `Either` context specifically for handling `AppError` errors.
 * This is a specialized version of `scopedEither` that uses `AppError` as the error type.
 *
 * @param R The type of the result produced by the block.
 * @param context An optional `CoroutineContext` to be added to the scope. Defaults to `EmptyCoroutineContext`.
 * @param block The suspendable block of code to execute within the `EitherContext`.
 *              This block has access to both error handling and coroutine execution capabilities.
 * @return An `Either` containing the result of the block execution (`Right`) or an `AppError` (`Left`).
 */
public suspend fun <R> scopedEitherWithError(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend EitherContext<AppError>.() -> R,
): Either<AppError, R> = scopedEither<AppError, R>(context, block)


