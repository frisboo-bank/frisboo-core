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

import com.frisboo.corebanking.persistence.exposed.abstracts.BaseTable
import com.frisboo.corebanking.persistence.postgres.testing.PostgreSQLTestFixture
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
private object TestVerification : BaseTable("test_verification") {
    val tableId: Column<Uuid> = uuid("table_id").autoGenerate()
    val verifiedAt = timestampWithTimeZone("verified_at").nullable()
    val verificationStatus = varchar("verification_status", 20).default("PENDING")
    override val primaryKey = PrimaryKey(tableId)
}

private const val TRIGGER_TEST_TABLES_SQL =
    """
    CREATE TRIGGER test_ttl_expired_at
        BEFORE INSERT ON test_ttl
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_update_expired_at();

    CREATE TRIGGER test_ttl_timestamps
        BEFORE INSERT OR UPDATE ON test_ttl
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_handle_timestamps();

    CREATE TRIGGER test_verification_status
        BEFORE INSERT OR UPDATE ON test_verification
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_update_verification_status();

    CREATE TRIGGER test_verification_timestamps
        BEFORE INSERT OR UPDATE ON test_verification
        FOR EACH ROW
        EXECUTE FUNCTION public.fcb_handle_timestamps();
    """

internal class SqlTriggerTest : StringSpec() {

    lateinit var testFixture: PostgreSQLTestFixture

    override suspend fun beforeSpec(spec: Spec) {
        testFixture = PostgreSQLTestFixture(
            databaseName = "trigger_test",
        )

        testFixture.start()
        testFixture.migrate("classpath:db/migration/postgres")

        suspendTransaction {
            exec(TRIGGER_TEST_TABLES_SQL)
        }
    }

    override suspend fun afterSpec(spec: Spec) {
        testFixture.stop()
    }

    init {
    }
}

//
//        "fcb_update_verification_status: INSERT without verified_at sets PENDING" {
//            suspendTransaction {
//                exec("INSERT INTO test_verification DEFAULT VALUES")
//            }
//
//            val status =
//                suspendTransaction {
//                    exec("SELECT verification_status FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
//                        rs.next()
//                        rs.getString("verification_status")
//                    }!!
//                }
//
//            status shouldBe "PENDING"
//        }
//
//        "fcb_update_verification_status: INSERT with verified_at sets VERIFIED" {
//            suspendTransaction {
//                exec("INSERT INTO test_verification (verified_at) VALUES (NOW())")
//            }
//
//            val status =
//                suspendTransaction {
//                    exec("SELECT verification_status FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
//                        rs.next()
//                        rs.getString("verification_status")
//                    }!!
//                }
//
//            status shouldBe "VERIFIED"
//        }
//
//        "fcb_update_verification_status: UPDATE clearing verified_at sets REVOKED" {
//            suspendTransaction {
//                exec("INSERT INTO test_verification (verified_at) VALUES (NOW())")
//            }
//
//            val id =
//                suspendTransaction {
//                    exec("SELECT id FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
//                        rs.next()
//                        rs.getLong("id")
//                    }!!
//                }
//
//            suspendTransaction {
//                exec("UPDATE test_verification SET verified_at = NULL WHERE id = $id")
//            }
//
//            val status =
//                suspendTransaction {
//                    exec("SELECT verification_status FROM test_verification WHERE id = $id") { rs ->
//                        rs.next()
//                        rs.getString("verification_status")
//                    }!!
//                }
//
//            status shouldBe "REVOKED"
//        }
//
//        "fcb_update_verification_status: INSERT overrides application-set status" {
//            suspendTransaction {
//                exec("INSERT INTO test_verification (verification_status) VALUES ('VERIFIED')")
//            }
//
//            val status =
//                suspendTransaction {
//                    exec("SELECT verification_status FROM test_verification ORDER BY id DESC LIMIT 1") { rs ->
//                        rs.next()
//                        rs.getString("verification_status")
//                    }!!
//                }
//
//            status shouldBe "PENDING"
//        }
//
//        "fcb_validate_of_age: returns true for DOB 18+ years ago" {
//            val result =
//                suspendTransaction {
//                    exec("SELECT public.fcb_validate_of_age((CURRENT_DATE - INTERVAL '18 years')::DATE)") { rs ->
//                        rs.next()
//                        rs.getBoolean(1)
//                    }!!
//                }
//
//            result shouldBe true
//        }
//
//        "fcb_validate_of_age: returns false for DOB less than 18 years ago" {
//            val result =
//                suspendTransaction {
//                    exec("SELECT public.fcb_validate_of_age((CURRENT_DATE - INTERVAL '17 years')::DATE)") { rs ->
//                        rs.next()
//                        rs.getBoolean(1)
//                    }!!
//                }
//
//            result shouldBe false
//        }
//
//        "fcb_validate_of_age: returns NULL for NULL input (STRICT)" {
//            val result =
//                suspendTransaction {
//                    exec("SELECT public.fcb_validate_of_age(NULL::DATE)") { rs ->
//                        rs.next()
//                        rs.getObject(1)
//                    }
//                }
//
//            result shouldBe null
//        }

