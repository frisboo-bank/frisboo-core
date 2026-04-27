package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class ClientIp private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): ClientIp {
            require(value.isNotBlank()) { "clientIp must not be blank" }
            require(value.length <= 45) { "clientIp too long (max 45 chars for IPv6)" }
            return ClientIp(value)
        }
    }
}
