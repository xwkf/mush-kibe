package br.com.muxe.core.common.persistence.sql.observe;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.metrics.Tags;
import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class StatementMetricsTest {

    private final SimpleMetricRegistry metrics = new SimpleMetricRegistry();
    private final List<String> warnings = new ArrayList<>();
    private final StatementMetrics observer = new StatementMetrics(metrics, Duration.ofMillis(100), logger());

    @Test
    void everyExecutionIsTimedByStatementAndOutcome() {
        observer.executed("account.find_by_id", Duration.ofMillis(2).toNanos(), true);
        observer.executed("account.find_by_id", Duration.ofMillis(3).toNanos(), true);
        observer.executed("account.find_by_id", Duration.ofMillis(1).toNanos(), false);

        assertThat(metrics.timer("muxe_db_statement_seconds", Tags.of("statement", "account.find_by_id", "outcome", "success")).snapshot().count()).isEqualTo(2);
        assertThat(metrics.timer("muxe_db_statement_seconds", Tags.of("statement", "account.find_by_id", "outcome", "error")).snapshot().count()).isEqualTo(1);
        assertThat(warnings).isEmpty();
    }

    @Test
    void aSlowStatementIsCountedAndLogged() {
        observer.executed("bedwars.stats.top", Duration.ofMillis(340).toNanos(), true);

        assertThat(metrics.counter("muxe_db_slow_statements_total", Tags.of("statement", "bedwars.stats.top")).count()).isEqualTo(1);
        assertThat(warnings).containsExactly("Slow SQL statement 'bedwars.stats.top' took 340 ms (limit 100 ms)");
    }

    private Logger logger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        return logger;
    }
}
