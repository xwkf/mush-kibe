package br.com.muxe.core.common.arena.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
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
class PostgresArenaRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final Instant NOW = Instant.parse("2026-11-02T15:00:00Z");

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresArenaRepository repository = new PostgresArenaRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "test", new SimpleMetricRegistry());
        new MigrationRunner(database.dataSource()).migrate("core", PostgresArenaRepositoryIntegrationTest.class.getClassLoader());
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void anArenaAndItsMapSurviveARoundTripPerGame() {
        String game = "game-" + UUID.randomUUID().toString().substring(0, 8);
        byte[] map = {7, 7, 7, 1, 2, 3};
        transactions.inTransaction("test.save", () -> {
            repository.save(new ArenaRecord(game, "quartetos_teste", "quartetos", false, "name: quartetos_teste", map, NOW), false);
            repository.save(new ArenaRecord(game, "quartetos_teste", "quartetos", true, "name: quartetos_teste\nready: true", new byte[0], NOW.plusSeconds(5)), true);
            repository.save(new ArenaRecord(game, "1v1_slip", "1v1", true, "name: 1v1_slip", new byte[] {9}, NOW), false);
            return null;
        });

        ArenaRecord found = transactions.readOnly("test.find", () -> repository.find(game, "quartetos_teste")).orElseThrow();

        assertThat(found.ready()).isTrue();
        assertThat(found.definition()).contains("ready: true");
        assertThat(found.map()).containsExactly(7, 7, 7, 1, 2, 3);
        assertThat(found.updatedAt()).isEqualTo(NOW.plusSeconds(5));
        assertThat(transactions.readOnly("test.names", () -> repository.names(game))).containsExactly("1v1_slip", "quartetos_teste");
        assertThat(transactions.readOnly("test.names", () -> repository.names("other-" + game))).isEmpty();
    }

    @Test
    void deletingAnArenaRemovesOnlyThatGamesRow() {
        String game = "game-" + UUID.randomUUID().toString().substring(0, 8);
        transactions.inTransaction("test.save", () -> {
            repository.save(new ArenaRecord(game, "majestic_solo", "solo", false, "name: majestic_solo", new byte[] {1}, NOW), false);
            repository.save(new ArenaRecord("other-" + game, "majestic_solo", "solo", false, "name: majestic_solo", new byte[] {1}, NOW), false);
            return null;
        });

        assertThat(transactions.inTransaction("test.delete", () -> repository.delete(game, "majestic_solo"))).isTrue();
        assertThat(transactions.inTransaction("test.delete", () -> repository.delete(game, "majestic_solo"))).isFalse();
        assertThat(transactions.readOnly("test.find", () -> repository.find(game, "majestic_solo"))).isEmpty();
        assertThat(transactions.readOnly("test.find", () -> repository.find("other-" + game, "majestic_solo"))).isPresent();
    }
}
