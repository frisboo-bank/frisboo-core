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
package com.frisboo.corebanking.coordination.saga.contracts

import arrow.core.Either
import java.util.UUID

/**
 * Contract for saga (process manager) implementations.
 *
 * A saga orchestrates a multi-step business process that spans multiple aggregates
 * or bounded contexts. Each step may succeed or fail independently, and the saga
 * is responsible for driving the process forward or triggering compensation on failure.
 *
 * @param S the saga state type, typically a sealed interface representing
 *        the finite state machine of the process.
 */
public interface Saga<S> {
    /**
     * Returns the unique identifier of this saga instance.
     */
    public val sagaId: UUID

    /**
     * Returns the current state of the saga.
     */
    public val currentState: S

    /**
     * Transitions the saga to the next state based on the given [event].
     *
     * @param event the domain event or step result that drives the transition.
     * @return [Either.Right] with the new state on success,
     *         or [Either.Left] with a [SagaError] if the transition is invalid.
     */
    public suspend fun <E> transition(event: E): Either<SagaError, S>

    /**
     * Triggers compensation for completed steps, rolling back side-effects
     * in reverse order.
     *
     * @return [Either.Right] on successful compensation,
     *         or [Either.Left] with a [SagaError] if compensation itself fails.
     */
    public suspend fun compensate(): Either<SagaError, S>
}

/**
 * Contract for persisting and retrieving saga instances.
 *
 * @param S the saga state type.
 */
public interface SagaStore<S> {
    /**
     * Persists the current state of a saga.
     *
     * @param sagaId the unique identifier of the saga.
     * @param state the state to persist.
     * @return [Either.Right] on success, [Either.Left] on failure.
     */
    public suspend fun save(
        sagaId: UUID,
        state: S,
    ): Either<SagaError, Unit>

    /**
     * Loads the most recent state of a saga.
     *
     * @param sagaId the unique identifier of the saga.
     * @return [Either.Right] with the saga state, or [Either.Left] if not found or unavailable.
     */
    public suspend fun load(sagaId: UUID): Either<SagaError, S>
}

/** Errors that can occur during saga operations. */
public sealed interface SagaError {
    /** The requested state transition is not allowed from the current state. */
    public data class InvalidTransition(
        val sagaId: UUID,
        val fromState: String,
        val toState: String,
    ) : SagaError

    /** Compensation for one or more steps failed. */
    public data class CompensationFailed(
        val sagaId: UUID,
        val failedStep: String,
        val cause: Throwable? = null,
    ) : SagaError

    /** The saga instance was not found. */
    public data class NotFound(
        val sagaId: UUID,
    ) : SagaError

    /** The underlying store is unavailable. */
    public data class StoreUnavailable(
        val cause: Throwable? = null,
    ) : SagaError
}
