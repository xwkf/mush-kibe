package br.com.muxe.minigame.bedwars.hotbar.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgresHotbarRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final Instant NOW = Instant.parse("2026-11-03T03:00:00Z");

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresHotbarRepository repository = new PostgresHotbarRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "bedwars-hotbar-test", new SimpleMetricRegistry());
        ClassLoader classLoader = PostgresHotbarRepositoryIntegrationTest.class.getClassLoader();
        MigrationRunner migrations = new MigrationRunner(database.dataSource());
        migrations.migrate("core", classLoader);
        migrations.migrate("bedwars", classLoader);
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void aPlayerWithoutASavedHotbarHasNone() {
        assertThat(transactions.readOnly("test.find", () -> repository.find(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void theSavedSlotsComeBackWithTheirEntriesInOrder() {
        UUID player = UUID.randomUUID();
        HotbarLayout layout = HotbarLayout.empty()
            .add(0, HotbarEntry.category("blocos"))
            .add(0, HotbarEntry.item("la"))
            .add(3, HotbarEntry.item("picareta"))
            .add(8, HotbarEntry.category("utilidades"));

        transactions.inTransaction("test.save", () -> {
            repository.save(player, layout, NOW);
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(layout);
    }

    @Test
    void anEmptyHotbarIsSavedAndComesBackEmpty() {
        UUID player = UUID.randomUUID();

        transactions.inTransaction("test.save", () -> {
            repository.save(player, HotbarLayout.empty(), NOW);
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(HotbarLayout.empty());
    }

    @Test
    void savingAgainReplacesTheHotbar() {
        UUID player = UUID.randomUUID();
        HotbarLayout latest = HotbarLayout.empty().add(2, HotbarEntry.item("tnt"));

        transactions.inTransaction("test.save", () -> {
            repository.save(player, HotbarLayout.empty().add(0, HotbarEntry.item("la")), NOW);
            repository.save(player, latest, NOW.plusSeconds(1));
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(latest);
    }
}
