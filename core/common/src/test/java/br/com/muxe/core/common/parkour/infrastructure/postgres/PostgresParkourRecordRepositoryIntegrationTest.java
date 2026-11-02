package br.com.muxe.core.common.parkour.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.parkour.application.command.SubmitParkourTimeCommand;
import br.com.muxe.core.common.parkour.application.command.SubmitParkourTimeHandler;
import br.com.muxe.core.common.parkour.domain.ParkourOutcome;
import br.com.muxe.core.common.parkour.domain.ParkourRecordRepository;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgresParkourRecordRepositoryIntegrationTest {

    private static final Duration RACE_WINDOW = Duration.ofSeconds(1);

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresParkourRecordRepository repository = new PostgresParkourRecordRepository();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-02T12:00:00Z"));
    private final UUID player = UUID.randomUUID();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 4, Duration.ofSeconds(5), true);
        database = Database.connect(config, "test", new SimpleMetricRegistry());
        new MigrationRunner(database.dataSource()).migrate("core", PostgresParkourRecordRepositoryIntegrationTest.class.getClassLoader());
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void aSlowerTimeNeverReplacesAFasterRecord() {
        save(Duration.ofSeconds(50));

        save(Duration.ofSeconds(55));

        assertThat(best()).contains(Duration.ofSeconds(50));
    }

    @Test
    void simultaneousFinishesOfOnePlayerAreAppliedOneAfterTheOther() throws Exception {
        SubmitParkourTimeHandler submit = new SubmitParkourTimeHandler(new ReadThenWait(repository, new CyclicBarrier(2)), transactions, clock);

        List<ParkourOutcome> outcomes;
        try (ExecutorService runners = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<ParkourOutcome> slower = runners.submit(() -> submit.handle(command(Duration.ofSeconds(55))).orElseThrow());
            Future<ParkourOutcome> faster = runners.submit(() -> submit.handle(command(Duration.ofSeconds(50))).orElseThrow());
            outcomes = List.of(slower.get(10, TimeUnit.SECONDS), faster.get(10, TimeUnit.SECONDS));
        }

        List<ParkourOutcome> first = outcomes.stream().filter(outcome -> outcome.previous().isEmpty()).toList();
        assertThat(first).hasSize(1);
        ParkourOutcome second = outcomes.stream().filter(outcome -> outcome.previous().isPresent()).findFirst().orElseThrow();
        assertThat(second.previous()).contains(first.getFirst().time());
        assertThat(second.newRecord()).isEqualTo(second.time().compareTo(first.getFirst().time()) < 0);
        assertThat(best()).contains(Duration.ofSeconds(50));
    }

    private SubmitParkourTimeCommand command(Duration time) {
        return new SubmitParkourTimeCommand("lobby", player, time);
    }

    private void save(Duration time) {
        transactions.inTransaction("test.save", () -> {
            repository.save("lobby", player, time, clock.instant());
            return null;
        });
    }

    private Optional<Duration> best() {
        return transactions.readOnly("test.find", () -> repository.best("lobby", player));
    }

    private record ReadThenWait(ParkourRecordRepository records, CyclicBarrier bothRead) implements ParkourRecordRepository {

        @Override
        public Optional<Duration> best(String group, UUID playerId) {
            return records.best(group, playerId);
        }

        @Override
        public Optional<Duration> bestForUpdate(String group, UUID playerId) {
            Optional<Duration> best = records.bestForUpdate(group, playerId);
            try {
                bothRead.await(RACE_WINDOW.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException | BrokenBarrierException expectedWhenTheOtherFinishIsBlocked) {
                bothRead.reset();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
            return best;
        }

        @Override
        public void save(String group, UUID playerId, Duration best, Instant achievedAt) {
            records.save(group, playerId, best, achievedAt);
        }
    }
}
