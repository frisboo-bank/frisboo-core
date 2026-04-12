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
package com.frisboo.corebanking.registry.errors

import com.frisboo.corebanking.persistence.core.errors.PersistenceError

public sealed interface RegistryError {

    /**
     * Errors related to connectivity issues with the underlying registry storage or service.
     */
    public data class ConnectionFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : RegistryError

    /**
     * Errors related to scope violations, such as attempting to access or modify
     * registry entries outside of the allowed team or name scope.
     */
    public data class ScopeViolation(
        public val message: String,
    ) : RegistryError

    /**
     * Errors related to timeouts during registry operations.
     */
    public data class OperationTimeout(
        public val message: String,
        public val timeoutMillis: Long,
    ) : RegistryError

    /**
     * Errors related to invalid arguments provided to registry operations.
     */
    public data class InvalidArgument(
        public val name: String,
        public val message: String,
    ) : RegistryError

    /**
     * Errors related to serialization of registry values.
     */
    public data class SerializationFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : RegistryError

    /**
     * Errors related to deserialization of registry values.
     */
    public data class DeserializationFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : RegistryError

    /**
     * Errors related to failures during the call of the value factory.
     */
    public data class ComputationFailed(
        public val message: String,
        public val cause: Throwable? = null,
    ) : RegistryError

    /**
     * Errors related to response size exceeding safety limits.
     */
    public data class ResponseTooLarge(
        public val sizeBytes: Int,
        public val message: String = "SCAN response exceeds safety limit ($sizeBytes bytes)",
    ) : RegistryError

    public companion object {
        public fun fromPersistenceError(e: PersistenceError): RegistryError = when (e) {
            is PersistenceError.ConnectionFailed -> ConnectionFailed(e.message, e.cause)
            is PersistenceError.DeserializationFailed -> DeserializationFailed(e.message, e.cause)
            is PersistenceError.InvalidArgument -> InvalidArgument(e.name, e.message)
            is PersistenceError.OperationTimeout -> OperationTimeout(e.message, e.timeoutMillis)
            is PersistenceError.SerializationFailed -> SerializationFailed(e.message, e.cause)
            else -> throw IllegalArgumentException("Unmapped PersistenceError subtype: ${e::class.simpleName}")
        }
    }
}
