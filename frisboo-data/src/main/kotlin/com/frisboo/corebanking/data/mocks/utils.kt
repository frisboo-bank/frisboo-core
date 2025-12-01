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
package com.frisboo.corebanking.data.mocks

import com.frisboo.corebanking.core.domain.valueobjects.AccountType
import com.frisboo.corebanking.core.domain.valueobjects.Currency
import io.kotest.core.Tuple3
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.email
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.frequency
import io.kotest.property.arbitrary.kotlinInstant
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid
import io.kotest.property.arbs.firstName
import io.kotest.property.arbs.lastName
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

public data class Customer(
    val customerId: UUID,
    val firstName: String,
    val lastName: String,
    val email: String,
)

public data class Account(
    val accountId: UUID,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: BigDecimal,
    val currency: Currency,
)

public val versionArb: Arb<Long> = Arb.long(1, 100)

public fun timestampArb(baseTimestamp: CharSequence = "2020-01-01T00:00:00Z"): Arb<Tuple3<Instant, Instant, Instant?>> =
    arbitrary {
        val createdAt: Instant = Arb.kotlinInstant(maxValue = Clock.System.now()).bind()
        val updatedAt: Instant = Arb.kotlinInstant(minValue = createdAt).bind()
        val processedAt: Instant? = Arb.frequency(
            7 to Arb.constant(null),
            3 to Arb.kotlinInstant(minValue = createdAt, maxValue = updatedAt),
        ).bind()
        Tuple3(createdAt, updatedAt, processedAt)
    }

public fun customerArb(): Arb<Customer> = arbitrary {
    val customerId = Arb.uuid().bind()
    val firstName = Arb.firstName().bind()
    val lastName = Arb.lastName().bind()
    val email = Arb.email().bind()

    Customer(
        customerId,
        firstName = firstName.toString(),
        lastName = lastName.toString(),
        email,
    )
}

public fun accountArb(): Arb<Account> = arbitrary {
    val accountId = Arb.uuid().bind()
    val accountNumber = accountNumberArb().bind()
    val accountType = accountTypeArb().bind()
    val balance = moneyAmountArb().bind()
    val currency = currencyArb().bind()

    Account(
        accountId,
        accountNumber,
        accountType,
        balance,
        currency,
    )
}

public fun accountTypeArb(): Arb<AccountType> = Arb.element(AccountType.entries)

public fun accountNumberArb(): Arb<String> = arbitrary { "ACC-${Arb.uuid().bind()}" }

public fun currencyArb(): Arb<Currency> = Arb.element(Currency.entries)

public fun moneyAmountArb(
    min: Double = 0.0,
    max: Double = 1_0000_0000_0000_0000.0,
    scale: Int = 4,
    rounding: RoundingMode = RoundingMode.HALF_EVEN,
): Arb<BigDecimal> = Arb.double(min, max).map {
    BigDecimal(it).setScale(scale, rounding)
}

public fun accountOverdraftLimitsArb(): Arb<Pair<BigDecimal, BigDecimal>> {
    val oldLimit = moneyAmountArb(min = 0.0, max = 1000.0)
    val newLimit = oldLimit.flatMap {
        val changePercentage = (10..50).random() / 100.0
        val amountChange = it * BigDecimal(changePercentage)
        val newLimit = if (listOf(true, false).random()) it + amountChange
        else (it - amountChange).max(BigDecimal.ZERO)
        Arb.constant(newLimit)
    }
    return Arb.bind(oldLimit, newLimit, ::Pair)
}
