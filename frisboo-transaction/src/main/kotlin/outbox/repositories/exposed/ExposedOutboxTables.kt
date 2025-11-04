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
package outbox.repositories.exposed

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.time.OffsetDateTime
import java.util.UUID

private const val SCHEMA_NAME = "transaction"

internal object OutboxTable : Table("$SCHEMA_NAME.outbox") {
    val eventId: Column<UUID> = uuid("outbox_id")
    val name: Column<String> = varchar("name", length = 255)
    val subject: Column<String> = varchar("subject", length = 255)
    val data: Column<ByteArray> = binary("data")
    val metadata: Column<ByteArray> = binary("metadata")
    val sentAt: Column<OffsetDateTime> = timestampWithTimeZone("sent_at")
    val receivedAt: Column<OffsetDateTime?> = timestampWithTimeZone("received_at").nullable()
    val version: Column<Long> = long("version")
    val createdAt: Column<OffsetDateTime> = timestampWithTimeZone("created_at")
    val updatedAt: Column<OffsetDateTime> = timestampWithTimeZone("updated_at")

    override val primaryKey: PrimaryKey = PrimaryKey(eventId, name = "pk_outbox")

    init {
        index("outbox_unpublished_idx", false, receivedAt) { receivedAt eq null }
    }
}
