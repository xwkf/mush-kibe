package br.com.muxe.minigame.bedwars.stats.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.account.domain.Account;
import br.com.muxe.core.common.account.infrastructure.postgres.PostgresAccountRepository;
import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationReport;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import br.com.muxe.minigame.bedwars.stats.domain.MatchResult;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatCounters;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgresBedWarsStatsRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final Instant WEDNESDAY = Instant.parse("2026-11-04T15:00:00Z");

    private static Database database;
    private static JdbcTransactions transactions;
    private static MigrationReport migration;

    private final PostgresBedWarsStatsRepository repository = new PostgresBedWarsStatsRepository();
    private final String mode = "m" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "bedwars-test", new SimpleMetricRegistry());
        ClassLoader classLoader = PostgresBedWarsStatsRepositoryIntegrationTest.class.getClassLoader();
        MigrationRunner migrations = new MigrationRunner(database.dataSource());
        migrations.migrate("core", classLoader);
        migration = migrations.migrate("bedwars", classLoader);
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void migrationsCreateTheBedWarsSchema() {
        assertThat(migration.schemaVersion()).isEqualTo("1");
    }

    @Test
    void recordingAddsUpTheModeAndGeneralRowsAndTracksTheWinstreak() {
        UUID player = UUID.randomUUID();

        record(outcome(player, 2, MatchResult.WIN, 100), WEDNESDAY);
        record(outcome(player, 1, MatchResult.WIN, 50), WEDNESDAY);
        record(outcome(player, 0, MatchResult.NONE, 0), WEDNESDAY);
        record(outcome(player, 3, MatchResult.LOSS, 20), WEDNESDAY.plus(Duration.ofDays(1)));

        PlayerStats totals = transactions.readOnly("test.totals", () -> repository.totals(player));
        StatCounters expected = new StatCounters(4, 0, 0, 4, 4, 0, 6, 0, 0, 2, 1, 0, 2, 170);
        assertThat(totals.of(mode)).isEqualTo(expected);
        assertThat(totals.general()).isEqualTo(expected);

        PlayerStats wednesday = transactions.readOnly("test.period", () -> repository.period(player, StatsPeriod.DAILY, WEDNESDAY, SAO_PAULO));
        assertThat(wednesday.general().games()).isEqualTo(3);
        assertThat(wednesday.general().winstreak()).isEqualTo(2);
        assertThat(wednesday.general().bestWinstreak()).isEqualTo(2);

        PlayerStats week = transactions.readOnly("test.period", () -> repository.period(player, StatsPeriod.WEEKLY, WEDNESDAY, SAO_PAULO));
        assertThat(week.of(mode)).isEqualTo(expected);
    }

    @Test
    void topsJoinTheAccountNameSkipZerosAndOrderStably() {
        UUID named = UUID.randomUUID();
        UUID unnamed = UUID.randomUUID();
        UUID zero = UUID.randomUUID();
        String name = "Bw" + UUID.randomUUID().toString().substring(0, 8);
        transactions.inTransaction("test.account", () -> {
            new PostgresAccountRepository().save(Account.firstLogin(named, name, true, WEDNESDAY));
            return null;
        });

        record(outcome(named, 4, MatchResult.WIN, 300), WEDNESDAY);
        record(outcome(unnamed, 7, MatchResult.LOSS, 80), WEDNESDAY);
        record(outcome(zero, 0, MatchResult.LOSS, 10), WEDNESDAY);

        LocalDate start = StatsPeriod.DAILY.start(WEDNESDAY, SAO_PAULO);
        List<TopEntry> finalKills = transactions.readOnly("test.top", () -> repository.top(TopCategory.FINAL_KILLS, mode, StatsPeriod.DAILY, start, 10));

        assertThat(finalKills).containsExactly(
            new TopEntry(1, unnamed, unnamed.toString(), 7, 80),
            new TopEntry(2, named, name, 4, 300)
        );
        assertThat(transactions.readOnly("test.top", () -> repository.top(TopCategory.LEVEL, mode, StatsPeriod.TOTAL, StatsPeriod.TOTAL_START, 2)))
            .extracting(TopEntry::playerId)
            .containsExactly(named, unnamed);
        assertThat(transactions.readOnly("test.top", () -> repository.top(TopCategory.WINSTREAK, mode, StatsPeriod.WEEKLY, StatsPeriod.WEEKLY.start(WEDNESDAY, SAO_PAULO), 10)))
            .extracting(TopEntry::playerId)
            .containsExactly(named);
        assertThat(transactions.readOnly("test.top", () -> repository.top(TopCategory.WINS, mode, StatsPeriod.DAILY, start.plusDays(1), 10)))
            .isEmpty();
    }

    @Test
    void favouriteMapsToggle() {
        UUID player = UUID.randomUUID();

        assertThat(toggle(player, "Lighthouse")).isTrue();
        assertThat(toggle(player, "Aquarium")).isTrue();
        assertThat(transactions.readOnly("test.favourites", () -> repository.favouriteMaps(player))).containsExactly("Aquarium", "Lighthouse");

        assertThat(toggle(player, "Lighthouse")).isFalse();
        assertThat(transactions.readOnly("test.favourites", () -> repository.favouriteMaps(player))).containsExactly("Aquarium");
    }

    private void record(MatchOutcome outcome, Instant at) {
        transactions.inTransaction("test.record", () -> {
            repository.record(outcome, at, SAO_PAULO);
            return null;
        });
    }

    private boolean toggle(UUID player, String arena) {
        return transactions.inTransaction("test.toggle", () -> repository.toggleFavouriteMap(player, arena, WEDNESDAY));
    }

    private MatchOutcome outcome(UUID player, long finalKills, MatchResult result, long xp) {
        return new MatchOutcome(player, mode, 0, 0, 1, 1, 0, finalKills, 0, 0, result, xp);
    }
}
