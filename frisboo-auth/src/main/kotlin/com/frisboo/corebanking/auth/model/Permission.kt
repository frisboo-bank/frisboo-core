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
package com.frisboo.corebanking.auth.model

/**
 * A fine-grained permission representing an allowed action on a resource type.
 *
 * Convention: `"<resource>:<action>"` (e.g., `"account:read"`, `"payment:create"`).
 */
public data class Permission(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Permission value must not be blank" }
    }
}

public data class Role(
    val name: String,
    val permissions: Set<Permission>,
) {
    init {
        require(name.isNotBlank()) { "Role name must not be blank" }
    }
}
