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
import arrow.core.raise.either
import com.frisboo.corebanking.persistence.core.utils.toSafeSqlIdentifier
import com.frisboo.corebanking.persistence.exposed.errors.ExposedError
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.update

public interface WithOptimisticLocking {
    public val optimisticLockingVersion: Column<Long>
}

public const val SAVEPOINT_PREFIX: String = "opt_upd_"
public const val MAX_PG_IDENTIFIER: Int = 63

/**
 * Performs an optimistic-locking–guarded UPDATE that enforces exactly-one-row semantics.
 */
public inline fun <T> T.optimisticUpdate(
    crossinline where: () -> Op<Boolean>,
    version: Long,
    crossinline body: T.(UpdateStatement) -> Unit,
): Either<ExposedError, Int> where T : Table, T : WithOptimisticLocking = either {
    val versionCheck = this@optimisticUpdate.optimisticLockingVersion eq version
    val safeName = tableName.toSafeSqlIdentifier(MAX_PG_IDENTIFIER - SAVEPOINT_PREFIX.length)
    val savepointName = "$SAVEPOINT_PREFIX$safeName"
    val tx = TransactionManager.current()

    tx.exec("SAVEPOINT $savepointName")

    try {
        val rowsUpdated = try {
            update(
                where = { where().and(versionCheck) },
            ) {
                body(it)
                it[this@optimisticUpdate.optimisticLockingVersion] = version + 1
            }
        } catch (ex: Throwable) {
            tx.exec("ROLLBACK TO SAVEPOINT $savepointName")
            throw ex
        }

        when (rowsUpdated) {
            1 -> rowsUpdated
            0 -> {
                val exists = !select(intLiteral(1)).where(where()).limit(1).empty()
                if (exists) {
                    raise(ExposedError.OptimisticLockFailed(tableName, version))
                } else {
                    raise(ExposedError.RowNotFound(tableName))
                }
            }

            else -> {
                tx.exec("ROLLBACK TO SAVEPOINT $savepointName")
                raise(ExposedError.NonUniqueUpdate(tableName, rowsUpdated))
            }
        }
    } finally {
        runCatching { tx.exec("RELEASE SAVEPOINT $savepointName") }
    }
}
