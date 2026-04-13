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
package com.frisboo.corebanking.statemanager.serialization

import com.frisboo.corebanking.crypto.contracts.CryptoService
import com.frisboo.corebanking.crypto.errors.CryptoException
import com.frisboo.corebanking.persistence.core.serializers.PersistenceEncryptingSerializerImpl
import com.frisboo.corebanking.persistence.core.serializers.PersistenceStringSerializerImpl
import com.frisboo.corebanking.statemanager.contracts.StateManagerSerializer
import com.frisboo.corebanking.persistence.core.models.StateManagerScope
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.util.Base64

internal class StateManagerEncryptingSerializerImplTest :
    StringSpec(
        {
            val cryptoService = FakeAeadCryptoService()
            val scope = StateManagerScope(name = "encrypt-test", team = "test-team")
            val otherScope = StateManagerScope(name = "encrypt-other", team = "test-team")

            "serialize then deserialize round-trip produces original value" {
                val delegate = PersistenceStringSerializerImpl()
                val serializer = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)
                val value = "sensitive-value"

                val ciphertext = serializer.serialize(value)
                val result = serializer.deserialize(ciphertext)

                result shouldBe value
            }

            "deserialize with wrong scope different AAD throws DecryptionFailed" {
                val delegate = PersistenceStringSerializerImpl()
                val serializer = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)
                val serializerWithWrongScope = PersistenceEncryptingSerializerImpl(delegate, cryptoService, otherScope)

                val ciphertext = serializer.serialize("secret")

                shouldThrow<CryptoException.DecryptionFailed> {
                    serializerWithWrongScope.deserialize(ciphertext)
                }
            }

            "deserialize with tampered ciphertext throws CryptoException.DecryptionFailed" {
                val delegate = PersistenceStringSerializerImpl()
                val serializer = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)

                val ciphertext = serializer.serialize("secret")
                val tampered = ciphertext.copyOf().also { bytes -> bytes[0] = (bytes[0].toInt() xor 1).toByte() }

                shouldThrow<CryptoException.DecryptionFailed> {
                    serializer.deserialize(tampered)
                }
            }

            "different scopes produce different ciphertexts for same plaintext" {
                val delegate = PersistenceStringSerializerImpl()
                val serializerA = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)
                val serializerB = PersistenceEncryptingSerializerImpl(delegate, cryptoService, otherScope)
                val value = "same-plaintext"

                val ciphertextA = serializerA.serialize(value)
                val ciphertextB = serializerB.serialize(value)

                val encodedA = Base64.getEncoder().encodeToString(ciphertextA)
                val encodedB = Base64.getEncoder().encodeToString(ciphertextB)
                encodedA shouldNotBe encodedB
            }

            "delegate serializer is called correctly value to bytes to encrypt decrypt to bytes to value" {
                val delegate = RecordingStringSerializer()
                val serializer = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)
                val value = "wire-value"

                val ciphertext = serializer.serialize(value)
                val result = serializer.deserialize(ciphertext)

                delegate.serializeInputs shouldBe listOf(value)
                delegate.deserializeInputs.size shouldBe 1
                delegate.deserializeInputs.single().decodeToString() shouldBe value
                result shouldBe value
            }
            "property-based: encrypt-decrypt roundtrip holds for arbitrary string values" {
                val delegate = PersistenceStringSerializerImpl()
                val serializer = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)

                checkAll(100, Arb.string(1..4096)) { value ->
                    val ciphertext = serializer.serialize(value)
                    val result = serializer.deserialize(ciphertext)
                    result shouldBe value
                }
            }

            "property-based: different values produce different ciphertexts" {
                val delegate = PersistenceStringSerializerImpl()
                val serializer = PersistenceEncryptingSerializerImpl(delegate, cryptoService, scope)

                checkAll(50, Arb.string(1..256), Arb.string(1..256)) { a, b ->
                    if (a != b) {
                        val ciphertextA = Base64.getEncoder().encodeToString(serializer.serialize(a))
                        val ciphertextB = Base64.getEncoder().encodeToString(serializer.serialize(b))
                        ciphertextA shouldNotBe ciphertextB
                    }
                }
            }
        },
    )

private class RecordingStringSerializer : StateManagerSerializer<String> {
    val serializeInputs = mutableListOf<String>()
    val deserializeInputs = mutableListOf<ByteArray>()

    override fun serialize(value: String): ByteArray {
        serializeInputs.add(value)
        return value.toByteArray(Charsets.UTF_8)
    }

    override fun deserialize(value: ByteArray): String {
        deserializeInputs.add(value)
        return value.toString(Charsets.UTF_8)
    }
}

private class FakeAeadCryptoService : CryptoService {
    override fun encrypt(
        plaintext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        require(plaintext.isNotEmpty())
        require(associatedData.isNotEmpty())

        val aadHash = associatedData.contentHashCode().toString().toByteArray(Charsets.UTF_8)
        return aadHash + "|".toByteArray(Charsets.UTF_8) + plaintext
    }

    override fun decrypt(
        ciphertext: ByteArray,
        associatedData: ByteArray,
    ): ByteArray {
        require(ciphertext.isNotEmpty())
        require(associatedData.isNotEmpty())

        val sep = ciphertext.indexOf('|'.code.toByte())
        if (sep <= 0) {
            throw CryptoException.DecryptionFailed("Malformed ciphertext")
        }

        val aadHash = ciphertext.copyOfRange(0, sep).toString(Charsets.UTF_8)
        val expected = associatedData.contentHashCode().toString()
        if (aadHash != expected) {
            throw CryptoException.DecryptionFailed("AAD mismatch")
        }

        return ciphertext.copyOfRange(sep + 1, ciphertext.size)
    }
}
