package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class ClientId private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): ClientId {
            require(value.isNotBlank()) { "clientId must not be blank" }
            require(value.length <= 128) { "clientId too long (max 128)" }
            return ClientId(value)
        }
    }
}
