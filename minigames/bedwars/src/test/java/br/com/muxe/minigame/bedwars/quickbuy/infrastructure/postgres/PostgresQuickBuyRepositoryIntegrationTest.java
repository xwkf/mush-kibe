package br.com.muxe.minigame.bedwars.quickbuy.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgresQuickBuyRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final Instant NOW = Instant.parse("2026-11-03T03:00:00Z");

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresQuickBuyRepository repository = new PostgresQuickBuyRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "bedwars-quick-buy-test", new SimpleMetricRegistry());
        ClassLoader classLoader = PostgresQuickBuyRepositoryIntegrationTest.class.getClassLoader();
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
    void aPlayerWithoutASavedQuickBuyHasNone() {
        assertThat(transactions.readOnly("test.find", () -> repository.find(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void theSavedSlotsComeBackInOrderWithTheEmptyOnes() {
        UUID player = UUID.randomUUID();
        QuickBuy quickBuy = QuickBuy.of(List.of("argila", "", "tnt")).place(20, "perola");

        transactions.inTransaction("test.save", () -> {
            repository.save(player, quickBuy, NOW);
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(quickBuy);
    }

    @Test
    void savingAgainReplacesTheQuickBuy() {
        UUID player = UUID.randomUUID();
        QuickBuy latest = QuickBuy.of(List.of("la"));

        transactions.inTransaction("test.save", () -> {
            repository.save(player, QuickBuy.of(List.of("tnt")), NOW);
            repository.save(player, latest, NOW.plusSeconds(1));
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(latest);
    }
}
