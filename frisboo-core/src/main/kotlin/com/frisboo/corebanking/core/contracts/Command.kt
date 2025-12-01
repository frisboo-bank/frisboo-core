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
package com.frisboo.corebanking.core.contracts

import arrow.core.Either
import com.frisboo.corebanking.core.domain.errors.AppError

/**
 * A marker interface representing a command in the system.
 *
 * Commands are used to encapsulate all the information needed to perform an action or trigger a process.
 */
public interface Command

/**
 * Represents a handler for a specific type of command in the system.
 *
 * @param C The type of the command that this handler processes. Must extend the `Command` interface.
 * @param R The type of the result returned after processing the command.
 */
public interface CommandHandler<C : Command, R> {
    /**
     * Handles the given command and returns either an application error or the result.
     *
     * @param command The command to be processed.
     * @return An `Either` containing an `AppError` if the processing fails, or the result of type `R` if successful.
     */
    public suspend fun handle(command: C): Either<AppError, R>
}

/**
 * A bus for dispatching commands to their respective handlers.
 *
 * The `CommandBus` is responsible for routing commands to the appropriate `CommandHandler` implementation.
 */
public interface CommandBus {
    /**
     * Dispatches the given command to its corresponding handler and returns the result.
     *
     * @param C The type of the command being dispatched. Must extend the `Command` interface.
     * @param R The type of the result returned after processing the command.
     * @param command The command to be dispatched.
     * @return An `Either` containing an `AppError` if the dispatch or processing fails, or the result of type `R` if successful.
     */
    public suspend fun <C : Command, R> dispatch(command: C): Either<AppError, R>
}
