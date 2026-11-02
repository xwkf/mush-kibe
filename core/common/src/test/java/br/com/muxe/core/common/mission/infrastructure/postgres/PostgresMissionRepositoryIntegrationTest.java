package br.com.muxe.core.common.mission.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.mission.domain.MissionProgress;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgresMissionRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresMissionRepository repository = new PostgresMissionRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "test", new SimpleMetricRegistry());
        new MigrationRunner(database.dataSource()).migrate("core", PostgresMissionRepositoryIntegrationTest.class.getClassLoader());
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void progressIsKeptPerGameMissionAndPeriod() {
        UUID player = UUID.randomUUID();
        transactions.inTransaction("test.save", () -> {
            repository.save("bedwars", player, new MissionProgress("fim-da-linha", MONDAY, 3, false));
            repository.save("bedwars", player, new MissionProgress("fim-da-linha", MONDAY, 35, true));
            repository.save("skywars", player, new MissionProgress("fim-da-linha", MONDAY, 7, false));
            return null;
        });

        Map<String, MissionProgress> bedwars = transactions.readOnly("test.find",
            () -> repository.find("bedwars", player, Map.of("fim-da-linha", MONDAY, "artesao", MONDAY)));
        Map<String, MissionProgress> nextWeek = transactions.readOnly("test.find",
            () -> repository.find("bedwars", player, Map.of("fim-da-linha", MONDAY.plusWeeks(1))));

        assertThat(bedwars).containsOnlyKeys("fim-da-linha");
        assertThat(bedwars.get("fim-da-linha")).isEqualTo(new MissionProgress("fim-da-linha", MONDAY, 35, true));
        assertThat(nextWeek).isEmpty();
    }
}
