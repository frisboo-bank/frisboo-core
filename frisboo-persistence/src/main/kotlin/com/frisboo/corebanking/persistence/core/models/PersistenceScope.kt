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
package com.frisboo.corebanking.persistence.core.models

private val FORBIDDEN_CHARS = setOf(':', '*', '?', '[', ']', '_')

private const val MIN_LENGTH = 1
private const val MAX_LENGTH = 63

public data class PersistenceScope(
    public val name: String,
    public val team: String,
) {
    public val prefix: String get() = "$team:$name"

    init {
        require(name.length in MIN_LENGTH..MAX_LENGTH) {
            "StateManager scope name must be between $MIN_LENGTH and $MAX_LENGTH characters"
        }
        require(team.length in MIN_LENGTH..MAX_LENGTH) {
            "StateManager scope team must be between $MIN_LENGTH and $MAX_LENGTH characters"
        }
        require(FORBIDDEN_CHARS.none { it in name }) {
            "StateManager scope name contains forbidden characters: $FORBIDDEN_CHARS"
        }
        require(FORBIDDEN_CHARS.none { it in team }) {
            "StateManager scope team contains forbidden characters: $FORBIDDEN_CHARS"
        }
    }
}
