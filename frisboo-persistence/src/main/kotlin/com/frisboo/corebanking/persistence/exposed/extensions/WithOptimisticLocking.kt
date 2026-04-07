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
package com.frisboo.corebanking.persistence.exposed.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.frisboo.corebanking.persistence.core.errors.PersistenceError
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager

public interface WithOptimisticLocking {
    public val optimisticLockingVersion: Column<Long>
}

/**
 * Performs an optimistic-locking–guarded UPDATE that enforces exactly-one-row semantics.
 *
 * The caller **must** supply a [where] clause that targets a single row (typically a primary-key match).
 * The version check is ANDed with [where] automatically; if the row's current version does not equal
 * [version], zero rows are updated and [PersistenceError.OptimisticLockFailed] is returned.
 *
 * If the [where] clause matches more than one row, the update is **rolled back** via a database
 * savepoint and [PersistenceError.NonUniqueUpdate] is returned — no rows are modified.
 *
 * On success the version column is incremented to `version + 1` and `1` is returned as [Either.Right].
 *
 * @param where  Row-selection predicate — **must** uniquely identify a single row.
 * @param version  Expected current version of the target row.
 * @param body  Column assignments to apply (do **not** set the version column manually).
 * @return [Either.Right] with `1` on success, or [Either.Left] with
 *         [PersistenceError.OptimisticLockFailed] when the version did not match, or
 *         [PersistenceError.NonUniqueUpdate] when multiple rows were affected (rolled back).
 */
public suspend fun <T> T.optimisticUpdate(
    where: () -> Op<Boolean>,
    version: Long,
    body: T.(UpdateStatement) -> Unit,
): Either<PersistenceError, Int> where T : Table, T : WithOptimisticLocking {
    val versionCheck = this@optimisticUpdate.optimisticLockingVersion eq version
    val safeName = this.tableName.replace(Regex("[^a-z0-9_]"), "_").take(MAX_PG_IDENTIFIER - SAVEPOINT_PREFIX.length)
    val savepointName = "$SAVEPOINT_PREFIX$safeName"
    val tx = TransactionManager.current()

    tx.exec("SAVEPOINT $savepointName")

    try {
        val rowsUpdated = try {
            this.update(
                where = { where().and(versionCheck) },
            ) {
                body(it)
                it[this@optimisticUpdate.optimisticLockingVersion] = version + 1
            }
        } catch (ex: Throwable) {
            tx.exec("ROLLBACK TO SAVEPOINT $savepointName")
            throw ex
        }

        return when {
            rowsUpdated == 1 -> rowsUpdated.right()
            rowsUpdated > 1 -> {
                tx.exec("ROLLBACK TO SAVEPOINT $savepointName")
                PersistenceError.NonUniqueUpdate(
                    table = this.tableName,
                    affectedRows = rowsUpdated,
                ).left()
            }
            else -> PersistenceError.OptimisticLockFailed(
                table = this.tableName,
                expectedVersion = version,
            ).left()
        }
    } finally {
        runCatching { tx.exec("RELEASE SAVEPOINT $savepointName") }
    }
}

private const val SAVEPOINT_PREFIX = "opt_upd_"
private const val MAX_PG_IDENTIFIER = 63
