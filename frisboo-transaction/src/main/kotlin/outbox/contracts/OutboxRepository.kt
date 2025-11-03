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
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>
    ): Either<AppError, Unit>
}
