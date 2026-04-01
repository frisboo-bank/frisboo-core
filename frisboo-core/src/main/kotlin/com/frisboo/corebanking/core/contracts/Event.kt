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

/**
 * Marker interface for domain events dispatched through the [EventBus].
 *
 * This is the mediator-level event contract used for in-process event dispatching.
 * For the full domain event envelope with metadata (aggregateId, version, timestamps),
 * see [com.frisboo.corebanking.core.domain.events.DomainEvent].
 */
public interface Event

/**
 * Handles a specific type of event.
 *
 * @param E the event type this handler processes.
 */
public interface EventHandler<E : Event> {
    public suspend fun handle(event: E)
}

/**
 * Publishes events to all registered handlers.
 */
public interface EventBus {
    public suspend fun <E : Event> publish(event: E)

    public suspend fun <E : Event> publishAll(events: List<E>)
}
