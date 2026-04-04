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
package com.frisboo.corebanking.config.featureflag.contracts

import arrow.core.Either

/**
 * Write contract for feature flag mutations.
 */
public interface FeatureFlagWriter {
    /**
     * Enables a feature flag globally.
     */
    public suspend fun enable(flagName: String): Either<FeatureFlagError, Unit>

    /**
     * Disables a feature flag globally.
     */
    public suspend fun disable(flagName: String): Either<FeatureFlagError, Unit>

    /**
     * Sets the rollout percentage for a feature flag.
     */
    public suspend fun setPercentage(
        flagName: String,
        percentage: Int,
    ): Either<FeatureFlagError, Unit>

    /**
     * Enables a feature flag for a specific user.
     */
    public suspend fun enableForUser(
        flagName: String,
        userId: String,
    ): Either<FeatureFlagError, Unit>

    /**
     * Disables a feature flag for a specific user.
     */
    public suspend fun disableForUser(
        flagName: String,
        userId: String,
    ): Either<FeatureFlagError, Unit>
}

/**
 * Domain errors for feature flag write operations.
 */
public sealed interface FeatureFlagError {
    /**
     * The requested flag does not exist.
     */
    public data class NotFound(
        public val flagName: String,
    ) : FeatureFlagError

    /**
     * The provided percentage is outside the valid range.
     */
    public data class InvalidPercentage(
        public val flagName: String,
        public val percentage: Int,
    ) : FeatureFlagError

    /**
     * The underlying flag store is unavailable.
     */
    public data class Unavailable(
        public val cause: Throwable? = null,
    ) : FeatureFlagError
}
