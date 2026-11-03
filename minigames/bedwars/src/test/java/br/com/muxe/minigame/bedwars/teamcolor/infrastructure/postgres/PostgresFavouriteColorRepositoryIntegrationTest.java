package br.com.muxe.minigame.bedwars.teamcolor.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import br.com.muxe.minigame.engine.team.TeamColor;
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
class PostgresFavouriteColorRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final Instant NOW = Instant.parse("2026-11-03T03:00:00Z");

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresFavouriteColorRepository repository = new PostgresFavouriteColorRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "bedwars-team-color-test", new SimpleMetricRegistry());
        ClassLoader classLoader = PostgresFavouriteColorRepositoryIntegrationTest.class.getClassLoader();
        MigrationRunner migrations = new MigrationRunner(database.dataSource());
        migrations.migrate("core", classLoader);
        migrations.migrate("bedwars", classLoader);
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    private void save(UUID player, TeamColor color, Instant at) {
        transactions.inTransaction("test.save", () -> {
            repository.save(player, color, at);
            return null;
        });
    }

    @Test
    void aPlayerWithoutASavedColourHasNone() {
        assertThat(transactions.readOnly("test.find", () -> repository.find(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void theSavedColourComesBack() {
        UUID player = UUID.randomUUID();

        save(player, TeamColor.LARANJA, NOW);

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(TeamColor.LARANJA);
    }

    @Test
    void savingAgainReplacesTheColour() {
        UUID player = UUID.randomUUID();

        save(player, TeamColor.LARANJA, NOW);
        save(player, TeamColor.CIANO, NOW.plusSeconds(1));

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(TeamColor.CIANO);
    }

    @Test
    void clearingForgetsOnlyThatPlayer() {
        UUID player = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        save(player, TeamColor.ROSA, NOW);
        save(other, TeamColor.ROSA, NOW);

        transactions.inTransaction("test.clear", () -> {
            repository.clear(player);
            return null;
        });

        assertThat(transactions.readOnly("test.find", () -> repository.find(player))).isEmpty();
        assertThat(transactions.readOnly("test.find", () -> repository.find(other))).contains(TeamColor.ROSA);
    }

    @Test
    void everyTeamColourRoundTrips() {
        for (TeamColor color : TeamColor.values()) {
            UUID player = UUID.randomUUID();

            save(player, color, NOW);

            assertThat(transactions.readOnly("test.find", () -> repository.find(player))).contains(color);
        }
    }
}
