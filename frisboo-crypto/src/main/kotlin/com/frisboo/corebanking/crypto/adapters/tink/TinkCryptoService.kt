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

import com.frisboo.corebanking.crypto.contracts.CryptoService
import com.frisboo.corebanking.crypto.errors.CryptoException
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle

/**
 * Google Tink AEAD implementation of [CryptoService].
 *
 * Ensures Tink primitives are registered before obtaining the AEAD primitive from [keysetHandle].
 *
 * @throws CryptoException.InitializationFailed if [keysetHandle] does not contain a valid AEAD key
 */
public class TinkCryptoService(
    keysetHandle: KeysetHandle,
) : CryptoService {
    init {
        TinkCryptoInitializer.initialize()
    }

    private val aead: Aead = keysetHandle.tinkPrimitive("Failed to obtain AEAD primitive from keyset")

    override fun encrypt(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        requireNonEmptyPlaintext(plaintext)
        requireNonEmptyAssociatedData(associatedData)
        return wrapCryptoOp(CryptoException::EncryptionFailed, "Encryption failed") {
            aead.encrypt(plaintext, associatedData)
        }
    }

    override fun decrypt(
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        requireNonEmptyCiphertext(ciphertext)
        requireNonEmptyAssociatedData(associatedData)
        return wrapCryptoOp(CryptoException::DecryptionFailed, "Decryption failed") {
            aead.decrypt(ciphertext, associatedData)
        }
    }
}
