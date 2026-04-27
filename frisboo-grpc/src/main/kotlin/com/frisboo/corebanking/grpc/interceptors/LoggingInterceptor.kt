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
package com.frisboo.corebanking.grpc.interceptors

import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor

public class LoggingInterceptor : ServerInterceptor {
    private companion object {
        private val logger = KotlinLogging.logger { }

        /** Header names whose values must never appear in logs. */
        private val SENSITIVE_HEADERS: Set<String> =
            setOf(
                "authorization",
                "cookie",
                "set-cookie",
                "x-api-key",
                "x-auth-token",
                "proxy-authorization",
            )
    }

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {
        logger.info {
            "Grpc call on " +
                "service: ${call.methodDescriptor.serviceName}, " +
                "method: ${call.methodDescriptor.bareMethodName}, " +
                "headers: ${redactHeaders(headers)}"
        }
        return next.startCall(call, headers)
    }

    private fun redactHeaders(headers: Metadata): String =
        headers
            .keys()
            .joinToString { key ->
                if (key.lowercase() in
                    SENSITIVE_HEADERS
                ) {
                    "$key=<REDACTED>"
                } else {
                    "$key=${headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))}"
                }
            }
}
