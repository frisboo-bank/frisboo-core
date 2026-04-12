package com.frisboo.corebanking.persistence.core.errors

public sealed interface PersistenceError {

    /**
     * The `where` clause did not match any rows.
     *
     * [optimisticUpdate][com.frisboo.corebanking.persistence.exposed.extensions.optimisticUpdate]
     * enforces exactly-one-row semantics; if the predicate is not satisfied the update is
     * **rolled back** and this error is returned.
     */
    public data class OptimisticLockFailed(
        public val table: String,
        public val expectedVersion: Long,
    ) : PersistenceError

    /**
     * The `where` clause matched more than one row.
     *
     * [optimisticUpdate][com.frisboo.corebanking.persistence.exposed.extensions.optimisticUpdate]
     * enforces exactly-one-row semantics; if the predicate is not unique the update is
     * **rolled back** and this error is returned.
     */
    public data class NonUniqueUpdate(
        public val table: String,
        public val affectedRows: Int,
    ) : PersistenceError

    /**
     * Errors related to connection failures to the persistence layer.
     */
    public data class ConnectionFailed(
        val message: String,
        val cause: Throwable? = null,
    ) : PersistenceError

    /**
     * Errors related to timeouts during persistence operations.
     */
    public data class OperationTimeout(
        val message: String,
        val timeoutMillis: Long,
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
}
