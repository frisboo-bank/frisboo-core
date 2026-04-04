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
import com.google.crypto.tink.hybrid.HybridConfig
import java.security.GeneralSecurityException

/**
 * Single entry-point for Tink primitive registration.
 *
 * [HybridConfig.register] internally registers AEAD, MAC, and Hybrid primitives,
 * so a separate [com.google.crypto.tink.aead.AeadConfig.register] call is unnecessary.
 *
 * Safe to call multiple times and concurrently; the [lazy] delegate guarantees
 * exactly-once execution and blocks any concurrent caller until registration completes.
 *
 * @throws CryptoException.InitializationFailed if Tink primitive registration fails
 */
public object TinkCryptoInitializer {
    private val registration: Unit by lazy {
        try {
            HybridConfig.register()
        } catch (e: GeneralSecurityException) {
            throw CryptoException.InitializationFailed("Tink primitive registration failed", e)
        }
    }

    /** Registers all Tink primitives required by this module. Safe to call concurrently. */
    public fun initialize() {
        registration
    }
}
