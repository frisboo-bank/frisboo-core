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
package outbox.contracts

import arrow.core.Either
import com.frisboo.corebanking.core.domain.errors.AppError
import outbox.models.OutboxEvent

public interface OutboxRepository {
    /**
     * Saves an outbox event to the repository.
     *
     * @param event The outbox event to be saved.
     * @return Either an AppError if the operation fails, or Unit if it succeeds.
     */
    public suspend fun saveEvent(event: OutboxEvent): Either<AppError, OutboxEvent>

    /**
     * Deletes an outbox event from the repository using a callback function.
     *
     * @param event The outbox event to be deleted.
     * @param callback A suspend function that takes an OutputEvent and returns Either an AppError or Unit.
     * @return Either an AppError if the operation fails, or Unit if it succeeds.
     */
    public suspend fun deleteEvent(
        event: OutboxEvent,
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>,
    ): Either<AppError, Unit>
}
