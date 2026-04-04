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
 * Unified mediator that dispatches commands, queries, and events.
 *
 * Provides a single entry point for all in-process message routing,
 * decoupling senders from handlers.
 */
public interface Mediator {
    public suspend fun <C : Command, R> send(command: C): Either<AppError, R>

    public suspend fun <R, Q : Query<R>> query(query: Q): Either<AppError, R>

    public suspend fun <E : Event> publish(event: E)

    public suspend fun <E : Event> publishAll(events: List<E>)
}
