package br.com.muxe.core.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.account.domain.Account;
import br.com.muxe.core.common.account.infrastructure.postgres.PostgresAccountRepository;
import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.mission.domain.MissionProgress;
import br.com.muxe.core.common.mission.infrastructure.postgres.PostgresMissionRepository;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.permission.application.command.ApplyPermissionChangesCommand;
import br.com.muxe.core.common.permission.application.command.ApplyPermissionChangesHandler;
import br.com.muxe.core.common.permission.domain.Group;
import br.com.muxe.core.common.permission.domain.PermissionChange;
import br.com.muxe.core.common.permission.domain.PermissionNode;
import br.com.muxe.core.common.permission.domain.PermissionUser;
import br.com.muxe.core.common.permission.infrastructure.postgres.PostgresPermissionRepository;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PersistenceRoundTripsIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static final Instant NOW = Instant.parse("2026-11-07T12:00:00Z");
    private static final int WARM_UPS = 3;
    private static final int RUNS = 10;

    private static Database database;
    private static CountingDataSource counting;
    private static JdbcTransactions transactions;

    private final PostgresPermissionRepository permissions = new PostgresPermissionRepository();
    private final PostgresAccountRepository accounts = new PostgresAccountRepository();
    private final PostgresMissionRepository missions = new PostgresMissionRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "round-trips-test", new SimpleMetricRegistry());
        new MigrationRunner(database.dataSource()).migrate("core", PersistenceRoundTripsIntegrationTest.class.getClassLoader());
        counting = new CountingDataSource(database.dataSource());
        transactions = new JdbcTransactions(counting.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void applyingTwentyUserChangesTakesAFixedNumberOfRoundTrips() {
        ApplyPermissionChangesHandler handler = new ApplyPermissionChangesHandler(permissions, transactions, Clock.fixed(NOW, ZoneOffset.UTC));
        List<PermissionChange> changes = IntStream.range(0, 20)
            .mapToObj(index -> (PermissionChange) new PermissionChange.PutUser(user(UUID.randomUUID())))
            .toList();

        Measurement measurement = measure(() -> assertThat(handler.handle(new ApplyPermissionChangesCommand("benchmark", changes)).isSuccess()).isTrue());

        report("apply 20 user changes", measurement);
        assertThat(measurement.roundTrips()).isEqualTo(10);
    }

    @Test
    void savingAGroupWithTwoThousandNodesIsOneBatch() {
        Group group = new Group("benchmark", "Benchmark", 1, Optional.empty(), IntStream.range(0, 2000)
            .mapToObj(index -> PermissionNode.permanent("muxe.benchmark." + index, true, NOW))
            .toList());

        Measurement measurement = measure(() -> transactions.inTransaction("benchmark.save_group", () -> {
            permissions.saveGroup(group);
            return null;
        }));

        report("save a group with 2000 nodes", measurement);
        assertThat(measurement.roundTrips()).isEqualTo(3);
    }

    @Test
    void lookingUpTwoHundredAccountsOneByOne() {
        List<UUID> ids = savedAccounts(200);

        Measurement measurement = measure(() -> transactions.readOnly("benchmark.find_accounts", () -> {
            List<Account> found = new ArrayList<>();
            for (UUID id : ids) {
                accounts.findById(id).ifPresent(found::add);
            }
            assertThat(found).hasSize(200);
            return null;
        }));

        report("find 200 accounts one by one", measurement);
        assertThat(measurement.roundTrips()).isEqualTo(200);
    }

    @Test
    void lookingUpTwoHundredAccountsAtOnce() {
        List<UUID> ids = savedAccounts(200);

        Measurement measurement = measure(() -> assertThat(transactions.readOnly("benchmark.find_accounts", () -> accounts.findByIds(ids))).hasSize(200));

        report("find 200 accounts at once", measurement);
        assertThat(measurement.roundTrips()).isEqualTo(1);
    }

    @Test
    void findingTheProgressOfTwentyMissionsIsOneQuery() {
        UUID player = UUID.randomUUID();
        Map<String, LocalDate> wanted = new LinkedHashMap<>();
        transactions.inTransaction("benchmark.save_missions", () -> {
            for (int index = 0; index < 20; index++) {
                LocalDate start = LocalDate.of(2026, 11, 1 + index % 7);
                wanted.put("mission-" + index, start);
                missions.save("bedwars", player, new MissionProgress("mission-" + index, start, index, index % 2 == 0));
            }
            return null;
        });

        Measurement measurement = measure(() -> assertThat(transactions.readOnly("benchmark.find_missions", () -> missions.find("bedwars", player, wanted))).hasSize(20));

        report("find 20 missions", measurement);
        assertThat(measurement.roundTrips()).isEqualTo(1);
    }

    private List<UUID> savedAccounts(int count) {
        List<UUID> ids = IntStream.range(0, count).mapToObj(index -> UUID.randomUUID()).toList();
        transactions.inTransaction("benchmark.save_accounts", () -> {
            for (int index = 0; index < count; index++) {
                accounts.save(new Account(ids.get(index), ids.get(index).toString().replace("-", "").substring(0, 12), false, NOW, NOW));
            }
            return null;
        });
        return ids;
    }

    private static PermissionUser user(UUID id) {
        List<PermissionNode> nodes = IntStream.range(0, 5).mapToObj(index -> PermissionNode.permanent("muxe.benchmark.user." + index, true, NOW)).toList();
        return new PermissionUser(id, nodes, Optional.of("vip"), Optional.empty());
    }

    private static Measurement measure(Runnable work) {
        for (int run = 0; run < WARM_UPS; run++) {
            work.run();
        }
        counting.reset();
        long[] durations = new long[RUNS];
        for (int run = 0; run < RUNS; run++) {
            long start = System.nanoTime();
            work.run();
            durations[run] = System.nanoTime() - start;
        }
        Arrays.sort(durations);
        return new Measurement(counting.roundTrips() / RUNS, durations[RUNS / 2] / 1_000_000.0);
    }

    private static void report(String scenario, Measurement measurement) {
        System.out.printf("[round-trips] %s: %d round trips, median %.2f ms%n", scenario, measurement.roundTrips(), measurement.medianMillis());
    }

    private record Measurement(int roundTrips, double medianMillis) {
    }
}
