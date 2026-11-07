package br.com.muxe.core.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.Tags;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.sql.Argument;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.SelectBuilder;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.observe.StatementMetrics;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class SqlStatementsIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final Instant NOW = Instant.parse("2026-11-07T12:00:00.123Z");

    private static final Table SAMPLES = Table.of("sqltest", "samples");
    private static final Column<UUID> ID = SAMPLES.column("id", SqlType.UUID);
    private static final Column<String> NAME = SAMPLES.column("name", SqlType.TEXT);
    private static final Column<Boolean> FLAG = SAMPLES.column("flag", SqlType.BOOLEAN);
    private static final Column<Integer> SMALL = SAMPLES.column("small", SqlType.SMALLINT);
    private static final Column<Integer> NUMBER = SAMPLES.column("number", SqlType.INTEGER);
    private static final Column<Long> BIG = SAMPLES.column("big", SqlType.BIGINT);
    private static final Column<Double> RATIO = SAMPLES.column("ratio", SqlType.DOUBLE);
    private static final Column<Float> ANGLE = SAMPLES.column("angle", SqlType.REAL);
    private static final Column<Instant> AT = SAMPLES.column("at", SqlType.TIMESTAMPTZ);
    private static final Column<LocalDate> DAY = SAMPLES.column("day", SqlType.DATE);
    private static final Column<List<String>> TAGS = SAMPLES.column("tags", SqlType.TEXT_ARRAY);
    private static final Column<byte[]> DATA = SAMPLES.column("data", SqlType.BYTEA);
    private static final Column<Optional<Instant>> BANNED_AT = SAMPLES.column("banned_at", SqlType.TIMESTAMPTZ.optional());
    private static final Column<Kind> KIND = SAMPLES.column("kind", SqlType.enumByName(Kind.class));

    private static final Table COUNTERS = Table.of("sqltest", "counters");
    private static final Column<String> COUNTER_KEY = COUNTERS.column("key", SqlType.TEXT);
    private static final Column<Long> COUNTER_VALUE = COUNTERS.column("value", SqlType.BIGINT);
    private static final Column<String> COUNTER_NOTE = COUNTERS.column("note", SqlType.TEXT);

    private static final Column<Long> TOTAL = Sql.parameter("total", SqlType.BIGINT);

    private static final Write<Sample> INSERT = Sql.<Sample>insertInto(SAMPLES)
        .value(ID, Sample::id)
        .value(NAME, Sample::name)
        .value(FLAG, Sample::flag)
        .value(SMALL, Sample::small)
        .value(NUMBER, Sample::number)
        .value(BIG, Sample::big)
        .value(RATIO, Sample::ratio)
        .value(ANGLE, Sample::angle)
        .value(AT, Sample::at)
        .value(DAY, Sample::day)
        .value(TAGS, Sample::tags)
        .value(DATA, Sample::data)
        .value(BANNED_AT, Sample::bannedAt)
        .value(KIND, Sample::kind)
        .named("test.insert_sample");

    private static final Select<Sample> BY_ID = select().where(ID.eq()).map(SqlStatementsIntegrationTest::sample).named("test.sample_by_id");

    private static final Write<Counted> COUNT = Sql.<Counted>insertInto(COUNTERS)
        .as("c")
        .value(COUNTER_KEY, Counted::key)
        .value(COUNTER_VALUE, Counted::value)
        .value(COUNTER_NOTE, Counted::note)
        .onConflict(COUNTER_KEY).doUpdate(COUNTER_VALUE, "c.value + EXCLUDED.value")
        .named("test.count");

    private static final Write<Counted> REPLACE_NOTE = Sql.<Counted>insertInto(COUNTERS)
        .value(COUNTER_KEY, Counted::key)
        .value(COUNTER_VALUE, Counted::value)
        .value(COUNTER_NOTE, Counted::note)
        .onConflict(COUNTER_KEY).doUpdate(COUNTER_NOTE)
        .named("test.replace_note");

    private static final Select<Counted> COUNTER = Sql.select(COUNTER_KEY, COUNTER_VALUE, COUNTER_NOTE).from(COUNTERS)
        .where(COUNTER_KEY.eq())
        .map(row -> new Counted(row.get(COUNTER_KEY), row.get(COUNTER_VALUE), row.get(COUNTER_NOTE)))
        .named("test.counter");

    private static final Select<Long> SAMPLES_ABOVE = Sql.query("SELECT count(*) AS total FROM sqltest.samples WHERE big >= ?")
        .parameters(BIG)
        .columns(TOTAL)
        .map(row -> row.get(TOTAL))
        .named("test.samples_above");

    private static final Write<Void> CLEAR = Sql.<Void>statement("DELETE FROM sqltest.samples WHERE big >= ?").parameter(BIG).named("test.clear");

    private static Database database;
    private static CountingDataSource counting;
    private static SimpleMetricRegistry metrics;
    private static JdbcTransactions transactions;

    @BeforeAll
    static void createTables() throws SQLException {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 8, Duration.ofSeconds(5), false);
        database = Database.connect(config, "sql-test", new SimpleMetricRegistry());
        try (Connection connection = database.dataSource().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA sqltest");
            statement.execute("""
                CREATE TABLE sqltest.samples (
                    id UUID PRIMARY KEY, name VARCHAR(32), flag BOOLEAN, small SMALLINT, number INTEGER, big BIGINT,
                    ratio DOUBLE PRECISION, angle REAL, at TIMESTAMPTZ, day DATE, tags TEXT[], data BYTEA, banned_at TIMESTAMPTZ, kind VARCHAR(16)
                )
                """);
            statement.execute("CREATE TABLE sqltest.counters (key VARCHAR(32) PRIMARY KEY, value BIGINT NOT NULL, note TEXT)");
        }
        counting = new CountingDataSource(database.dataSource());
        metrics = new SimpleMetricRegistry();
        transactions = new JdbcTransactions(counting.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry(),
            new StatementMetrics(metrics, Duration.ofSeconds(10), Logger.getAnonymousLogger()));
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @BeforeEach
    void emptyTables() {
        transactions.inTransaction("test.empty", () -> CLEAR.execute(BIG.is(Long.MIN_VALUE)));
    }

    @Test
    void everyTypeIsWrittenAndReadBack() {
        Sample full = sample(1);
        Sample empty = new Sample(UUID.randomUUID(), null, null, null, null, Long.MIN_VALUE + 1, null, null, null, null, null, null, Optional.empty(), null);

        write(INSERT, full);
        write(INSERT, empty);

        assertThat(find(full.id())).hasValueSatisfying(found -> assertSame(found, full));
        assertThat(find(empty.id())).hasValueSatisfying(found -> assertSame(found, empty));
    }

    @Test
    void anyMatchesManyValuesOfEachComparableType() {
        Sample first = sample(1);
        Sample second = sample(2);
        Sample third = sample(3);
        transactions.inTransaction("test.insert", () -> {
            INSERT.batch(List.of(first, second, third));
            return null;
        });

        assertThat(ids(select().where(ID.in()), ID.in(List.of(first.id(), third.id())))).containsExactlyInAnyOrder(first.id(), third.id());
        assertThat(ids(select().where(NAME.in()), NAME.in(List.of("sample-2")))).containsExactly(second.id());
        assertThat(ids(select().where(NUMBER.in()), NUMBER.in(List.of(1, 2)))).containsExactlyInAnyOrder(first.id(), second.id());
        assertThat(ids(select().where(DAY.in()), DAY.in(List.of(third.day())))).containsExactly(third.id());
        assertThat(ids(select().where(AT.in()), AT.in(List.of(first.at(), second.at())))).containsExactlyInAnyOrder(first.id(), second.id());
        assertThat(ids(select().where(KIND.in()), KIND.in(List.of(Kind.ODD)))).containsExactlyInAnyOrder(first.id(), third.id());
        assertThat(ids(select().where(ID.in()), ID.in(List.of()))).isEmpty();
    }

    @Test
    void aBatchOfAThousandRowsIsOneRoundTrip() {
        List<Sample> samples = IntStream.rangeClosed(1, 1000).mapToObj(SqlStatementsIntegrationTest::sample).toList();
        counting.reset();

        transactions.inTransaction("test.batch", () -> {
            INSERT.batch(samples);
            return null;
        });

        assertThat(counting.roundTrips()).isEqualTo(1);
        assertThat(transactions.readOnly("test.count", () -> SAMPLES_ABOVE.first(BIG.is(0L)))).contains(1000L);
    }

    @Test
    void anUpsertChangesOnlyTheChosenColumns() {
        write(REPLACE_NOTE, new Counted("upsert", 5, "first"));
        write(REPLACE_NOTE, new Counted("upsert", 99, "second"));

        assertThat(transactions.readOnly("test.find", () -> COUNTER.first(COUNTER_KEY.is("upsert")))).contains(new Counted("upsert", 5, "second"));
    }

    @Test
    void aBatchOfUpsertsWithDifferentKeysIsApplied() {
        write(COUNT, new Counted("batch-a", 1, null));
        transactions.inTransaction("test.batch", () -> {
            COUNT.batch(List.of(new Counted("batch-a", 2, null), new Counted("batch-b", 3, null)));
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> COUNTER.first(COUNTER_KEY.is("batch-a")))).map(Counted::value).contains(3L);
        assertThat(transactions.readOnly("test.find", () -> COUNTER.first(COUNTER_KEY.is("batch-b")))).map(Counted::value).contains(3L);
    }

    @Test
    void concurrentUpsertsAddUpWithoutLosingAnyIncrement() throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> increments = IntStream.range(0, 64)
                .<Future<?>>mapToObj(index -> executor.submit(() -> write(COUNT, new Counted("concurrent", 1, null))))
                .toList();
            for (Future<?> increment : increments) {
                increment.get();
            }
        }

        assertThat(transactions.readOnly("test.find", () -> COUNTER.first(COUNTER_KEY.is("concurrent")))).map(Counted::value).contains(64L);
    }

    @Test
    void aFailedTransactionRollsBackItsWrites() {
        Sample sample = sample(7);

        assertThatThrownBy(() -> transactions.inTransaction("test.fail", () -> {
            INSERT.execute(sample);
            throw new IllegalStateException("boom");
        })).hasMessage("boom");

        assertThat(find(sample.id())).isEmpty();
    }

    @Test
    void aFailedBatchWritesNothing() {
        Sample sample = sample(8);

        assertThatThrownBy(() -> transactions.inTransaction("test.batch", () -> {
            INSERT.batch(List.of(sample(9), sample, sample));
            return null;
        })).isInstanceOf(PersistenceException.class).hasMessageContaining("test.insert_sample");

        assertThat(transactions.readOnly("test.count", () -> SAMPLES_ABOVE.first(BIG.is(0L)))).contains(0L);
    }

    @Test
    void argumentsMustMatchTheParametersOfTheStatement() {
        assertThatThrownBy(() -> transactions.readOnly("test.find", () -> BY_ID.first())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("needs a value");
        assertThatThrownBy(() -> transactions.readOnly("test.find", () -> BY_ID.first(ID.is(UUID.randomUUID()), NAME.is("x"))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no parameter");
        assertThatThrownBy(() -> transactions.inTransaction("test.insert", () -> INSERT.execute(ID.is(UUID.randomUUID()))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no parameter");
        assertThatThrownBy(() -> transactions.inTransaction("test.insert", () -> INSERT.execute()))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("reads samples.id from a value");
        assertThatThrownBy(() -> BY_ID.first(ID.is(UUID.randomUUID()))).isInstanceOf(IllegalStateException.class).hasMessageContaining("No active transaction");
    }

    @Test
    void everyStatementIsTimedUnderItsName() {
        long counted = executions("test.count", "success");
        long read = executions("test.counter", "success");
        long failed = executions("test.insert_sample", "error");
        Sample sample = sample(11);

        write(COUNT, new Counted("metrics", 1, null));
        transactions.readOnly("test.find", () -> COUNTER.first(COUNTER_KEY.is("metrics")));
        write(INSERT, sample);
        assertThatThrownBy(() -> write(INSERT, sample)).isInstanceOf(PersistenceException.class);

        assertThat(executions("test.count", "success")).isEqualTo(counted + 1);
        assertThat(executions("test.counter", "success")).isEqualTo(read + 1);
        assertThat(executions("test.insert_sample", "error")).isEqualTo(failed + 1);
    }

    private static long executions(String statement, String outcome) {
        return metrics.timer("muxe_db_statement_seconds", Tags.of("statement", statement, "outcome", outcome)).snapshot().count();
    }

    private static <T> void write(Write<T> write, T value) {
        transactions.inTransaction("test.write", () -> write.execute(value));
    }

    private static Optional<Sample> find(UUID id) {
        return transactions.readOnly("test.find", () -> BY_ID.first(ID.is(id)));
    }

    private static List<UUID> ids(SelectBuilder<Object> select, Argument argument) {
        Select<UUID> ids = select.map(row -> row.get(ID)).named("test.ids");
        return transactions.readOnly("test.ids", () -> ids.list(argument));
    }

    private static SelectBuilder<Object> select() {
        return Sql.select(SAMPLES.columns()).from(SAMPLES);
    }

    private static Sample sample(int index) {
        return new Sample(UUID.randomUUID(), "sample-" + index, index % 2 == 0, index % 100, index, (long) index, index / 3.0, index / 7.0f,
            NOW.plusSeconds(index), LocalDate.of(2026, 1, 1).plusDays(index), List.of("a" + index, "b"), new byte[] {(byte) index, 7},
            Optional.of(NOW.minusSeconds(index)), index % 2 == 0 ? Kind.EVEN : Kind.ODD);
    }

    private static Sample sample(Row row) throws SQLException {
        return new Sample(row.get(ID), row.get(NAME), row.get(FLAG), row.get(SMALL), row.get(NUMBER), row.get(BIG), row.get(RATIO), row.get(ANGLE),
            row.get(AT), row.get(DAY), row.get(TAGS), row.get(DATA), row.get(BANNED_AT), row.get(KIND));
    }

    private static void assertSame(Sample found, Sample expected) {
        assertThat(found).usingRecursiveComparison().isEqualTo(expected);
    }

    private enum Kind {
        EVEN,
        ODD
    }

    private record Sample(UUID id, String name, Boolean flag, Integer small, Integer number, Long big, Double ratio, Float angle,
        Instant at, LocalDate day, List<String> tags, byte[] data, Optional<Instant> bannedAt, Kind kind) {
    }

    private record Counted(String key, long value, String note) {
    }
}
