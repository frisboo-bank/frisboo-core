package com.frisboo.corebanking.core.domain.valueobjects

@JvmInline
public value class AcceptLanguage private constructor(public val value: String) {
    public companion object {
        public fun of(value: String): AcceptLanguage {
            require(value.isNotBlank()) { "acceptLanguage must not be blank" }
            require(value.length <= 128) { "acceptLanguage too long (max 128)" }
            return AcceptLanguage(value)
        }
    }
}
