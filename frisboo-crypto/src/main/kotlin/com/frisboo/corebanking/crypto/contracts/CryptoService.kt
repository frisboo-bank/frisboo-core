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
package com.frisboo.corebanking.crypto.contracts

/**
 * Authenticated encryption with associated data (AEAD).
 *
 * **Associated data** is authenticated but _not_ encrypted. It binds ciphertext to a specific
 * context (e.g. scope, tenant, record ID) so that ciphertext cannot be silently moved between
 * contexts. In banking workloads, always supply meaningful associated data.
 *
 * A recommended AAD format: `version || tenantId || objectType || objectId || fieldName`.
 */
public interface CryptoService {
    /**
     * Encrypts [plaintext] with [associatedData] binding.
     * @param plaintext raw bytes to encrypt — **must not be empty**
     * @param associatedData context binding (e.g. scope prefix); must match on decrypt — **must not be empty**
     * @return ciphertext bytes (format is implementation-defined)
     * @throws com.frisboo.corebanking.crypto.errors.CryptoException.EncryptionFailed on failure
     */
    public fun encrypt(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray

    /**
     * Decrypts [ciphertext] previously produced by [encrypt] with matching [associatedData].
     * @param ciphertext bytes produced by [encrypt]
     * @param associatedData must match the value used during encryption — **must not be empty**
     * @return original plaintext
     * @throws com.frisboo.corebanking.crypto.errors.CryptoException.DecryptionFailed on failure
     */
    public fun decrypt(
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray
}
