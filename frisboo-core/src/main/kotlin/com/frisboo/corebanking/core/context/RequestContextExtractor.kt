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
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.web.server.ServerWebExchange

public const val CLIENT_ID_HEADER: String = "X-Client-Id"
public const val CORRELATION_ID_HEADER: String = "X-Correlation-Id"
public const val CUSTOMER_GUID_HEADER: String = "X-Customer-Guid"
public const val DEVICE_FINGERPRINT_HEADER: String = "X-Device-Fingerprint"
public const val IDEMPOTENCY_KEY_HEADER: String = "X-Idempotency-Key"
public const val PRINCIPAL_ID_HEADER: String = "X-Principal-Id"
public const val PRINCIPAL_TYPE_HEADER: String = "X-Principal-Type"
public const val REQUEST_CONTEXT_HEADER: String = "X-Request-Id"
public const val SESSION_ID_HEADER: String = "X-Session-Id"
public const val TENANT_ID_HEADER: String = "X-Tenant-Id"

public object RequestContextExtractor {

    private val logger = KotlinLogging.logger {}

    public fun extract(exchange: ServerWebExchange): RequestContext {
        val headers = exchange.request.headers
        val remoteIp = exchange.request.remoteAddress?.address?.hostAddress

        return RequestContext(
            acceptLanguage = parseOrNull(
                headers.getFirst(HttpHeaders.ACCEPT_LANGUAGE),
                "acceptLanguage",
            ) { AcceptLanguage.of(it) },
            clientId = parseOrNull(headers.getFirst(CLIENT_ID_HEADER), "clientId") { ClientId.of(it) },
            clientIp = parseOrNull(remoteIp, "clientIp") { ClientIp.of(it) },
            correlationId = parseOrNull(
                headers.getFirst(CORRELATION_ID_HEADER),
                "correlationId",
            ) { CorrelationId.of(it) } ?: CorrelationId.generate(),
            customerGuid = parseOrNull(headers.getFirst(CUSTOMER_GUID_HEADER), "customerGuid") { CustomerGuid.of(it) },
            deviceFingerprint = parseOrNull(
                headers.getFirst(DEVICE_FINGERPRINT_HEADER),
                "deviceFingerprint",
            ) { DeviceFingerprint.of(it) },
            idempotencyKey = parseOrNull(
                headers.getFirst(IDEMPOTENCY_KEY_HEADER),
                "idempotencyKey",
            ) { IdempotencyKey.of(it) },
            principalId = parseOrNull(headers.getFirst(PRINCIPAL_ID_HEADER), "principalId") { PrincipalId.of(it) },
            principalType = parseOrNull(
                headers.getFirst(PRINCIPAL_TYPE_HEADER),
                "principalType",
            ) { PrincipalType.valueOf(it) },
            requestId = parseOrNull(headers.getFirst(REQUEST_CONTEXT_HEADER), "requestId") { RequestId.of(it) }
                ?: RequestId.generate(),
            sessionId = parseOrNull(headers.getFirst(SESSION_ID_HEADER), "sessionId") { SessionId.of(it) },
            tenantId = parseOrNull(headers.getFirst(TENANT_ID_HEADER), "tenantId") { TenantId.of(it) },
            userAgent = parseOrNull(headers.getFirst(HttpHeaders.USER_AGENT), "userAgent") { UserAgent.of(it) },
        )
    }

    private inline fun <reified T> parseOrNull(value: String?, fieldName: String, parser: (String) -> T): T? {
        if (value.isNullOrBlank()) {
            return null
        }

        return runCatching { parser(value) }
            .onFailure { logger.warn(it) { "Failed to parse $fieldName: $value" } }
            .getOrNull()

    }
}
