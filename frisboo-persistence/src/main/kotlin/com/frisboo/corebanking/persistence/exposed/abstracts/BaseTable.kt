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
package com.frisboo.corebanking.persistence.exposed.abstracts

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.time.OffsetDateTime

/**
 * Abstract base table providing audit columns and an application-managed optimistic locking version.
 */
public abstract class BaseTable(
    name: String,
) : Table(name) {
    public val version: Column<Long> = long("version").default(1)

    public val createdAt: Column<OffsetDateTime> = timestampWithTimeZone("created_at").databaseGenerated()
    public val updatedAt: Column<OffsetDateTime> = timestampWithTimeZone("updated_at").databaseGenerated()
}
