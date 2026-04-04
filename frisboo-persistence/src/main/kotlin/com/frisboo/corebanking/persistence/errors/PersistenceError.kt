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
package com.frisboo.corebanking.persistence.errors

public sealed interface PersistenceError {
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
}
