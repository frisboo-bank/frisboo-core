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
