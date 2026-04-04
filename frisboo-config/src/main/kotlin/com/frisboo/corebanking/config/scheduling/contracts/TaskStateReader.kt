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

import com.frisboo.corebanking.config.scheduling.model.TaskSchedule

/**
 * Read contract for scheduled task state.
 */
public interface TaskStateReader {
    /**
     * Returns a single task schedule by identifier, if registered.
     */
    public suspend fun get(taskId: String): TaskSchedule?

    /**
     * Returns all registered task schedules.
     */
    public suspend fun allSchedules(): List<TaskSchedule>

    /**
     * Returns whether a task is currently due for execution.
     */
    public suspend fun isDue(taskId: String): Boolean
}
