package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class UserAgent private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): UserAgent {
            require(value.isNotBlank()) { "userAgent must not be blank" }
            require(value.length <= 512) { "userAgent too long (max 512)" }
            return UserAgent(value)
        }
    }
}
