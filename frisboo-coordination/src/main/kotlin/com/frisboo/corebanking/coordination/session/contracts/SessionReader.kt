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
package com.frisboo.corebanking.coordination.session.contracts

/**
 * Read-only contract for querying distributed session state.
 */
public interface SessionReader {
    /**
     * Returns whether a session is currently active.
     *
     * @param sessionId unique session identifier.
     * @return `true` when the session is active.
     */
    public suspend fun isActive(sessionId: String): Boolean

    /**
     * Returns the attribute map for an active session.
     *
     * @param sessionId unique session identifier.
     * @return session attribute map, or `null` when the session does not exist.
     */
    public suspend fun get(sessionId: String): Map<String, String>?

    /**
     * Returns active session identifiers associated with a node.
     *
     * @param nodeId unique node identifier.
     * @return list of active session identifiers owned by the node.
     */
    public suspend fun activeSessions(nodeId: String): List<String>
}
