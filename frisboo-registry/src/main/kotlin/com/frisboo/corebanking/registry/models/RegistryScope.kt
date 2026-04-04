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

private const val IDENTIFIER_PATTERN = "^[a-z]([a-z0-9\\-]{0,61}[a-z0-9])?$"
private val VALID_IDENTIFIER = Regex(IDENTIFIER_PATTERN)

/**
 * Defines the namespace for a registry instance, scoped per team.
 *
 * Validation enforces DNS-like naming to prevent key-injection attacks
 * (e.g., team names containing `:` or `*` could leak across scopes in Redis).
 */
public data class RegistryScope(
    public val name: String,
    public val team: String,
) {
    public val prefix: String get() = "$team:$name"

    init {
        require(VALID_IDENTIFIER.matches(name)) {
            "Registry scope name must match $IDENTIFIER_PATTERN, got: '$name'"
        }
        require(VALID_IDENTIFIER.matches(team)) {
            "Registry scope team must match $IDENTIFIER_PATTERN, got: '$team'"
        }
    }
}
