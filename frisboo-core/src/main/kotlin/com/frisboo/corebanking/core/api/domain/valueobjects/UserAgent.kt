package com.frisboo.corebanking.core.api.domain.valueobjects

@JvmInline
public value class UserAgent(public val userAgent: String) {
    init {
        require(userAgent.isNotBlank()) { "UserAgent cannot be blank" }
    }
}
