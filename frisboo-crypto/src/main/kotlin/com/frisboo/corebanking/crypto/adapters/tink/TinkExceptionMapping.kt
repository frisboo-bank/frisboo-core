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
package com.frisboo.corebanking.crypto.adapters.tink

import com.frisboo.corebanking.crypto.errors.CryptoException
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import java.security.GeneralSecurityException

internal inline fun <T> wrapInit(message: String, block: () -> T): T =
    try {
        block()
    } catch (e: GeneralSecurityException) {
        throw CryptoException.InitializationFailed(message, e)
    }

internal inline fun <T> wrapCryptoOp(wrap: (String, Throwable) -> CryptoException, message: String, block: () -> T): T =
    try {
        block()
    } catch (e: GeneralSecurityException) {
        throw wrap(message, e)
    }

internal inline fun <reified T> KeysetHandle.tinkPrimitive(failureMessage: String): T =
    wrapInit(failureMessage) { getPrimitive(RegistryConfiguration.get(), T::class.java) }

internal fun requireNonEmptyPlaintext(plaintext: ByteArray) {
    require(plaintext.isNotEmpty()) { "plaintext must not be empty" }
}

internal fun requireNonEmptyCiphertext(ciphertext: ByteArray) {
    require(ciphertext.isNotEmpty()) { "ciphertext must not be empty" }
}

internal fun requireNonEmptyAssociatedData(associatedData: ByteArray) {
    require(associatedData.isNotEmpty()) {
        "associatedData must not be empty — supply a context binding (e.g. tenantId:objectType:objectId)"
    }
}

internal fun requireNonEmptyContextInfo(contextInfo: ByteArray) {
    require(contextInfo.isNotEmpty()) {
        "contextInfo must not be empty — supply a context binding (e.g. purpose:tenantId:transactionId)"
    }
}
