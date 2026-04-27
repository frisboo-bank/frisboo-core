package com.frisboo.corebanking.core.context

import com.frisboo.corebanking.core.domain.valueobjects.AcceptLanguage
import com.frisboo.corebanking.core.domain.valueobjects.ClientId
import com.frisboo.corebanking.core.domain.valueobjects.ClientIp
import com.frisboo.corebanking.core.domain.valueobjects.CorrelationId
import com.frisboo.corebanking.core.domain.valueobjects.CustomerGuid
import com.frisboo.corebanking.core.domain.valueobjects.DeviceFingerprint
import com.frisboo.corebanking.core.domain.valueobjects.IdempotencyKey
import com.frisboo.corebanking.core.domain.valueobjects.PrincipalId
import com.frisboo.corebanking.core.domain.valueobjects.PrincipalType
import com.frisboo.corebanking.core.domain.valueobjects.RequestId
import com.frisboo.corebanking.core.domain.valueobjects.SessionId
import com.frisboo.corebanking.core.domain.valueobjects.TenantId
import com.frisboo.corebanking.core.domain.valueobjects.UserAgent

public data class RequestContext(
    val clientIp: ClientIp?,
    val correlationId: CorrelationId?,
    val customerGuid: CustomerGuid?,
    val deviceFingerprint: DeviceFingerprint?,
    val idempotencyKey: IdempotencyKey?,
    val principalId: PrincipalId?,
    val principalType: PrincipalType?,
    val requestId: RequestId,
    val sessionId: SessionId?,
    val tenantId: TenantId?,
    val userAgent: UserAgent?,
    val acceptLanguage: AcceptLanguage?,
    val clientId: ClientId?,
) {
    override fun toString(): String =
        "RequestContext(" +
                "requestId=$requestId, " +
                "correlationId=$correlationId, " +
                "tenantId=$tenantId, " +
                "principalType=$principalType" +
                ")"
}
