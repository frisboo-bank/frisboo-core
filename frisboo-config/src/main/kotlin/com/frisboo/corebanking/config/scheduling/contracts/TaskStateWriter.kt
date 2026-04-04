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
package com.frisboo.corebanking.config.scheduling.contracts

import arrow.core.Either
import com.frisboo.corebanking.config.scheduling.model.TaskSchedule
import kotlin.time.Instant

/**
 * Write contract for scheduled task lifecycle and execution state.
 */
public interface TaskStateWriter {
    /**
     * Registers a schedule for a task.
     */
    public suspend fun register(schedule: TaskSchedule): Either<SchedulingError, Unit>

    /**
     * Records a task execution timestamp.
     */
    public suspend fun recordExecution(
        taskId: String,
        executedAt: Instant,
    ): Either<SchedulingError, Unit>

    /**
     * Enables scheduling for the given task.
     */
    public suspend fun enable(taskId: String): Either<SchedulingError, Unit>

    /**
     * Disables scheduling for the given task.
     */
    public suspend fun disable(taskId: String): Either<SchedulingError, Unit>

    /**
     * Unregisters a task schedule.
     */
    public suspend fun unregister(taskId: String): Either<SchedulingError, Unit>
}

/**
 * Domain errors for scheduling state write operations.
 */
public sealed interface SchedulingError {
    /**
     * The requested task was not found.
     */
    public data class TaskNotFound(
        public val taskId: String,
    ) : SchedulingError

    /**
     * The task is already registered.
     */
    public data class AlreadyRegistered(
        public val taskId: String,
    ) : SchedulingError

    /**
     * The cron expression is invalid for the task.
     */
    public data class InvalidCron(
        public val taskId: String,
        public val expression: String,
    ) : SchedulingError

    /**
     * The underlying scheduling store is unavailable.
     */
    public data class Unavailable(
        public val cause: Throwable? = null,
    ) : SchedulingError
}
