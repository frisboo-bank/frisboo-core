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
 * Represents an authenticated identity in the system.
 *
 * All access control decisions are made against a [Principal].
 * The sealed hierarchy ensures exhaustive handling in authorization logic.
 */
public sealed interface Principal {
    public val id: String
    public val attributes: Map<String, String>
}

public data class UserPrincipal(
    override val id: String,
    val tenantId: String,
    val roles: Set<Role>,
    override val attributes: Map<String, String> = emptyMap(),
) : Principal

public data class ServicePrincipal(
    override val id: String,
    val serviceName: String,
    val permissions: Set<Permission>,
    override val attributes: Map<String, String> = emptyMap(),
) : Principal
