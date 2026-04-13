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
package com.frisboo.corebanking.core.serializers.adaptors.jackson

import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.forAll
import java.math.BigDecimal

private data class Wallet(
    val balance: BigDecimal,
)

class BigDecimalNumericSerializerTest :
    StringSpec(
        {

            val objectMapper =
                jsonMapper {
                    findAndAddModules()
                }
            val serializer = JacksonJsonSerializerImpl(objectMapper)

            "serialize and deserialize BigDecimal correctly" {
                Arb.bigDecimal().forAll { bigDecimal ->
                    val wallet = Wallet(balance = bigDecimal)
                    val serialized = serializer.serializeToString(wallet)
                    val deserialized: Wallet = serializer.deserialize(serialized.toByteArray(Charsets.UTF_8), Wallet::class.java)
                    deserialized.balance == bigDecimal
                }
            }

            "serialize and deserialize known BigDecimal values correctly" {
                val testValues =
                    mapOf<BigDecimal, String>(
                        BigDecimal("725345854747326287606413621318.311864440287151714280387858224") to
                            "725345854747326287606413621318.311864440287151714280387858224",
                        BigDecimal("336052472523017262165484244513.836582112201211216526831524328") to
                            "336052472523017262165484244513.836582112201211216526831524328",
                        BigDecimal("211054843014778386028147282517.011200287614476453868782405400") to
                            "211054843014778386028147282517.011200287614476453868782405400",
                        BigDecimal("364751025728628060231208776573.207325218263752602211531367642") to
                            "364751025728628060231208776573.207325218263752602211531367642",
                        BigDecimal("508257556021513833656664177125.824502734715222686411316853148") to
                            "508257556021513833656664177125.824502734715222686411316853148",
                        BigDecimal("127134584027580606401102614002.366672301517071543257300444000") to
                            "127134584027580606401102614002.366672301517071543257300444000",
                    )

                testValues.forEach { (bigDecimal, expectedString) ->
                    val wallet = Wallet(balance = bigDecimal)
                    val serialized = serializer.serializeToString(wallet)
                    val deserialized: Wallet = serializer.deserialize(serialized.toByteArray(Charsets.UTF_8), Wallet::class.java)

                    deserialized.balance shouldBeEqual bigDecimal
                    serialized shouldContain "{\"balance\":$expectedString}"
                }
            }
        },
    )
