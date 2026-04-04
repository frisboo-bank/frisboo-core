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
package com.frisboo.corebanking.resilience.stampede.contracts

import arrow.core.Either
import kotlin.time.Duration

/**
 * Aggregate facade for cache stampede prevention read and write operations.
 */
public interface CacheStampedePrevention :
    StampedeReader,
    StampedeWriter {
    /**
     * Returns cached value for [key] or computes and stores it with [ttl].
     */
    public suspend fun <T> getOrCompute(
        key: String,
        ttl: Duration,
        compute: suspend () -> T,
    ): Either<StampedeError, T>
}
