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
package com.frisboo.corebanking.statemanager.errors

import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import kotlin.time.Duration

public sealed interface StateManagerError {

    public data class ConnectionFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : StateManagerError

    public data class OperationTimeout(
        public val message: String,
        public val duration: Duration,
    ) : StateManagerError

    public data class OperationFailed(
        public val message: String,
    ) : StateManagerError

    public data class InvalidArgument(
        public val name: String,
        public val message: String,
    ) : StateManagerError

    public data class SerializationFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : StateManagerError

    public data class DeserializationFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : StateManagerError

    public data class ComputationFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : StateManagerError

    public data class LockNotHeld(
        public val message: String,
    ) : StateManagerError

    public companion object {
        public fun fromPersistenceError(e: PersistenceError): StateManagerError = when (e) {
            is PersistenceError.ConnectionFailed -> ConnectionFailed(e.message, e.cause)
            is PersistenceError.DeserializationFailed -> DeserializationFailed(e.message, e.cause)
            is PersistenceError.InvalidArgument -> InvalidArgument(e.name, e.message)
            is PersistenceError.OperationFailed -> OperationFailed(e.message)
            is PersistenceError.OperationTimeout -> OperationTimeout(e.message, e.duration)
            is PersistenceError.SerializationFailed -> SerializationFailed(e.message, e.cause)
            is PersistenceError.LockNotHeld -> LockNotHeld(message = e.message)
        }
    }
}
