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

import com.frisboo.corebanking.crypto.adapters.tink.TinkCryptoInitializer
import com.frisboo.corebanking.crypto.adapters.tink.TinkCryptoService
import com.frisboo.corebanking.crypto.errors.CryptoException
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.PredefinedAeadParameters
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class TinkCryptoServiceTest :
    StringSpec({
        TinkCryptoInitializer.initialize()

        "encrypt and decrypt round-trip with AAD" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val plaintext = "frisboo-crypto-with-aad".encodeToByteArray()
            val associatedData = "tenant:bank:scope".encodeToByteArray()

            val ciphertext = cryptoService.encrypt(plaintext, associatedData)
            val decrypted = cryptoService.decrypt(ciphertext, associatedData)

            ciphertext shouldNotBe null
            decrypted shouldBe plaintext
        }

        "encrypt rejects empty plaintext" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)

            shouldThrow<IllegalArgumentException> {
                cryptoService.encrypt(byteArrayOf(), "aad".encodeToByteArray())
            }
        }

        "encrypt rejects empty associatedData" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)

            shouldThrow<IllegalArgumentException> {
                cryptoService.encrypt("plaintext".encodeToByteArray(), byteArrayOf())
            }
        }

        "decrypt rejects empty ciphertext" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)

            shouldThrow<IllegalArgumentException> {
                cryptoService.decrypt(byteArrayOf(), "aad".encodeToByteArray())
            }
        }

        "decrypt rejects empty associatedData" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val ciphertext = cryptoService.encrypt("data".encodeToByteArray(), "aad".encodeToByteArray())

            shouldThrow<IllegalArgumentException> {
                cryptoService.decrypt(ciphertext, byteArrayOf())
            }
        }

        "decrypt with wrong AAD throws DecryptionFailed" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val plaintext = "frisboo-crypto-wrong-aad".encodeToByteArray()
            val associatedData = "aad-correct".encodeToByteArray()
            val wrongAssociatedData = "aad-wrong".encodeToByteArray()
            val ciphertext = cryptoService.encrypt(plaintext, associatedData)

            shouldThrow<CryptoException.DecryptionFailed> {
                cryptoService.decrypt(ciphertext, wrongAssociatedData)
            }
        }

        "decrypt with tampered ciphertext throws DecryptionFailed" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val plaintext = "frisboo-crypto-tampered-ciphertext".encodeToByteArray()
            val associatedData = "aad".encodeToByteArray()
            val ciphertext = cryptoService.encrypt(plaintext, associatedData)
            val tamperedCiphertext =
                ciphertext.copyOf().also {
                    it[it.lastIndex] =
                        (it[it.lastIndex].toInt() xor 1).toByte()
                }

            shouldThrow<CryptoException.DecryptionFailed> {
                cryptoService.decrypt(tamperedCiphertext, associatedData)
            }
        }

        "decrypt with wrong key throws DecryptionFailed" {
            val keysetHandleA = KeysetHandleFactory.generateAes256Gcm()
            val keysetHandleB = KeysetHandleFactory.generateAes256Gcm()
            val cryptoServiceA = TinkCryptoService(keysetHandleA)
            val cryptoServiceB = TinkCryptoService(keysetHandleB)
            val plaintext = "frisboo-crypto-wrong-key".encodeToByteArray()
            val associatedData = "aad".encodeToByteArray()
            val ciphertext = cryptoServiceA.encrypt(plaintext, associatedData)

            shouldThrow<CryptoException.DecryptionFailed> {
                cryptoServiceB.decrypt(ciphertext, associatedData)
            }
        }

        "KeysetHandleFactory.generateAes256Gcm produces working keys" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val plaintext = "frisboo-crypto-working-key".encodeToByteArray()
            val associatedData = "aad".encodeToByteArray()

            val ciphertext = cryptoService.encrypt(plaintext, associatedData)
            val decrypted = cryptoService.decrypt(ciphertext, associatedData)

            keysetHandle shouldNotBe null
            decrypted shouldBe plaintext
        }

        "TinkCryptoInitializer.initialize is idempotent" {
            TinkCryptoInitializer.initialize()
            TinkCryptoInitializer.initialize()

            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val plaintext = "frisboo-crypto-initialize-idempotent".encodeToByteArray()
            val associatedData = "aad".encodeToByteArray()
            val decrypted = cryptoService.decrypt(cryptoService.encrypt(plaintext, associatedData), associatedData)

            decrypted shouldBe plaintext
        }

        "key rotation keeps old ciphertext decryptable after promoting new key" {
            val keysetHandleA = KeysetHandleFactory.generateAes256Gcm()
            val cryptoServiceBeforeRotation = TinkCryptoService(keysetHandleA)
            val plaintext = "frisboo-crypto-key-rotation".encodeToByteArray()
            val associatedData = "aad".encodeToByteArray()
            val ciphertextWithKeyA = cryptoServiceBeforeRotation.encrypt(plaintext, associatedData)

            val rotatedHandle =
                KeysetHandle
                    .newBuilder(keysetHandleA)
                    .addEntry(
                        KeysetHandle
                            .generateEntryFromParameters(PredefinedAeadParameters.AES256_GCM)
                            .withRandomId()
                            .makePrimary(),
                    ).build()

            val cryptoServiceAfterRotation = TinkCryptoService(rotatedHandle)
            val decrypted = cryptoServiceAfterRotation.decrypt(ciphertextWithKeyA, associatedData)

            rotatedHandle shouldNotBe null
            decrypted shouldBe plaintext
        }

        "construction with invalid keyset throws InitializationFailed" {
            val hybridHandle = KeysetHandleFactory.generateHybridHpke()

            shouldThrow<CryptoException.InitializationFailed> {
                TinkCryptoService(hybridHandle)
            }
        }

        "property-based: encrypt-decrypt roundtrip holds for arbitrary plaintext and AAD" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)

            checkAll(100, Arb.byteArray(Arb.int(1..4096), Arb.int().map { it.toByte() }), Arb.string(1..256)) { plaintext, aad ->
                val aadBytes = aad.encodeToByteArray()
                val ciphertext = cryptoService.encrypt(plaintext, aadBytes)
                val decrypted = cryptoService.decrypt(ciphertext, aadBytes)

                decrypted shouldBe plaintext
            }
        }

        "concurrent encrypt-decrypt operations are thread-safe" {
            val keysetHandle = KeysetHandleFactory.generateAes256Gcm()
            val cryptoService = TinkCryptoService(keysetHandle)
            val iterations = 100

            val results = (1..iterations).map { i ->
                async(Dispatchers.Default) {
                    val plaintext = "concurrent-test-$i".encodeToByteArray()
                    val aad = "aad-$i".encodeToByteArray()
                    val ciphertext = cryptoService.encrypt(plaintext, aad)
                    val decrypted = cryptoService.decrypt(ciphertext, aad)
                    decrypted shouldBe plaintext
                }
            }.awaitAll()

            results.size shouldBe iterations
        }
    })
