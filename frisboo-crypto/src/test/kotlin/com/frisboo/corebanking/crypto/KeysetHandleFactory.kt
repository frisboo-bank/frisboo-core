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
package com.frisboo.corebanking.crypto

import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.hybrid.HpkeParameters

/**
 * Test-only factory for generating in-memory [KeysetHandle] instances.
 *
 * Generates ephemeral keys with no persistence or KMS envelope protection.
 * In production, load keysets from an encrypted store or KMS.
 */
internal object KeysetHandleFactory {
    fun generateAes256Gcm(): KeysetHandle = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)

    fun generateHybridHpke(): KeysetHandle =
        KeysetHandle.generateNew(
            HpkeParameters
                .builder()
                .setKemId(HpkeParameters.KemId.DHKEM_X25519_HKDF_SHA256)
                .setKdfId(HpkeParameters.KdfId.HKDF_SHA256)
                .setAeadId(HpkeParameters.AeadId.AES_256_GCM)
                .setVariant(HpkeParameters.Variant.TINK)
                .build(),
        )
}
