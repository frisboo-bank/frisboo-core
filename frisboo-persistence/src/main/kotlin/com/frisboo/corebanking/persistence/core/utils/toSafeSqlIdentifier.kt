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
package com.frisboo.corebanking.persistence.core.utils

/**
 * Converts a string into a safe SQL identifier by replacing invalid characters with underscores.
 *
 * @param maxLength The maximum length of the resulting identifier.
 * @return A string that is a valid SQL identifier
 */
public fun String.toSafeSqlIdentifier(maxLength: Int): String {
    val sb = StringBuilder(minOf(length, maxLength))

    for (ch in this) {
        if (sb.length >= maxLength) break
        when (ch) {
            in 'a'..'z', in 'A'..'Z', in '0'..'9', '_' -> sb.append(ch)
            else -> if (sb.length < maxLength) sb.append('_')
        }
    }

    fun Char.isValidIdentifierStart() = this in 'a'..'z' || this in 'A'..'Z' || this == '_'

    if (sb.isNotEmpty() && !sb[0].isValidIdentifierStart()) {
        if (sb.length >= maxLength) {
            sb.deleteCharAt(sb.length - 1)
        }
        sb.insert(0, '_')
    }

    return sb.toString()
}
