package com.frisboo.corebanking.persistence.exposed.extensions

import arrow.core.Either
import arrow.core.raise.either
import com.frisboo.corebanking.persistence.core.constants.POSTGRESQL_LATEST_IMAGE
import com.frisboo.corebanking.persistence.exposed.abstracts.BaseTable
import com.frisboo.corebanking.persistence.exposed.errors.ExposedError
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.PropTestConfig
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
private object TestTable : BaseTable("test_table"), WithOptimisticLocking {
    val tableId: Column<Uuid> = uuid("table_id").autoGenerate()
    val name: Column<String> = varchar("name", 255)
    val balance: Column<Long> = long("balance").default(0)

    override val primaryKey = PrimaryKey(tableId)
    override val optimisticLockingVersion: Column<Long> = version
}

@OptIn(ExperimentalUuidApi::class, ExperimentalKotest::class)
internal class WithOptimisticLockingTest : StringSpec() {

    companion object {
        private const val TEST_ITERATIONS = 20
    }

    private val postgresContainer = PostgreSQLContainer(POSTGRESQL_LATEST_IMAGE).apply {
        withReuse(true)
        withDatabaseName("persistence_test")
        withUsername("postgres")
        withPassword("postgres")
        withCommand("postgres", "-c", "max_connections=200")
    }

    private lateinit var dataSource: HikariDataSource
    private var integrationStarted: Boolean = false

    override suspend fun beforeSpec(spec: Spec) {
        if (System.getenv("RUN_INTEGRATION_TESTS") != "true") return
        postgresContainer.start()
        integrationStarted = true
        dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = postgresContainer.jdbcUrl
                username = postgresContainer.username
                password = postgresContainer.password
                maximumPoolSize = 150
            },
        )
        Database.connect(dataSource)
        Flyway.configure()
            .dataSource(
                postgresContainer.jdbcUrl,
                postgresContainer.username,
                postgresContainer.password,
            )
            .locations("classpath:db/migration/postgres")
            .load()
            .migrate()
        transaction {
            exec(
                """
                CREATE TABLE test_table (
                    table_id   Uuid PRIMARY KEY DEFAULT gen_random_Uuid(),
                    name       VARCHAR(255) NOT NULL,
                    balance    BIGINT NOT NULL DEFAULT 0,
                    version    BIGINT NOT NULL DEFAULT 1,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
                );

                CREATE TRIGGER test_table_timestamps
                BEFORE INSERT OR UPDATE ON test_table
                FOR EACH ROW
                EXECUTE FUNCTION public.fcb_handle_timestamps();
                """.trimIndent(),
            )
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        if (!integrationStarted) return

        transaction {
            exec("TRUNCATE TABLE test_table RESTART IDENTITY CASCADE")
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        if (integrationStarted) {
            dataSource.close()
            postgresContainer.stop()
        }
    }

    private suspend fun insertRow(name: String, balance: Long = 0): Either<ExposedError, Uuid> = either {
        suspendTransaction {
            TestTable.insert {
                it[TestTable.name] = name
                it[TestTable.balance] = balance
            } get TestTable.tableId
        }
    }

    private suspend fun readVersion(tableId: Uuid): Either<ExposedError, Long> = either {
        suspendTransaction {
            TestTable.select(TestTable.optimisticLockingVersion)
                .where { TestTable.tableId eq tableId }
                .single()[TestTable.version]
        }
    }

    private suspend fun readBalance(tableId: Uuid): Either<ExposedError, Long> = either {
        suspendTransaction {
            TestTable.select(TestTable.balance)
                .where { TestTable.tableId eq tableId }
                .single()[TestTable.balance]
        }
    }

    private val integrationEnabled = System.getenv("RUN_INTEGRATION_TESTS") == "true"

    init {
        if (!integrationEnabled) {
            "integration tests disabled; set RUN_INTEGRATION_TESTS=true to enable" {
                // no-op
            }
        } else {
        "happy-path: update succeeds and increments version" {
            checkAll(
                PropTestConfig(iterations = TEST_ITERATIONS),
                Arb.string(1..100),
                Arb.long(0L..10000L),
            ) { name, balance ->
                val id = insertRow(name, balance).shouldBeRight()
                readVersion(id).shouldBeRight(1L)

                val newBalance = balance + 100

                val result = suspendTransaction {
                    TestTable.optimisticUpdate(
                        where = { TestTable.tableId eq id },
                        version = 1L,
                    ) {
                        it[TestTable.balance] = newBalance
                    }
                }

                result.shouldBeRight(1)
                readBalance(id).shouldBeRight(newBalance)
                readVersion(id).shouldBeRight(2L)
            }
        }

        "version mismatch returns OptimisticLockFailed" {
            checkAll(
                PropTestConfig(iterations = TEST_ITERATIONS),
                Arb.string(1..100),
                Arb.long(0L..1000L),
                Arb.long(2L..100L),
            ) { name, balance, wrongVersion ->
                val id = insertRow(name, balance).shouldBeRight()
                readVersion(id).shouldBeRight(1L)

                val result = suspendTransaction {
                    TestTable.optimisticUpdate(
                        where = { TestTable.tableId eq id },
                        version = wrongVersion,
                    ) {
                        it[TestTable.balance] = 9999L
                    }
                }

                result.shouldBeLeft(ExposedError.OptimisticLockFailed(TestTable.tableName, wrongVersion))
                readBalance(id).shouldBeRight(balance)
                readVersion(id).shouldBeRight(1L)
            }
        }

        "sequential updates with correct versions succeed" {
            checkAll(
                PropTestConfig(iterations = TEST_ITERATIONS),
                Arb.string(1..100),
                Arb.long(0L..1000L),
                Arb.int(2..10),
            ) { name, startBalance, steps ->
                val id = insertRow(name, startBalance).shouldBeRight()
                readVersion(id).shouldBeRight(1L)

                var expectedVersion = 1L
                var expectedBalance = startBalance

                for (step in 1..steps) {
                    val newBalance = expectedBalance + step * 10
                    val result = suspendTransaction {
                        TestTable.optimisticUpdate(
                            where = { TestTable.tableId eq id },
                            version = expectedVersion,
                        ) {
                            it[TestTable.balance] = newBalance
                        }
                    }
                    result.shouldBeRight(1)
                    expectedVersion++
                    expectedBalance = newBalance
                }

                readVersion(id).shouldBeRight(expectedVersion)
                readBalance(id).shouldBeRight(expectedBalance)
            }
        }

        "stale version after successful update returns OptimisticLockFailed" {
            checkAll(
                PropTestConfig(iterations = TEST_ITERATIONS),
                Arb.string(1..100),
                Arb.long(0L..1000L),
            ) { name, balance ->
                val id = insertRow(name, balance).shouldBeRight()
                readVersion(id).shouldBeRight(1L)

                val newBalance = balance + 200

                val firstUpdate = suspendTransaction {
                    TestTable.optimisticUpdate(
                        where = { TestTable.tableId eq id },
                        version = 1L,
                    ) {
                        it[TestTable.balance] = newBalance
                    }
                }

                firstUpdate.shouldBeRight(1)
                readVersion(id).shouldBeRight(2L)
                readBalance(id).shouldBeRight(newBalance)

                val staleUpdate = suspendTransaction {
                    TestTable.optimisticUpdate(
                        where = { TestTable.tableId eq id },
                        version = 1L,
                    ) {
                        it[TestTable.balance] = newBalance + 100
                    }
                }

                staleUpdate.shouldBeLeft(ExposedError.OptimisticLockFailed(TestTable.tableName, 1L))
                readBalance(id).shouldBeRight(newBalance)
                readVersion(id).shouldBeRight(2L)
            }
        }

        "non-unique where clause returns NonUniqueUpdate and rolls back" {
            checkAll(
                PropTestConfig(iterations = TEST_ITERATIONS),
                Arb.string(1..50),
                Arb.string(1..50),
                Arb.long(0L..1000L),
                Arb.long(0L..1000L),
            ) { name1, name2, balance1, balance2 ->
                val id1 = insertRow(name1, balance1).shouldBeRight()
                val id2 = insertRow(name2, balance2).shouldBeRight()

                readVersion(id1).shouldBeRight(1L)
                readVersion(id2).shouldBeRight(1L)

                val result = suspendTransaction {
                    TestTable.optimisticUpdate(
                        where = { (TestTable.tableId eq id1) or (TestTable.tableId eq id2) },
                        version = 1L,
                    ) {
                        it[TestTable.balance] = 9999L
                    }
                }

                result.shouldBeLeft(ExposedError.NonUniqueUpdate(TestTable.tableName, 2))
                readBalance(id1).shouldBeRight(balance1)
                readVersion(id1).shouldBeRight(1L)
                readBalance(id2).shouldBeRight(balance2)
                readVersion(id2).shouldBeRight(1L)
            }
        }

        "non-existent row returns RowNotFound" {
            checkAll(PropTestConfig(iterations = 10), Arb.long(1L..100L)) { version ->
                val nonExistentId = Uuid.random()

                val result = suspendTransaction {
                    TestTable.optimisticUpdate(
                        where = { TestTable.tableId eq nonExistentId },
                        version = version,
                    ) {
                        it[TestTable.balance] = 999L
                    }
                }
                result.shouldBeLeft(ExposedError.RowNotFound(TestTable.tableName))
            }
        }

        "concurrent optimistic updates — exactly one winner per round" {
            checkAll(
                Arb.int(10..50),
                Arb.long(0L..5000L),
            ) { concurrentAttempts, startBalance ->
                val id = insertRow("concurrent-prop", startBalance).shouldBeRight()
                readVersion(id).shouldBeRight(1L)

                val successCount = AtomicInteger(0)
                val failureCount = AtomicInteger(0)
                val seenVersions = ConcurrentHashMap.newKeySet<Long>()

                coroutineScope {
                    val jobs = (1..concurrentAttempts).map { i ->
                        async(Dispatchers.IO) {
                            val result = suspendTransaction {
                                TestTable.optimisticUpdate(
                                    where = { TestTable.tableId eq id },
                                    version = 1L,
                                ) {
                                    it[TestTable.balance] = startBalance + i.toLong()
                                }
                            }
                            when (result) {
                                is Either.Right -> {
                                    successCount.incrementAndGet()
                                    readVersion(id).onRight { v -> seenVersions.add(v) }
                                }

                                is Either.Left -> failureCount.incrementAndGet()
                            }
                            result
                        }
                    }
                    jobs.awaitAll()
                }

                successCount.get() shouldBe 1
                failureCount.get() shouldBe concurrentAttempts - 1
                readVersion(id).shouldBeRight(2L)
                readBalance(id).shouldNotBe(startBalance)
                seenVersions.size shouldBe 1
                seenVersions.first() shouldBe 2L
            }
        }
        }
    }
}
