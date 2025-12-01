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
package com.frisboo.corebanking.core.serializers

import com.fasterxml.jackson.module.kotlin.jsonMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.email
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.javaInstant
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.take
import io.kotest.property.arbs.firstName
import io.kotest.property.arbs.lastName
import io.kotest.property.arbs.payments.transactions
import io.kotest.property.forAll
import kotlinx.datetime.toJavaLocalDateTime
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

private data class Transaction(
    val amount: BigDecimal,
    val cardNumber: String,
    val createdAt: LocalDateTime,
)

private data class Person(
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val dateOfBirth: LocalDate?,
    val email: String? = null,
    val isActive: Boolean,
    val numberOfDependents: Int = 0,
    val budget: BigDecimal = BigDecimal.ZERO,
    val transactions: Map<UUID, Transaction> = emptyMap(),
    val createdAt: Instant,
)

private val personArb: Arb<Person> =
    arbitrary {
        val firstName = Arb.firstName().bind()
        val middleName = Arb.firstName().orNull(nullProbability = .8).bind()
        val lastName = Arb.lastName().bind()
        val dateOfBirth = Arb.localDate().bind()
        val email = Arb.email().orNull(.1).bind()
        val isActive = Arb.boolean().bind()
        val numberOfDependents = Arb.int(0..10).bind()
        val budget = Arb.bigDecimal().bind()
        val transactions: Map<UUID, Transaction> =
            Arb
                .transactions()
                .take((0..20).random())
                .map { transaction ->
                    Pair(
                        UUID.randomUUID(),
                        Transaction(
                            amount = transaction.amount.toBigDecimal(),
                            cardNumber = transaction.cardNumber,
                            createdAt = transaction.date.toJavaLocalDateTime(),
                        ),
                    )
                }.toMap()
        val createdAt = Arb.javaInstant().bind()

        Person(
            firstName = firstName.name,
            middleName = middleName?.name,
            lastName = lastName.name,
            dateOfBirth,
            email,
            isActive,
            numberOfDependents,
            budget,
            transactions = transactions,
            createdAt,
        )
    }

class JacksonJsonSerializerImplTest :
    StringSpec(
        {

            val objectMapper = jsonMapper { findAndAddModules() }
            val serializer = JacksonJsonSerializerImpl(objectMapper)

            "serialize to bytes and unserialize objects" {
                forAll(personArb) { person ->
                    val personInBytes: ByteArray = serializer.serializeToBytes(person)
                    val deserializedPerson: Person = serializer.deserialize(personInBytes, Person::class.java)
                    person == deserializedPerson
                }
            }

            "serialize to string matches the bytes version" {
                forAll(personArb) { person ->
                    val personInString = serializer.serializeToString(person)
                    val personInBytes = serializer.serializeToBytes(person)
                    personInString == personInBytes.toString(Charsets.UTF_8)
                }
            }
        },
    )
