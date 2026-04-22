package com.frisboo.corebanking.persistence.triggers

import com.frisboo.corebanking.persistence.exposed.abstracts.BaseTable
import com.frisboo.corebanking.persistence.postgres.testing.PostgreSQLTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.time.OffsetDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
private object TestTtl : BaseTable("test_ttl") {
    val tableId: Column<Uuid> = uuid("table_id").autoGenerate()
    val ttlInSecond = integer("ttl_in_second")
    val expiredAt: Column<OffsetDateTime> = timestampWithTimeZone("expired_at").databaseGenerated()
    override val primaryKey = PrimaryKey(tableId)
}

@OptIn(ExperimentalUuidApi::class)
internal class SqlTriggerExpiredTest : StringSpec() {

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
                CREATE TABLE test_ttl (
                    table_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    ttl_in_second INTEGER NOT NULL,
                    expired_at    TIMESTAMPTZ
                );
                """.trimIndent(),
            )
            exec(
                """
                CREATE TRIGGER test_ttl_expired_at
                BEFORE INSERT ON test_ttl
                FOR EACH ROW
                EXECUTE FUNCTION public.fcb_update_expired_at();
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
        "fcb_update_expired_at: INSERT with valid TTL sets expired_at" {
            checkAll(Arb.int(1..86_400)) { ttlSeconds ->
                val before = OffsetDateTime.now()

                suspendTransaction {
                    TestTtl.insert { it[ttlInSecond] = ttlSeconds }
                }

                val expiredAt =
                    suspendTransaction {
                        TestTtl.select(TestTtl.expiredAt)
                            .where { TestTtl.ttlInSecond eq ttlSeconds }
                            .map { it[TestTtl.expiredAt] }
                            .single()
                    }

                val expectedMin = before.plusSeconds(ttlSeconds.toLong()).minusSeconds(5)
                val expectedMax = before.plusSeconds(ttlSeconds.toLong()).plusSeconds(5)
                expiredAt shouldBeGreaterThanOrEqualTo expectedMin
                expiredAt shouldBeLessThanOrEqualTo expectedMax
            }
        }

        "fcb_update_expired_at: INSERT with zero TTL raises exception" {
            shouldThrow<ExposedSQLException> {
                suspendTransaction {
                    TestTtl.insert { it[ttlInSecond] = 0 }
                }
            }
        }

        "fcb_update_expired_at: INSERT with negative TTL raises exception" {
            shouldThrow<ExposedSQLException> {
                suspendTransaction {
                    TestTtl.insert { it[ttlInSecond] = -1 }
                }
            }
        }
        }
    }
}
