package com.frisboo.corebanking.core.domain.valueobjects

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
@OptIn(ExperimentalUuidApi::class)
public value class SessionId private constructor(public val value: Uuid) {
    public companion object {
        @OptIn(ExperimentalUuidApi::class)
        public fun generate(): SessionId = SessionId(Uuid.random())

        public fun of(value: Uuid): SessionId = SessionId(value)
        public fun of(value: String): SessionId {
            val uuid = try {
                Uuid.parse(value)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("sessionId must be a valid UUID: $value")
            }
            return SessionId(uuid)
        }
    }
}
