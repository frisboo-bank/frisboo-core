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
package com.frisboo.corebanking.persistence.exposed.extensions

import arrow.core.Either
import com.frisboo.corebanking.persistence.errors.PersistenceError
import com.frisboo.corebanking.persistence.exposed.abstracts.BaseTable
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test-only table for verifying [optimisticUpdate] behaviour.
 *
 * Created in-container via Flyway migration ([TEST_MIGRATION_SQL]).
 */
private object TestAccountsTable : BaseTable("test_accounts"), WithOptimisticLocking {
    val accountId: Column<Long> = long("account_id").autoIncrement()
    val name: Column<String> = varchar("name", 255)
    val balance: Column<Long> = long("balance").default(0)

    override val primaryKey = PrimaryKey(accountId)
    override val optimisticLockingVersion: Column<Long> = version
}

private const val TEST_MIGRATION_SQL =
    """
    CREATE TABLE test_accounts (
        account_id BIGSERIAL PRIMARY KEY,
        name       VARCHAR(255) NOT NULL,
        balance    BIGINT NOT NULL DEFAULT 0,
        version    BIGINT NOT NULL DEFAULT 1,
        created_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
    );

    CREATE TRIGGER test_accounts_timestamps
        BEFORE INSERT OR UPDATE ON test_accounts
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_handle_timestamps();
    """

internal class OptimisticUpdateTest :
    StringSpec({
        lateinit var container: PostgreSQLContainer<*>

        beforeSpec {
            container =
                PostgreSQLContainer<Nothing>("postgres:16.4-alpine").apply {
                    withDatabaseName("persistence-test")
                    withUsername("postgres")
                    withPassword("postgres")
                    start()
                }

            Flyway
                .configure()
                .dataSource(container.jdbcUrl, container.username, container.password)
                .locations("classpath:db/migration/postgres")
                .load()
                .migrate()

            Database.connect(
                url = container.jdbcUrl,
                user = container.username,
                password = container.password,
            )

            transaction {
                exec(TEST_MIGRATION_SQL)
            }
        }

        afterSpec {
            container.stop()
        }

        suspend fun insertAccount(name: String, balance: Long = 0): Long =
            suspendTransaction {
                TestAccountsTable.insert {
                    it[TestAccountsTable.name] = name
                    it[TestAccountsTable.balance] = balance
                } get TestAccountsTable.accountId
            }

        suspend fun readVersion(accountId: Long): Long =
            suspendTransaction {
                TestAccountsTable
                    .selectAll()
                    .where { TestAccountsTable.accountId eq accountId }
                    .single()[TestAccountsTable.version]
            }

        suspend fun readBalance(accountId: Long): Long =
            suspendTransaction {
                TestAccountsTable
                    .selectAll()
                    .where { TestAccountsTable.accountId eq accountId }
                    .single()[TestAccountsTable.balance]
            }

        "happy-path: update succeeds and increments version" {
            val id = insertAccount("alice", balance = 100)
            readVersion(id) shouldBe 1L

            val result: Either<PersistenceError, Int> =
                suspendTransaction {
                    TestAccountsTable.optimisticUpdate(
                        where = { TestAccountsTable.accountId eq id },
                        version = 1L,
                    ) {
                        it[TestAccountsTable.balance] = 200L
                    }
                }

            result.shouldBeRight(1)
            readBalance(id) shouldBe 200L
            readVersion(id) shouldBe 2L
        }

        "version mismatch returns OptimisticLockFailed" {
            val id = insertAccount("bob", balance = 50)
            readVersion(id) shouldBe 1L

            val result =
                suspendTransaction {
                    TestAccountsTable.optimisticUpdate(
                        where = { TestAccountsTable.accountId eq id },
                        version = 999L,
                    ) {
                        it[TestAccountsTable.balance] = 9999L
                    }
                }

            result.shouldBeLeft(
                PersistenceError.OptimisticLockFailed(
                    table = TestAccountsTable.tableName,
                    expectedVersion = 999L,
                ),
            )
            readBalance(id) shouldBe 50L
            readVersion(id) shouldBe 1L
        }

        "sequential updates with correct versions succeed" {
            val id = insertAccount("carol", balance = 0)

            for (v in 1L..5L) {
                val result =
                    suspendTransaction {
                        TestAccountsTable.optimisticUpdate(
                            where = { TestAccountsTable.accountId eq id },
                            version = v,
                        ) {
                            it[TestAccountsTable.balance] = v * 10
                        }
                    }
                result.shouldBeRight(1)
            }

            readVersion(id) shouldBe 6L
            readBalance(id) shouldBe 50L
        }

        "stale version after successful update returns OptimisticLockFailed" {
            val id = insertAccount("dave", balance = 100)

            val firstUpdate =
                suspendTransaction {
                    TestAccountsTable.optimisticUpdate(
                        where = { TestAccountsTable.accountId eq id },
                        version = 1L,
                    ) {
                        it[TestAccountsTable.balance] = 200L
                    }
                }
            firstUpdate.shouldBeRight(1)

            val staleUpdate =
                suspendTransaction {
                    TestAccountsTable.optimisticUpdate(
                        where = { TestAccountsTable.accountId eq id },
                        version = 1L,
                    ) {
                        it[TestAccountsTable.balance] = 300L
                    }
                }
            staleUpdate.shouldBeLeft(
                PersistenceError.OptimisticLockFailed(
                    table = TestAccountsTable.tableName,
                    expectedVersion = 1L,
                ),
            )

            readBalance(id) shouldBe 200L
            readVersion(id) shouldBe 2L
        }

        "non-unique where clause returns NonUniqueUpdate and rolls back" {
            val id1 = insertAccount("multi-a", balance = 100)
            val id2 = insertAccount("multi-b", balance = 200)

            // Both rows share version 1 — a non-unique WHERE targeting both should fail
            val result =
                suspendTransaction {
                    TestAccountsTable.optimisticUpdate(
                        where = {
                            (TestAccountsTable.accountId eq id1) or
                                (TestAccountsTable.accountId eq id2)
                        },
                        version = 1L,
                    ) {
                        it[TestAccountsTable.balance] = 999L
                    }
                }

            result.shouldBeLeft(
                PersistenceError.NonUniqueUpdate(
                    table = TestAccountsTable.tableName,
                    affectedRows = 2,
                ),
            )
            // Verify rollback: both rows must retain original balances
            readBalance(id1) shouldBe 100L
            readBalance(id2) shouldBe 200L
            readVersion(id1) shouldBe 1L
            readVersion(id2) shouldBe 1L
        }

        "concurrent optimistic updates — exactly one winner per round (convention rule #46)" {
            val id = insertAccount("concurrent-test", balance = 0)
            val iterations = 100
            val successCount = AtomicInteger(0)
            val failureCount = AtomicInteger(0)

            val results =
                (1..iterations).map { i ->
                    async(Dispatchers.Default) {
                        val result =
                            suspendTransaction {
                                TestAccountsTable.optimisticUpdate(
                                    where = { TestAccountsTable.accountId eq id },
                                    version = 1L,
                                ) {
                                    it[TestAccountsTable.balance] = i.toLong()
                                }
                            }
                        when (result) {
                            is Either.Right -> successCount.incrementAndGet()
                            is Either.Left -> failureCount.incrementAndGet()
                        }
                        result
                    }
                }.awaitAll()

            successCount.get() shouldBe 1
            failureCount.get() shouldBe iterations - 1
            readVersion(id) shouldBe 2L
        }
    })
