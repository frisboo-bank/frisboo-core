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
package com.frisboo.corebanking.persistence.triggers

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

private const val TRIGGER_TEST_TABLES_SQL =
    """
    CREATE TABLE test_timestamped (
        id         BIGSERIAL PRIMARY KEY,
        name       VARCHAR(255) NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
    );

    CREATE TRIGGER test_timestamped_timestamps
        BEFORE INSERT OR UPDATE ON test_timestamped
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_handle_timestamps();

    CREATE TABLE test_ttl (
        id            BIGSERIAL PRIMARY KEY,
        ttl_in_second INTEGER NOT NULL,
        expired_at    TIMESTAMPTZ,
        created_at    TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
        updated_at    TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
    );

    CREATE TRIGGER test_ttl_expired_at
        BEFORE INSERT ON test_ttl
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_update_expired_at();

    CREATE TRIGGER test_ttl_timestamps
        BEFORE INSERT OR UPDATE ON test_ttl
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_handle_timestamps();

    CREATE TABLE test_verification (
        id                  BIGSERIAL PRIMARY KEY,
        verified_at         TIMESTAMPTZ,
        verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        created_at          TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
        updated_at          TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
    );

    CREATE TRIGGER test_verification_status
        BEFORE INSERT OR UPDATE ON test_verification
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_update_verification_status();

    CREATE TRIGGER test_verification_timestamps
        BEFORE INSERT OR UPDATE ON test_verification
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_handle_timestamps();
    """

internal class SqlTriggerTest :
    StringSpec({
        lateinit var container: PostgreSQLContainer<*>

        beforeSpec {
            container =
                PostgreSQLContainer<Nothing>("postgres:16.4-alpine").apply {
                    withDatabaseName("trigger-test")
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
                exec(TRIGGER_TEST_TABLES_SQL)
            }
        }

        afterSpec {
            container.stop()
        }

        "fcb_handle_timestamps: INSERT sets created_at and updated_at" {
            val before = OffsetDateTime.now().minusSeconds(2)

            transaction {
                exec("INSERT INTO test_timestamped (name) VALUES ('ts-insert')")
            }

            val (createdAt, updatedAt) =
                transaction {
                    exec("SELECT created_at, updated_at FROM test_timestamped WHERE name = 'ts-insert'") { rs ->
                        rs.next()
                        rs.getObject("created_at", OffsetDateTime::class.java) to
                            rs.getObject("updated_at", OffsetDateTime::class.java)
                    }!!
                }

            createdAt shouldNotBe null
            updatedAt shouldNotBe null
            createdAt shouldBe updatedAt
            createdAt!!.shouldBeGreaterThanOrEqualTo(before)
        }

        "fcb_handle_timestamps: UPDATE changes updated_at but preserves created_at" {
            transaction {
                exec("INSERT INTO test_timestamped (name) VALUES ('ts-update')")
            }

            val (createdBefore, updatedBefore) =
                transaction {
                    exec("SELECT created_at, updated_at FROM test_timestamped WHERE name = 'ts-update'") { rs ->
                        rs.next()
                        rs.getObject("created_at", OffsetDateTime::class.java) to
                            rs.getObject("updated_at", OffsetDateTime::class.java)
                    }!!
                }

            // pg_sleep ensures measurable time difference
            transaction {
                exec("SELECT pg_sleep(0.05)")
                exec("UPDATE test_timestamped SET name = 'ts-update-changed' WHERE name = 'ts-update'")
            }

            val (createdAfter, updatedAfter) =
                transaction {
                    exec(
                        "SELECT created_at, updated_at FROM test_timestamped WHERE name = 'ts-update-changed'",
                    ) { rs ->
                        rs.next()
                        rs.getObject("created_at", OffsetDateTime::class.java) to
                            rs.getObject("updated_at", OffsetDateTime::class.java)
                    }!!
                }

            createdAfter shouldBe createdBefore
            updatedAfter!!.shouldBeGreaterThanOrEqualTo(updatedBefore!!)
        }

        "fcb_handle_timestamps: no-op UPDATE preserves both timestamps" {
            transaction {
                exec("INSERT INTO test_timestamped (name) VALUES ('ts-noop')")
            }

            val (createdBefore, updatedBefore) =
                transaction {
                    exec("SELECT created_at, updated_at FROM test_timestamped WHERE name = 'ts-noop'") { rs ->
                        rs.next()
                        rs.getObject("created_at", OffsetDateTime::class.java) to
                            rs.getObject("updated_at", OffsetDateTime::class.java)
                    }!!
                }

            transaction {
                exec("SELECT pg_sleep(0.05)")
                exec("UPDATE test_timestamped SET name = 'ts-noop' WHERE name = 'ts-noop'")
            }

            val (createdAfter, updatedAfter) =
                transaction {
                    exec("SELECT created_at, updated_at FROM test_timestamped WHERE name = 'ts-noop'") { rs ->
                        rs.next()
                        rs.getObject("created_at", OffsetDateTime::class.java) to
                            rs.getObject("updated_at", OffsetDateTime::class.java)
                    }!!
                }

            createdAfter shouldBe createdBefore
            updatedAfter shouldBe updatedBefore
        }

        "fcb_update_expired_at: INSERT with valid TTL sets expired_at" {
            val ttlSeconds = 3600
            val before = OffsetDateTime.now()

            transaction {
                exec("INSERT INTO test_ttl (ttl_in_second) VALUES ($ttlSeconds)")
            }

            val expiredAt =
                transaction {
                    exec("SELECT expired_at FROM test_ttl WHERE ttl_in_second = $ttlSeconds LIMIT 1") { rs ->
                        rs.next()
                        rs.getObject("expired_at", OffsetDateTime::class.java)
                    }!!
                }

            val expectedMin = before.plusSeconds(ttlSeconds.toLong()).minusSeconds(5)
            val expectedMax = before.plusSeconds(ttlSeconds.toLong()).plusSeconds(5)
            expiredAt!!.shouldBeGreaterThanOrEqualTo(expectedMin)
            expiredAt.shouldBeLessThanOrEqualTo(expectedMax)
        }

        "fcb_update_expired_at: INSERT with zero TTL raises exception" {
            shouldThrow<ExposedSQLException> {
                transaction {
                    exec("INSERT INTO test_ttl (ttl_in_second) VALUES (0)")
                }
            }
        }

        "fcb_update_expired_at: INSERT with negative TTL raises exception" {
            shouldThrow<ExposedSQLException> {
                transaction {
                    exec("INSERT INTO test_ttl (ttl_in_second) VALUES (-1)")
                }
            }
        }

        "fcb_update_expired_at: INSERT with TTL exceeding 30 days raises exception" {
            val overMax = 2592001
            shouldThrow<ExposedSQLException> {
                transaction {
                    exec("INSERT INTO test_ttl (ttl_in_second) VALUES ($overMax)")
                }
            }
        }

        "fcb_update_expired_at: INSERT with max TTL (30 days) succeeds" {
            val maxTtl = 2592000
            val before = OffsetDateTime.now()

            transaction {
                exec("INSERT INTO test_ttl (ttl_in_second) VALUES ($maxTtl)")
            }

            val expiredAt =
                transaction {
                    exec("SELECT expired_at FROM test_ttl ORDER BY id DESC LIMIT 1") { rs ->
                        rs.next()
                        rs.getObject("expired_at", OffsetDateTime::class.java)
                    }!!
                }

            val expectedApprox = before.plusSeconds(maxTtl.toLong())
            val diffSeconds = ChronoUnit.SECONDS.between(expectedApprox, expiredAt)
            diffSeconds.shouldBeGreaterThanOrEqualTo(-5)
            diffSeconds.shouldBeLessThanOrEqualTo(5)
        }

        "fcb_update_verification_status: INSERT without verified_at sets PENDING" {
            transaction {
                exec("INSERT INTO test_verification DEFAULT VALUES")
            }

            val status =
                transaction {
                    exec("SELECT verification_status FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
                        rs.next()
                        rs.getString("verification_status")
                    }!!
                }

            status shouldBe "PENDING"
        }

        "fcb_update_verification_status: INSERT with verified_at sets VERIFIED" {
            transaction {
                exec("INSERT INTO test_verification (verified_at) VALUES (NOW())")
            }

            val status =
                transaction {
                    exec("SELECT verification_status FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
                        rs.next()
                        rs.getString("verification_status")
                    }!!
                }

            status shouldBe "VERIFIED"
        }

        "fcb_update_verification_status: UPDATE clearing verified_at sets REVOKED" {
            transaction {
                exec("INSERT INTO test_verification (verified_at) VALUES (NOW())")
            }

            val id =
                transaction {
                    exec("SELECT id FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
                        rs.next()
                        rs.getLong("id")
                    }!!
                }

            transaction {
                exec("UPDATE test_verification SET verified_at = NULL WHERE id = $id")
            }

            val status =
                transaction {
                    exec("SELECT verification_status FROM test_verification WHERE id = $id") { rs ->
                        rs.next()
                        rs.getString("verification_status")
                    }!!
                }

            status shouldBe "REVOKED"
        }

        "fcb_update_verification_status: INSERT overrides application-set status" {
            transaction {
                exec("INSERT INTO test_verification (verification_status) VALUES ('VERIFIED')")
            }

            val status =
                transaction {
                    exec("SELECT verification_status FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
                        rs.next()
                        rs.getString("verification_status")
                    }!!
                }

            status shouldBe "PENDING"
        }

        "fcb_validate_of_age: returns true for DOB 18+ years ago" {
            val result =
                transaction {
                    exec("SELECT public.fcb_validate_of_age((CURRENT_DATE - INTERVAL '18 years')::DATE)") { rs ->
                        rs.next()
                        rs.getBoolean(1)
                    }!!
                }

            result shouldBe true
        }

        "fcb_validate_of_age: returns false for DOB less than 18 years ago" {
            val result =
                transaction {
                    exec("SELECT public.fcb_validate_of_age((CURRENT_DATE - INTERVAL '17 years')::DATE)") { rs ->
                        rs.next()
                        rs.getBoolean(1)
                    }!!
                }

            result shouldBe false
        }

        "fcb_validate_of_age: returns NULL for NULL input (STRICT)" {
            val result =
                transaction {
                    exec("SELECT public.fcb_validate_of_age(NULL::DATE)") { rs ->
                        rs.next()
                        rs.getObject(1)
                    }
                }

            result shouldBe null
        }
    })
