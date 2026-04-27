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
package com.frisboo.corebanking.crypto.errors

/**
 * Crypto operation failure — wraps all underlying provider exceptions into a single sealed hierarchy.
 *
 * Subclasses distinguish **initialization** (keyset/config problems detected at construction)
 * from **runtime** failures (encrypt/decrypt operation errors).
 */
public sealed class CryptoException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /** Keyset or provider misconfiguration detected during service construction. */
    public class InitializationFailed(
        message: String,
        cause: Throwable? = null,
    ) : CryptoException(message, cause)

    /** Encryption failed (e.g. invalid key state). */
    public class EncryptionFailed(
        message: String,
        cause: Throwable? = null,
    ) : CryptoException(message, cause)

    /** Decryption failed — tampered ciphertext, wrong key, or mismatched AAD. */
    public class DecryptionFailed(
        message: String,
        cause: Throwable? = null,
    ) : CryptoException(message, cause)

    /** Hybrid encryption failed (e.g. invalid public key). */
    public class HybridEncryptionFailed(
        message: String,
        cause: Throwable? = null,
    ) : CryptoException(message, cause)

    /** Hybrid decryption failed — wrong private key, tampered ciphertext, or mismatched context. */
    public class HybridDecryptionFailed(
        message: String,
        cause: Throwable? = null,
    ) : CryptoException(message, cause)
}
