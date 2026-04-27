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

import com.frisboo.corebanking.crypto.contracts.HybridCryptoService
import com.frisboo.corebanking.crypto.errors.CryptoException
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration

/**
 * Google Tink HPKE (Hybrid Public Key Encryption) implementation of [HybridCryptoService].
 *
 * Requires a private [KeysetHandle] for decryption. The public keyset handle
 * is derived internally for encryption.
 *
 * Ensures Tink primitives are registered before obtaining hybrid primitives from [privateKeysetHandle].
 *
 * @throws CryptoException.InitializationFailed if [privateKeysetHandle] does not contain a valid hybrid key
 */
public class TinkHybridCryptoService(
    privateKeysetHandle: KeysetHandle,
) : HybridCryptoService {
    init {
        TinkCryptoInitializer.initialize()
    }

    private val hybridEncrypt: HybridEncrypt =
        wrapInit("Failed to obtain HybridEncrypt primitive from keyset") {
            privateKeysetHandle.publicKeysetHandle
                .getPrimitive(RegistryConfiguration.get(), HybridEncrypt::class.java)
        }

    private val hybridDecrypt: HybridDecrypt =
        privateKeysetHandle.tinkPrimitive("Failed to obtain HybridDecrypt primitive from keyset")

    override fun encrypt(
        plaintext: ByteArray,
        contextInfo: ByteArray,
    ): ByteArray {
        requireNonEmptyPlaintext(plaintext)
        requireNonEmptyContextInfo(contextInfo)
        return wrapCryptoOp(CryptoException::HybridEncryptionFailed, "Hybrid encryption failed") {
            hybridEncrypt.encrypt(plaintext, contextInfo)
        }
    }

    override fun decrypt(
        ciphertext: ByteArray,
        contextInfo: ByteArray,
    ): ByteArray {
        requireNonEmptyCiphertext(ciphertext)
        requireNonEmptyContextInfo(contextInfo)
        return wrapCryptoOp(CryptoException::HybridDecryptionFailed, "Hybrid decryption failed") {
            hybridDecrypt.decrypt(ciphertext, contextInfo)
        }
    }
}
