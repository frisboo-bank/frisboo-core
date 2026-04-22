package com.frisboo.corebanking.persistence.triggers

import com.frisboo.corebanking.persistence.exposed.abstracts.BaseTable
import com.frisboo.corebanking.persistence.postgres.testing.PostgreSQLTestFixture
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
private object TestTimestamped : BaseTable("test_timestamped") {
    val tableId: Column<Uuid> = uuid("table_id").autoGenerate()
    val name = varchar("name", 255)
    override val primaryKey = PrimaryKey(tableId)
}

@OptIn(ExperimentalUuidApi::class)
internal class SqlTriggerTimestampedTest : StringSpec() {

    lateinit var postgreSQLTestFixture: PostgreSQLTestFixture

    override suspend fun beforeSpec(spec: Spec) {
        if (System.getenv("RUN_INTEGRATION_TESTS") != "true") return

        postgreSQLTestFixture = PostgreSQLTestFixture(
            databaseName = "trigger_test",
        )

        postgreSQLTestFixture.start()
        postgreSQLTestFixture.migrate("classpath:db/migration/postgres")

        suspendTransaction {
            exec(
                """
                    CREATE TABLE test_timestamped (
                        table_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        name       VARCHAR(255) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
                    );
                """.trimIndent(),
            )
            exec(
                """
                CREATE TRIGGER test_timestamped_timestamps
                BEFORE INSERT OR UPDATE ON test_timestamped
                FOR EACH ROW
                EXECUTE FUNCTION public.fcb_handle_timestamps();
            """.trimIndent(),
            )
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        try {
            postgreSQLTestFixture.stop()
        } catch (_: Throwable) {
            // If integration wasn't started, stop may throw; ignore
        }
    }

    private val integrationEnabled = System.getenv("RUN_INTEGRATION_TESTS") == "true"

    init {
        if (!integrationEnabled) {
            "integration tests disabled; set RUN_INTEGRATION_TESTS=true to enable" {
                // no-op
            }
        } else {
        "fcb_handle_timestamps: INSERT sets created_at and updated_at" {
            val before = OffsetDateTime.now().minusSeconds(2)
            val insertName = "ts-insert-${Uuid.random()}"

            suspendTransaction {
                TestTimestamped.insert { it[TestTimestamped.name] = insertName }
            }

            val (createdAt, updatedAt) =
                suspendTransaction {
                    TestTimestamped.select(
                        TestTimestamped.createdAt,
                        TestTimestamped.updatedAt,
                    )
                        .where { TestTimestamped.name eq insertName }
                        .map {
                            it[TestTimestamped.createdAt] to it[TestTimestamped.updatedAt]
                        }.single()
                }

            createdAt shouldNotBe null
            updatedAt shouldNotBe null
            createdAt shouldBe updatedAt
            createdAt shouldBeGreaterThanOrEqualTo before
        }

        "fcb_handle_timestamps: UPDATE changes updated_at but preserves created_at" {
            val insertName = "ts-insert-${Uuid.random()}"
            val updateName = "ts-insert-${Uuid.random()}"

            suspendTransaction {
                TestTimestamped.insert { it[name] = insertName }
            }

            val (createdBefore, updatedBefore) =
                suspendTransaction {
                    TestTimestamped.select(
                        TestTimestamped.createdAt,
                        TestTimestamped.updatedAt,
                    ).where { TestTimestamped.name eq insertName }
                        .map {
                            it[TestTimestamped.createdAt] to it[TestTimestamped.updatedAt]
                        }.single()
                }

            suspendTransaction {
                TestTimestamped.update({ TestTimestamped.name eq insertName })
                { it[name] = updateName }
            }

            val (createdAfter, updatedAfter) =
                suspendTransaction {
                    TestTimestamped.select(
                        TestTimestamped.createdAt,
                        TestTimestamped.updatedAt,
                    ).where { TestTimestamped.name eq updateName }
                        .map {
                            it[TestTimestamped.createdAt] to it[TestTimestamped.updatedAt]
                        }.single()
                }

            createdAfter shouldBe createdBefore
            updatedAfter shouldBeGreaterThanOrEqualTo updatedBefore
        }

        "fcb_handle_timestamps: no-op UPDATE preserves both timestamps" {
            val insertName = "ts-insert-${Uuid.random()}"

            suspendTransaction {
                TestTimestamped.insert { it[name] = insertName }
            }

            val (createdBefore, updatedBefore) =
                suspendTransaction {
                    TestTimestamped.select(
                        TestTimestamped.createdAt,
                        TestTimestamped.updatedAt,
                    ).where { TestTimestamped.name eq insertName }
                        .map {
                            it[TestTimestamped.createdAt] to it[TestTimestamped.updatedAt]
                        }.single()
                }

            suspendTransaction {
                TestTimestamped.update({ TestTimestamped.name eq insertName })
                { it[name] = insertName }
            }

            val (createdAfter, updatedAfter) =
                suspendTransaction {
                    TestTimestamped.select(
                        TestTimestamped.createdAt,
                        TestTimestamped.updatedAt,
                    ).where { TestTimestamped.name eq insertName }
                        .map {
                            it[TestTimestamped.createdAt] to it[TestTimestamped.updatedAt]
                        }.single()
                }

            createdAfter shouldBe createdBefore
            updatedAfter shouldBe updatedBefore
        }
        }
    }
}
