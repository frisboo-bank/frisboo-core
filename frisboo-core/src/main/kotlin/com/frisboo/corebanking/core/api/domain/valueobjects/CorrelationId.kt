package com.frisboo.corebanking.core.api.domain.valueobjects

@JvmInline
public value class CorrelationId(public val correlationId: String) {
    init {
        require(correlationId.isNotBlank()) { "CorrelationId cannot be blank" }
    }
}
