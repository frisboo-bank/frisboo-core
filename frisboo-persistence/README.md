# frisboo-persistence

Shared persistence primitives for Frisboo services — base tables, optimistic locking, SQL triggers, and Flyway migrations for PostgreSQL.

## What's Included?

| Component                                    | Purpose                                                                                                           |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `BaseTable`                                  | Abstract Exposed table with `version`, `created_at`, `updated_at` columns                                         |
| `WithOptimisticLocking` / `optimisticUpdate` | Application-managed optimistic locking returning `Either<PersistenceError, Int>` with exactly-one-row enforcement |
| `PersistenceError`                           | Sealed error hierarchy for persistence failures                                                                   |
| SQL triggers                                 | `fcb_handle_timestamps()`, `fcb_update_expired_at()`, `fcb_update_verification_status()`, `fcb_validate_of_age()` |
| Auto-configuration                           | `PersistenceProperties` with conditional `Postgres` / `MongoDb` markers                                           |

## Getting Started

### 1. Define a Table

Extend `BaseTable` and implement `WithOptimisticLocking` to get audit columns and version-checked updates:

```kotlin
object AccountsTable : BaseTable("accounts"), WithOptimisticLocking {
    val accountId = long("account_id").autoIncrement()
    val name = varchar("name", 255)
    val balance = long("balance").default(0)

    override val primaryKey = PrimaryKey(accountId)
    override val optimisticLockingVersion = version
}
```

This gives you:

- `version` — starts at `1`, incremented by `optimisticUpdate` on each successful write
- `created_at` / `updated_at` — managed by the `fcb_handle_timestamps()` database trigger (never set these from application code)

### 2. Update with optimistic locking

```kotlin
import arrow.core.Either
import com.frisboo.corebanking.persistence.errors.PersistenceError

val result: Either<PersistenceError, Int> =
    transaction {
        AccountsTable.optimisticUpdate(
            where = { AccountsTable.accountId eq 42L },
            version = currentVersion,
        ) {
            it[AccountsTable.balance] = newBalance
        }
    }
// Right(1)  — 1 row updated, version incremented to currentVersion + 1
// Left(OptimisticLockFailed(table="accounts", expectedVersion=3))  — stale version, no rows changed
// Left(NonUniqueUpdate(table="accounts", affectedRows=2))  — where matched multiple rows, rolled back via savepoint
```

The `where` parameter is required and must uniquely identify a single row (typically a primary-key match). The version check is ANDed automatically — you never set the version column manually. If the `where` clause matches more than one row, the update is rolled back and `NonUniqueUpdate` is returned.

### 3. Handle errors

All persistence failures are subclasses of `PersistenceError` (sealed interface):

```kotlin
when (val error = result.leftOrNull()) {
    is PersistenceError.OptimisticLockFailed -> {
        // error.table         => "accounts"
        // error.expectedVersion => 3
        // Retry with fresh version, or return conflict to caller
    }
    is PersistenceError.NonUniqueUpdate -> {
        // error.table         => "accounts"
        // error.affectedRows  => 2
        // Bug: WHERE clause is not unique — fix the predicate
    }
    null -> {
        // result is Right(1) — success
    }
}
```

### 4. Wire up Flyway migrations

Your service's migration folder automatically picks up the shared migrations from this module's classpath (`db/migration/postgres/`). These create the trigger functions used by `BaseTable`.

In your table migration, attach the timestamp trigger:

```sql
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    balance    BIGINT NOT NULL DEFAULT 0,
    version    BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT statement_timestamp()
);

CREATE TRIGGER accounts_timestamps
    BEFORE INSERT OR UPDATE ON accounts
    FOR EACH ROW
    EXECUTE FUNCTION public.fcb_handle_timestamps();
```

### 5. Use TTL expiration (optional)

For tables with time-to-live semantics, add a `ttl_in_second` column and attach `fcb_update_expired_at()`:

```sql
ALTER TABLE my_table ADD COLUMN ttl_in_second INTEGER NOT NULL;
ALTER TABLE my_table ADD COLUMN expired_at TIMESTAMPTZ;

CREATE TRIGGER my_table_expired_at
    BEFORE INSERT ON my_table
    FOR EACH ROW
    EXECUTE FUNCTION public.fcb_update_expired_at();
```

The trigger validates that `ttl_in_second` is positive and bounded (max 30 days = 2,592,000 seconds), then computes `expired_at = NOW() + ttl_in_second`.

```kotlin
// Insert with a 1-hour TTL
transaction {
    MyTable.insert {
        it[ttlInSecond] = 3600
        // expired_at is set automatically by the trigger — do not set it manually
    }
}
// expired_at => 2025-04-04T11:30:00+08:00 (1 hour from insert time)
```

Invalid values raise a database exception:

- `ttl_in_second = 0` → `"ttl_in_second must be positive, got 0"`
- `ttl_in_second = 3000000` → `"ttl_in_second exceeds maximum of 2592000 seconds, got 3000000"`

### 6. Use verification status derivation (optional)

For tables tracking verification state, attach `fcb_update_verification_status()`:

```sql
ALTER TABLE my_table ADD COLUMN verified_at TIMESTAMPTZ;
ALTER TABLE my_table ADD COLUMN verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

CREATE TRIGGER my_table_verification_status
    BEFORE INSERT OR UPDATE ON my_table
    FOR EACH ROW
    EXECUTE FUNCTION public.fcb_update_verification_status();
```

The trigger derives `verification_status` from `verified_at` — the application value is always overwritten:

| Condition                                                                | Status     |
| ------------------------------------------------------------------------ | ---------- |
| `verified_at IS NOT NULL`                                                | `VERIFIED` |
| UPDATE where `OLD.verified_at IS NOT NULL` and `NEW.verified_at IS NULL` | `REVOKED`  |
| Otherwise                                                                | `PENDING`  |

```kotlin
// Mark as verified
transaction {
    MyTable.update({ MyTable.id eq recordId }) {
        it[verifiedAt] = OffsetDateTime.now()
        // verification_status is automatically set to 'VERIFIED' by the trigger
    }
}

// Revoke verification
transaction {
    MyTable.update({ MyTable.id eq recordId }) {
        it[verifiedAt] = null
        // verification_status is automatically set to 'REVOKED' by the trigger
    }
}
```

## Using with Spring Boot

### Configuration

```yaml
frisboo:
    corebanking:
        persistence:
            postgres:
                enabled: true # activates the Postgres marker configuration
            mongodb:
                enabled: true # activates the MongoDB marker configuration
```

### Conditional bean wiring

The auto-configuration registers marker `@Configuration` classes that your service can gate on:

```kotlin
@Configuration
@ConditionalOnBean(PersistenceAutoConfiguration.Postgres::class)
class MyPostgresConfig {

    @Bean
    fun myRepository(database: Database): MyRepository =
        MyRepositoryImpl(database)
}
```

If `frisboo.corebanking.persistence.postgres.enabled` is `false` (the default), the `Postgres` marker is not created, so `MyPostgresConfig` is skipped entirely.

## Available SQL functions

| Function                           | Trigger event                   | Purpose                                                                                                               |
| ---------------------------------- | ------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| `fcb_handle_timestamps()`          | `BEFORE INSERT OR UPDATE`       | Sets `created_at`/`updated_at` on insert; updates `updated_at` on meaningful changes; preserves both on no-op updates |
| `fcb_update_expired_at()`          | `BEFORE INSERT`                 | Computes `expired_at` from `ttl_in_second`; validates positive and ≤ 30 days                                          |
| `fcb_update_verification_status()` | `BEFORE INSERT OR UPDATE`       | Derives `verification_status` from `verified_at` (PENDING / VERIFIED / REVOKED)                                       |
| `fcb_validate_of_age(dob DATE)`    | Pure function (`STABLE STRICT`) | Returns `true` if `dob` is ≥ 18 years before today; `NULL` on `NULL` input                                            |

## Extensions provided

| Extension  | Purpose                                                |
| ---------- | ------------------------------------------------------ |
| `citext`   | Case-insensitive text type for email, username columns |
| `pgcrypto` | Cryptographic functions (`gen_random_uuid()`, etc.)    |
| `pg_trgm`  | Trigram-based similarity search and indexing           |
