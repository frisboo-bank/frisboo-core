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
package outbox.repositories.exposed

import arrow.core.Either
import com.frisboo.corebanking.core.coroutine.raise.scopedEither
import com.frisboo.corebanking.core.coroutine.withCoroutineContext
import com.frisboo.corebanking.core.domain.errors.AppError
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import outbox.contracts.OutboxRepository
import outbox.models.OutboxEvent
import java.time.OffsetDateTime

@Component
@Transactional
public class OutboxRepositoryImpl : OutboxRepository {
    /**
     * Saves an outbox event to the database.
     *
     * @param event The outbox event to be saved.
     * @return Either an AppError or Unit on successful save.
     */
    override suspend fun saveEvent(event: OutboxEvent): Either<AppError, OutboxEvent> =
        scopedEither<AppError, OutboxEvent>(ctx) {
            suspendTransaction {
                OutboxTable
                    .insert {
                        it[name] = event.name
                        it[subject] = event.subject
                        it[data] = event.data
                        it[metadata] = event.metadata
                        it[sentAt] = OffsetDateTime.ofInstant(event.sentAt, java.time.ZoneOffset.UTC)
                    }.resultedValues
                    ?.singleOrNull()
                    ?.let { event } ?: event
            }
        }.onLeft { logger.error { "Failed to save outbox event with error: $it" } }
            .onRight { logger.info { "Successfully saved outbox event with ID: ${it.eventId}" } }

    /**
     * Deletes an outbox event from the database.
     *
     * @param event The outbox event to be deleted.
     * @param callback A suspend function that performs the deletion logic.
     * @return Either an AppError or Unit on successful deletion.
     */
    override suspend fun deleteEvent(
        event: OutboxEvent,
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>,
    ): Either<AppError, Unit> {
        TODO("Not yet implemented")
    }

    private val ctx = withCoroutineContext()

    private companion object {
        private val logger = KotlinLogging.logger { }
    }
}
