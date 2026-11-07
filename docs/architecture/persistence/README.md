**English** | [Português](README.pt-BR.md)

# Persistence

How Muxe stores data. PostgreSQL is the source of truth; Redis is a transport and cache that can be emptied at any time without losing anything.

Code: `core/common/src/main/java/br/com/muxe/core/common/persistence`.

## Building blocks

| Class | Responsibility |
|---|---|
| `DatabaseConfig` | URL, credentials, pool size, connection timeout, whether to run migrations, and the slow statement threshold. |
| `Database` | A HikariCP pool over `PGSimpleDataSource`, with pool metrics. Batched inserts are rewritten by the driver into multi-row inserts (`reWriteBatchedInserts`). |
| `Transactions` | The port used by handlers: `inTransaction(operation, work)` and `readOnly(operation, work)`. |
| `JdbcTransactions` | The JDBC implementation. |
| `TransactionContext` | Gives statements the connection and the statement observer of the current transaction. |
| `Table`, `Column`, `SqlType` | The typed schema: a table, its columns and how each Java value is written and read. Package `persistence.sql`. |
| `Sql`, `Select`, `Write` | Typed statements built once from the schema: selects, inserts, upserts, updates, deletes and hand-written SQL. |
| `StatementMetrics` | Times every statement and reports the slow ones. |
| `MigrationRunner` | Runs the Flyway migrations of one module in its own schema. |
| `ExpiringStore` | Short-lived values in Redis (`RedisExpiringStore`), every one with a time to live. Keys get the `muxe:` prefix. |

## Transactions

```java
return transactions.inTransaction("account.record_login", () -> {
    Optional<Account> holder = accounts.findByUsername(command.username());
    accounts.save(account);
    return Result.success(account);
});
```

- The connection is bound with a Java 25 **`ScopedValue`** for the duration of the work. Repositories call `TransactionContext.connection()`; calling it outside a transaction fails with a clear message.
- A transaction started inside another one **joins** it, so a handler can compose repository calls freely.
- The work commits when it returns and rolls back when it throws. Checked exceptions become `PersistenceException`; runtime exceptions propagate unchanged.
- `readOnly` marks the connection read-only, so PostgreSQL can optimize it and accidental writes fail.
- **Never on the main thread.** On Spigot, `JdbcTransactions` uses a `MainThreadGuard` and throws `BlockingOnMainThreadException` if called from the server thread. Handlers run on the CQRS buses' virtual threads.
- Every transaction is timed in `muxe_db_transaction_seconds{operation, outcome}`. The operation name is a stable, low-cardinality string such as `auth.register`.

## Repositories

- The interface lives in the feature's `domain` package; the implementation in `infrastructure/postgres`.
- Repositories declare their tables and statements with the typed schema below, as `static final` constants. No ORM and no reflection: each statement generates exactly one SQL string when the class loads, always runs as a `PreparedStatement`, and the cost of each query stays visible (`statement.sql()`).
- Tables are always schema-qualified (`core.accounts`, `auth.credentials`).
- Unit tests use in-memory repositories with `DirectTransactions`; integration tests use the real repository against a PostgreSQL container.

## Typed schema and statements

Code: `core/common/src/main/java/br/com/muxe/core/common/persistence/sql`.

```java
private static final Table ACCOUNTS = Table.of("core", "accounts");
private static final Column<UUID> ID = ACCOUNTS.column("id", SqlType.UUID);
private static final Column<String> USERNAME = ACCOUNTS.column("username", SqlType.TEXT);
private static final Column<Instant> LAST_SEEN_AT = ACCOUNTS.column("last_seen_at", SqlType.TIMESTAMPTZ);

private static final Select<Account> FIND_BY_IDS = Sql.select(ID, USERNAME, LAST_SEEN_AT).from(ACCOUNTS)
    .where(ID.in())
    .map(row -> new Account(row.get(ID), row.get(USERNAME), row.get(LAST_SEEN_AT)))
    .named("account.find_by_ids");

private static final Write<Account> SAVE = Sql.<Account>insertInto(ACCOUNTS)
    .value(ID, Account::id)
    .value(USERNAME, Account::username)
    .value(LAST_SEEN_AT, Account::lastSeenAt)
    .onConflict(ID).doUpdate(USERNAME, LAST_SEEN_AT)
    .named("account.save");

FIND_BY_IDS.list(ID.in(ids));   // SELECT id, username, last_seen_at FROM core.accounts WHERE id = ANY(?)
SAVE.execute(account);          // one upsert
SAVE.batch(accounts);           // one round trip for every account
```

| Piece | What it does |
|---|---|
| `Table.of(schema, name)` | A table. `table.column(name, type)` declares a `Column<T>`. Identifiers are checked (`[a-z_][a-z0-9_]*`), so no SQL can be injected through them. |
| `SqlType<T>` | How a Java value is bound and read: `UUID`, `TEXT`, `BOOLEAN`, `SMALLINT`, `INTEGER`, `BIGINT`, `DOUBLE`, `REAL`, `TIMESTAMPTZ` (`Instant`), `DATE` (`LocalDate`), `TEXT_ARRAY` (`List<String>`), `BYTEA`. `type.map(read, write)` adapts a domain value (`SqlType.TEXT.map(Visibility::parse, Visibility::name)`), `enumByName(type)` stores an enum by name, `optional()` turns SQL `NULL` into `Optional.empty()`. A `null` value is written as SQL `NULL` of the right type. |
| Conditions | `col.eq()`, `col.eqIgnoreCase()`, `col.in()` (`= ANY(?)`, one array parameter for any number of values), `col.isNull()`, `col.eq(other)` for joins, `a.and(b)`, and `Condition.sql("coins >= ?", COINS)` for anything else. |
| Arguments | Values are bound by column, not by position: `select.first(GROUP.is(group), ID.is(id))`, and `col.in(values)` for `ANY`. A missing or unknown argument fails with the statement name. |
| `Select<R>` | `first(...)` (the first row), `list(...)`, `exists(...)`. Rows are mapped with `row.get(column)`, read by index. A select with a join qualifies every column. Also `orderBy(col.desc())`, `limit(Sql.parameter("limit", SqlType.INTEGER))` and `selectDistinct`. |
| `Write<T>` | Built by `insertInto` (with `onConflict(...)` and then `doNothing()`, `doUpdate(columns)`, `doUpdate(column, "expression")`, `where("condition")`, plus `as(alias)`), `update` (`set`, `where(column, getter)`) or `deleteFrom` (a condition is required). A value comes from the entity (`value(col, Entity::getter)`) or from an argument (`value(col)`, then `execute(col.is(x))`). `execute(...)` returns the affected rows; `batch(values)` sends every value in one `executeBatch` and does nothing for an empty list. |
| `Sql.query(sql)`, `Sql.statement(sql)` | Hand-written SQL for what the builders do not cover (rankings with joins, `RETURNING`, advisory locks), still with typed parameters (`parameters(...)`, in placeholder order) and typed result columns (`columns(...)`). The number of `?` is checked when the statement is declared. |

Every statement has a stable, low-cardinality name (`named("account.save")`), used by the metrics and by `PersistenceException`. Statements run in the current transaction (`TransactionContext`); outside one they fail like any repository call.

### Batches and lookups by many ids

- Load what a screen or a handler needs in **one** query: `col.in(values)` instead of a loop of `first` calls. `AccountRepository.findByIds`, `MissionRepository.find` and `PermissionRepository.saveUsers` work this way.
- Write many rows with `batch(values)`. The driver rewrites a batched insert into multi-row inserts, so a batch of 1000 rows is one round trip. Because of that, **a batch of upserts must not contain the same key twice** (PostgreSQL refuses to update the same row twice in one statement): keep the last value per key before calling `batch`.
- `ApplyPermissionChangesHandler` saves every user of a command with `saveUsers` and writes the action log with `logAll`, so applying 20 user changes takes 10 round trips instead of 105.

### Statement metrics

| Metric | Type | What it shows |
|---|---|---|
| `muxe_db_statement_seconds{statement,outcome}` | histogram | Every statement, by name and `success` or `error`. Its count is the number of executions. |
| `muxe_db_slow_statements_total{statement}` | counter | Statements that took at least `database.slow-statement-threshold` (default `250ms`). Each one is also logged as a warning with its name and duration. |

## Migrations

- Each module owns a PostgreSQL **schema** with the same name and its migrations in `src/main/resources/db/migration/<module>/`, named `V<n>__<description>.sql`.
- `MigrationRunner.migrate(module, classLoader)` creates the schema if needed and keeps the history in `<module>.schema_history`.
- The core migrates the `core` schema on startup when `database.run-migrations` is true. A plugin migrates its own schema on enable: `core.migrations().migrate("auth", getClass().getClassLoader())`.
- Flyway uses a PostgreSQL lock, so several servers starting together never apply the same migration twice.
- A migration that was released is never edited. Changes are new migrations.
- In production, migrations can be applied by a single designated server (or a deploy step) with a privileged user, and disabled everywhere else, as described in [Security](../security/README.md).

| Schema | Owner | Tables |
|---|---|---|
| `core` | `core:common` | `accounts` (V1), `accounts.premium` (V2), nullable `accounts.username` for released names (V3), `npc_placements`, `hologram_placements`, `register_placements` (placed per server group) and `shop_promotion` (one row for the network) (V4), `player_visibility` (each player's visibility choice, copied from the old `lobby.player_settings`) (V5), `social_head_placements` (social media heads placed per server group) (V6), `parkour_placements` (parkour start, checkpoints and end placed per server group) and `parkour_records` (each player's best parkour time per server group) (V7) |
| `auth` | `servers:auth` | `credentials` |
| `lobby` | `servers:lobby` | None. `player_settings` (V1) was dropped by V2 after its rows moved to `core.player_visibility`. |

## Kinds of data

| Kind | Examples | Strategy |
|---|---|---|
| Critical | Credentials, coins, purchases, rewards | Written in a PostgreSQL transaction **before** the player is told it happened. |
| Non-critical | Preferences, toggles | Applied in memory and written in the background. A crash may lose the last seconds. |
| Match state | Teams, kills, timers | Memory only, on the server running the match. |
| Ephemeral network state | Server status, idempotency claims, login sessions, premium lookups | Redis or packets, always with an expiration. |

## Rules

- **Never block the main thread.** Database and Redis calls run outside it; results that touch the game are scheduled back.
- **Critical data is confirmed only after commit.**
- **Balances change atomically in SQL** (`UPDATE … SET coins = coins - 100 WHERE coins >= 100`), never by writing a value read earlier.
- **Uniqueness is enforced by the database**, not only by code. Example: `accounts_username_lower_key` makes two accounts whose names differ only in case impossible, even under a race.
- **Durable data never lives only in Redis.** Every Redis key that is not a cache has an expiration.
- **Redis keys share a prefix:** `muxe:<area>:<id>`, such as `muxe:idempotency:auth.authenticated:…`.
- **No queries inside loops.** Load what is needed in one query (`col.in(values)`) and write many rows with `batch`.
- **Every frequent query is backed by an index.**
- **Pools stay small.** Each PostgreSQL connection is a process; 4 connections per server is the default.

## Planned: session ownership

To support saving player data across servers safely, the plan is that each online player's data has exactly one owner server at a time, recorded in PostgreSQL with a generation number that only increases, and that every write carries the owner's generation so that a frozen or stale server can never overwrite newer data. Transfers will flush and release the session before the destination takes it. This is not implemented yet; it will be documented here when it is.
