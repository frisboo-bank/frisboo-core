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

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.update

public interface WithOptimisticLocking {
    public val optimisticLockingVersion: Column<Long>
}

public suspend fun <T> T.optimisticUpdate(
    where: (() -> Op<Boolean>)? = null,
    version: Long,
    limit: Int? = null,
    body: T.(UpdateStatement) -> Unit,
): Int where T : Table, T : WithOptimisticLocking {
    val whereClause: () -> Op<Boolean> = {
        val isEqualVersion = this@optimisticUpdate.optimisticLockingVersion eq version
        when (where != null) {
            true -> where().and(isEqualVersion)
            else -> isEqualVersion
        }
    }

    val updateVersionStatement: T.(UpdateStatement) -> Unit = {
        body(it)
        it[this.optimisticLockingVersion] = version + 1
    }

    return this.update(
        where = whereClause,
        limit = limit,
        body = updateVersionStatement,
    )
}

public suspend fun <T> T.optimisticUpdate(
    version: Long,
    body: T.(UpdateStatement) -> Unit,
): Int where T : Table, T : WithOptimisticLocking =
    this.optimisticUpdate(
        where = null,
        version = version,
        limit = null,
        body = body,
    )
