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
package com.frisboo.corebanking.registry.serialization

import com.frisboo.corebanking.registry.contracts.RegistrySerializer
import com.frisboo.corebanking.registry.models.RegistryScope
import com.frisboo.corebanking.crypto.errors.CryptoException
import com.frisboo.corebanking.crypto.contracts.CryptoService

/**
 * Encryption decorator; AAD is bound to [scope] to prevent cross-scope ciphertext reuse.
 */
public class EncryptingSerializerImpl<T>(
    private val delegate: RegistrySerializer<T>,
    private val cryptoService: CryptoService,
    private val scope: RegistryScope,
) : RegistrySerializer<T> {

    private val associatedData: ByteArray = scope.prefix.toByteArray(Charsets.UTF_8)

    override fun serialize(value: T): ByteArray {
        val plaintext = delegate.serialize(value)
        return cryptoService.encrypt(plaintext, associatedData)
    }

    override fun deserialize(data: ByteArray): T {
        val plaintext =
            try {
                cryptoService.decrypt(data, associatedData)
            } catch (e: CryptoException) {
                throw CryptoException.DecryptionFailed(
                    "Decryption failed for scope '${scope.prefix}': corrupted or tampered ciphertext",
                    e,
                )
            }
        return delegate.deserialize(plaintext)
    }
}
