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
 * Marker interface representing a query in the system.
 *
 * Queries are read-only operations that return data without side effects.
 *
 * @param R the result type of this query.
 */
public interface Query<R>

/**
 * Handles a specific type of query.
 *
 * @param Q the query type this handler processes.
 * @param R the result type returned after processing.
 */
public interface QueryHandler<Q : Query<R>, R> {
    public suspend fun handle(query: Q): Either<AppError, R>
}

/**
 * Dispatches queries to their registered handlers.
 */
public interface QueryBus {
    public suspend fun <R, Q : Query<R>> dispatch(query: Q): Either<AppError, R>
}
