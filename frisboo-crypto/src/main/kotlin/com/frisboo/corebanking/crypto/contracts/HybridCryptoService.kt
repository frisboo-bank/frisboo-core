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
 * Hybrid (asymmetric) encryption — encrypt with a public key, decrypt with the corresponding private key.
 *
 * Use for key exchange scenarios where the encryptor does not hold the decryption key
 * (e.g. encrypting payment data from a merchant using the bank's public key).
 *
 * **Context info** is authenticated but _not_ encrypted. It binds ciphertext to a specific context
 * so that ciphertext cannot be replayed in a different context. In banking workloads, always supply
 * meaningful context info.
 *
 * A recommended context format: `version || purpose || tenantId || transactionId`.
 */
public interface HybridCryptoService {
    /**
     * Encrypts [plaintext] with the public key. Anyone with the public key can encrypt,
     * but only the private-key holder can decrypt.
     *
     * @param plaintext raw bytes to encrypt — **must not be empty**
     * @param contextInfo context binding (authenticated but not encrypted); must match on decrypt — **must not be empty**
     * @return ciphertext bytes
     * @throws com.frisboo.corebanking.crypto.errors.CryptoException.HybridEncryptionFailed if encryption fails
     */
    public fun encrypt(
        plaintext: ByteArray,
        contextInfo: ByteArray,
    ): ByteArray

    /**
     * Decrypts [ciphertext] previously produced by [encrypt] with the corresponding public key.
     *
     * @param ciphertext bytes produced by [encrypt]
     * @param contextInfo must match the value used during encryption — **must not be empty**
     * @return original plaintext
     * @throws com.frisboo.corebanking.crypto.errors.CryptoException.HybridDecryptionFailed if decryption fails
     */
    public fun decrypt(
        ciphertext: ByteArray,
        contextInfo: ByteArray,
    ): ByteArray
}
