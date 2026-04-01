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
package com.frisboo.corebanking.data.eventsourcing.contracts

import arrow.core.Either
import com.frisboo.corebanking.core.domain.events.DomainEvent
import java.util.UUID

/**
 * Contract for event store implementations.
 *
 * Provides append-only persistence of domain events keyed by aggregate identity.
 * Implementations must guarantee:
 * - Events are appended atomically per aggregate.
 * - Optimistic concurrency via [expectedVersion] prevents lost writes.
 * - Events are returned in append order within a stream.
 */
public interface EventStore {
    /**
     * Appends events to the stream identified by [aggregateId].
     *
     * @param aggregateId the unique identifier of the aggregate root.
     * @param events the ordered list of events to append.
     * @param expectedVersion the expected current version of the stream for optimistic concurrency.
     *        Pass `0` when creating a new stream.
     * @return [Either.Right] with the new stream version after appending,
     *         or [Either.Left] with an [EventStoreError] on failure.
     */
    public suspend fun append(
        aggregateId: UUID,
        events: List<DomainEvent<*>>,
        expectedVersion: Long,
    ): Either<EventStoreError, Long>

    /**
     * Loads all events for the given aggregate, ordered by version.
     *
     * @param aggregateId the unique identifier of the aggregate root.
     * @param afterVersion load only events with a version strictly greater than this value.
     *        Pass `0` to load from the beginning.
     * @return [Either.Right] with the ordered list of events,
     *         or [Either.Left] with an [EventStoreError] on failure.
     */
    public suspend fun loadStream(
        aggregateId: UUID,
        afterVersion: Long = 0,
    ): Either<EventStoreError, List<DomainEvent<*>>>

    /**
     * Returns the current version of the stream for the given aggregate.
     *
     * @param aggregateId the unique identifier of the aggregate root.
     * @return [Either.Right] with the current version (`0` if no events exist),
     *         or [Either.Left] with an [EventStoreError] on failure.
     */
    public suspend fun currentVersion(aggregateId: UUID): Either<EventStoreError, Long>
}

/** Errors that can occur during event store operations. */
public sealed interface EventStoreError {
    /** The stream version does not match [expectedVersion], indicating a concurrent modification. */
    public data class VersionConflict(
        val aggregateId: UUID,
        val expectedVersion: Long,
        val actualVersion: Long,
    ) : EventStoreError

    /** No events exist for the given aggregate. */
    public data class StreamNotFound(
        val aggregateId: UUID,
    ) : EventStoreError

    /** The underlying storage is unavailable. */
    public data class StoreUnavailable(
        val cause: Throwable? = null,
    ) : EventStoreError

    /** An event failed serialization or deserialization. */
    public data class SerializationFailure(
        val message: String,
        val cause: Throwable? = null,
    ) : EventStoreError
}
