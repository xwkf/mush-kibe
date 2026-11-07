package br.com.muxe.core.common.shop.infrastructure.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.concurrent.MainThreadGuard;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.core.common.persistence.database.Database;
import br.com.muxe.core.common.persistence.database.DatabaseConfig;
import br.com.muxe.core.common.persistence.migration.MigrationRunner;
import br.com.muxe.core.common.persistence.transaction.JdbcTransactions;
import br.com.muxe.core.common.shop.domain.Promotion;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgresPromotionRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

    private static Database database;
    private static JdbcTransactions transactions;

    private final PostgresPromotionRepository repository = new PostgresPromotionRepository();

    @BeforeAll
    static void migrate() {
        DatabaseConfig config = new DatabaseConfig(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), 2, Duration.ofSeconds(5), true);
        database = Database.connect(config, "shop-test", new SimpleMetricRegistry());
        new MigrationRunner(database.dataSource()).migrate("core", PostgresPromotionRepositoryIntegrationTest.class.getClassLoader());
        transactions = new JdbcTransactions(database.dataSource(), MainThreadGuard.none(), new SimpleMetricRegistry());
    }

    @AfterAll
    static void close() {
        database.close();
    }

    @Test
    void theNetworkHasOnePromotionThatIsReplaced() {
        transactions.inTransaction("test.save", () -> {
            repository.save(new Promotion("vip", List.of("&aVIP", "&7por 30 dias")), Instant.parse("2026-11-07T12:00:00Z"));
            repository.save(new Promotion("mvp", List.of("&bMVP")), Instant.parse("2026-11-07T12:05:00Z"));
            return null;
        });

        assertThat(transactions.readOnly("test.find", repository::find)).contains(new Promotion("mvp", List.of("&bMVP")));
    }

    @Test
    void aPromotionWithoutLinesKeepsAnEmptyList() {
        transactions.inTransaction("test.save", () -> {
            repository.save(new Promotion("pro", List.of()), Instant.parse("2026-11-07T13:00:00Z"));
            return null;
        });

        assertThat(transactions.readOnly("test.find", repository::find)).contains(new Promotion("pro", List.of()));
    }
}
