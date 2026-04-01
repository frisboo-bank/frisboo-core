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
package com.frisboo.corebanking.coordination.lock.contracts

import arrow.core.Either
import com.frisboo.corebanking.coordination.lock.model.LockConfig

/**
 * Contract for distributed lock acquisition operations.
 */
public interface LockAcquirer {
    /**
     * Attempts to acquire a lock for the given resource using the provided configuration.
     *
     * @param resource unique resource identifier to lock.
     * @param config lock acquisition and holding configuration.
     * @return [Either.Right] with [LockToken] on successful acquisition, [Either.Left] with [LockError] on failure.
     */
    public suspend fun acquire(
        resource: String,
        config: LockConfig,
    ): Either<LockError, LockToken>
}
