package com.frisboo.corebanking.persistence.exposed.errors

public sealed interface ExposedError {

    /**
     * The `where` clause did not match any rows.
     */
    public data class OptimisticLockFailed(
        public val table: String,
        public val expectedVersion: Long,
    ) : ExposedError

    /**
     * The `where` clause matched more than one row.
     */
    public data class NonUniqueUpdate(
        public val table: String,
        public val affectedRows: Int,
    ) : ExposedError

    /**
     * A requested row was not found.
     */
    public data class RowNotFound(
        public val table: String,
    ) : ExposedError
}
