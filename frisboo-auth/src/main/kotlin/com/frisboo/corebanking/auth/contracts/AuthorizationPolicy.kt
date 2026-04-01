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
package com.frisboo.corebanking.auth.contracts

import arrow.core.Either
import com.frisboo.corebanking.auth.model.Principal

/**
 * Contract for evaluating whether a [Principal] is authorized to perform an action on a resource.
 *
 * Implementations encode organization-specific access control rules
 * (RBAC, ABAC, or hybrid). Multiple policies can be composed via decoration.
 */
public interface AuthorizationPolicy {
    /**
     * Evaluates whether [principal] may perform [action] on [resourceType]/[resourceId].
     *
     * @param principal the authenticated identity requesting access.
     * @param action the operation being attempted (e.g., "read", "create", "approve").
     * @param resourceType the type of resource (e.g., "account", "payment").
     * @param resourceId optional specific resource identifier for instance-level checks.
     * @param context additional context for attribute-based decisions (e.g., IP, time of day).
     * @return [Either.Right] with [AuthorizationDecision] on success,
     *         [Either.Left] with [AuthorizationError] on evaluation failure.
     */
    public suspend fun evaluate(
        principal: Principal,
        action: String,
        resourceType: String,
        resourceId: String? = null,
        context: Map<String, String> = emptyMap(),
    ): Either<AuthorizationError, AuthorizationDecision>
}

public enum class AuthorizationDecision {
    PERMIT,
    DENY,
}

public sealed interface AuthorizationError {
    public data class PolicyUnavailable(
        val cause: Throwable? = null,
    ) : AuthorizationError

    public data class InvalidPrincipal(
        val reason: String,
    ) : AuthorizationError
}
