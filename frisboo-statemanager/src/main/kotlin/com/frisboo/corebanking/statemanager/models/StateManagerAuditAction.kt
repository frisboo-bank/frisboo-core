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
package com.frisboo.corebanking.statemanager.models

@JvmInline
public value class StateManagerAuditAction(
    public val value: String,
) {
    public companion object {
        public val PUT: StateManagerAuditAction = StateManagerAuditAction("statemanager.put")
        public val GET_OR_PUT: StateManagerAuditAction = StateManagerAuditAction("statemanager.getOrPut")
        public val EVICT: StateManagerAuditAction = StateManagerAuditAction("statemanager.evict")
        public val SET_TTL: StateManagerAuditAction = StateManagerAuditAction("statemanager.setTTL")
    }
}
