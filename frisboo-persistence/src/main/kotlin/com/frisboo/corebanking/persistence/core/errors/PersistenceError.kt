package com.frisboo.corebanking.persistence.core.errors

import kotlin.time.Duration

public sealed interface PersistenceError {

    /**
     * Errors related to connection failures to the persistence layer.
     */
    public data class ConnectionFailed(
        val message: String,
        val cause: Throwable? = null,
    ) : PersistenceError

    /**
     * Errors related to failure during persistence operations.
     */
    public data class OperationFailed(
        val message: String,
        val cause: Throwable? = null,
    ) : PersistenceError

    /**
     * Errors related to timeouts during persistence operations.
     */
    public data class OperationTimeout(
        val message: String,
        val duration: Duration,
    ) : PersistenceError

    /**
     * Errors related to invalid arguments provided to persistence operations.
     */
    public data class InvalidArgument(
        val name: String,
        val message: String,
    ) : PersistenceError

    /**
     * Errors related to serialization of persistence values.
     */
    public data class SerializationFailed(
        val message: String,
        val cause: Throwable? = null,
    ) : PersistenceError

    /**
     * Errors related to deserialization of persistence values.
     */
    public data class DeserializationFailed(
        val message: String,
        val cause: Throwable? = null,
    ) : PersistenceError

    /**
     * Errors related to lock acquisition failures.
     */
    public data class LockNotHeld(
        val message: String,
    ) : PersistenceError

    public data class LockAlreadyHeld(
        val message: String,
    ) : PersistenceError
}
