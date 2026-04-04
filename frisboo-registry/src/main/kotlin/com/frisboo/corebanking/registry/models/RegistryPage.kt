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
package com.frisboo.corebanking.registry.models

public data class RegistryPage<out T>(
    public val items: List<T>,
    public val nextCursor: String?,
)

internal fun <T> buildPage(
    allKeys: List<T>,
    cursor: String?,
    limit: Int,
    maxPageSize: Int,
): RegistryPage<T> {
    val safeLimit = limit.coerceIn(1, maxPageSize)
    val startIndex =
        if (cursor != null) {
            val parsed = requireNotNull(cursor.toIntOrNull()) { "Invalid cursor: '$cursor'" }
            require(parsed >= 0) { "Cursor must not be negative: '$cursor'" }
            parsed
        } else {
            0
        }
    val page = allKeys.drop(startIndex).take(safeLimit)
    val nextCursor =
        if (startIndex + safeLimit < allKeys.size) {
            (startIndex + safeLimit).toString()
        } else {
            null
        }
    return RegistryPage(items = page, nextCursor = nextCursor)
}
