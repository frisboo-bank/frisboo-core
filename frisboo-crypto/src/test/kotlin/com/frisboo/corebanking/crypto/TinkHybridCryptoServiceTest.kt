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
import com.frisboo.corebanking.crypto.adapters.tink.TinkHybridCryptoService
import com.frisboo.corebanking.crypto.errors.CryptoException
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

internal class TinkHybridCryptoServiceTest :
    StringSpec({
        TinkCryptoInitializer.initialize()

        "hybrid encrypt and decrypt round-trip with context info" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)
            val plaintext = "frisboo-hybrid-with-context".encodeToByteArray()
            val contextInfo = "payment:merchant-123:txn-456".encodeToByteArray()

            val ciphertext = service.encrypt(plaintext, contextInfo)
            val decrypted = service.decrypt(ciphertext, contextInfo)

            ciphertext shouldNotBe null
            decrypted shouldBe plaintext
        }

        "encrypt rejects empty plaintext" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)

            shouldThrow<IllegalArgumentException> {
                service.encrypt(byteArrayOf(), "ctx".encodeToByteArray())
            }
        }

        "encrypt rejects empty contextInfo" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)

            shouldThrow<IllegalArgumentException> {
                service.encrypt("plaintext".encodeToByteArray(), byteArrayOf())
            }
        }

        "decrypt rejects empty ciphertext" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)

            shouldThrow<IllegalArgumentException> {
                service.decrypt(byteArrayOf(), "ctx".encodeToByteArray())
            }
        }

        "decrypt rejects empty contextInfo" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)
            val ciphertext = service.encrypt("data".encodeToByteArray(), "ctx".encodeToByteArray())

            shouldThrow<IllegalArgumentException> {
                service.decrypt(ciphertext, byteArrayOf())
            }
        }

        "hybrid decrypt with wrong context info throws HybridDecryptionFailed" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)
            val plaintext = "frisboo-hybrid-wrong-context".encodeToByteArray()
            val contextInfo = "context-correct".encodeToByteArray()
            val wrongContext = "context-wrong".encodeToByteArray()
            val ciphertext = service.encrypt(plaintext, contextInfo)

            shouldThrow<CryptoException.HybridDecryptionFailed> {
                service.decrypt(ciphertext, wrongContext)
            }
        }

        "hybrid decrypt with wrong key throws HybridDecryptionFailed" {
            val privateHandleA = KeysetHandleFactory.generateHybridHpke()
            val privateHandleB = KeysetHandleFactory.generateHybridHpke()
            val serviceA = TinkHybridCryptoService(privateHandleA)
            val serviceB = TinkHybridCryptoService(privateHandleB)
            val plaintext = "frisboo-hybrid-wrong-key".encodeToByteArray()
            val contextInfo = "ctx".encodeToByteArray()
            val ciphertext = serviceA.encrypt(plaintext, contextInfo)

            shouldThrow<CryptoException.HybridDecryptionFailed> {
                serviceB.decrypt(ciphertext, contextInfo)
            }
        }

        "hybrid decrypt with tampered ciphertext throws HybridDecryptionFailed" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)
            val plaintext = "frisboo-hybrid-tampered".encodeToByteArray()
            val contextInfo = "ctx".encodeToByteArray()
            val ciphertext = service.encrypt(plaintext, contextInfo)
            val tampered =
                ciphertext.copyOf().also {
                    it[it.lastIndex] = (it[it.lastIndex].toInt() xor 1).toByte()
                }

            shouldThrow<CryptoException.HybridDecryptionFailed> {
                service.decrypt(tampered, contextInfo)
            }
        }

        "construction with invalid keyset throws InitializationFailed" {
            val aeadHandle = KeysetHandleFactory.generateAes256Gcm()

            shouldThrow<CryptoException.InitializationFailed> {
                TinkHybridCryptoService(aeadHandle)
            }
        }

        "construction with AEAD keyset in TinkCryptoService does not throw" {
            val aeadHandle = KeysetHandleFactory.generateAes256Gcm()
            val service = TinkCryptoService(aeadHandle)

            service shouldNotBe null
        }

        "property-based: hybrid encrypt-decrypt roundtrip holds for arbitrary plaintext and context" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)

            checkAll(50, Arb.byteArray(Arb.int(1..4096), Arb.int().map { it.toByte() }), Arb.string(1..256)) { plaintext, ctx ->
                val ctxBytes = ctx.encodeToByteArray()
                val ciphertext = service.encrypt(plaintext, ctxBytes)
                val decrypted = service.decrypt(ciphertext, ctxBytes)

                decrypted shouldBe plaintext
            }
        }

        "concurrent hybrid encrypt-decrypt operations are thread-safe" {
            val privateHandle = KeysetHandleFactory.generateHybridHpke()
            val service = TinkHybridCryptoService(privateHandle)
            val iterations = 50

            val results = (1..iterations).map { i ->
                async(Dispatchers.Default) {
                    val plaintext = "concurrent-hybrid-$i".encodeToByteArray()
                    val ctx = "ctx-$i".encodeToByteArray()
                    val ciphertext = service.encrypt(plaintext, ctx)
                    val decrypted = service.decrypt(ciphertext, ctx)
                    decrypted shouldBe plaintext
                }
            }.awaitAll()

            results.size shouldBe iterations
        }
    })
