package br.com.muxe.core.common.persistence.sql.observe;

import br.com.muxe.core.common.metrics.MetricRegistry;
import br.com.muxe.core.common.metrics.Tags;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;

public final class StatementMetrics implements StatementObserver {

    private final MetricRegistry metrics;
    private final long slowNanos;
    private final Logger logger;

    public StatementMetrics(MetricRegistry metrics, Duration slowThreshold, Logger logger) {
        this.metrics = Objects.requireNonNull(metrics, "metrics");
        this.slowNanos = Objects.requireNonNull(slowThreshold, "slowThreshold").toNanos();
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void executed(String statement, long nanos, boolean success) {
        metrics.timer("muxe_db_statement_seconds", Tags.of("statement", statement, "outcome", success ? "success" : "error")).record(nanos);
        if (nanos >= slowNanos) {
            metrics.counter("muxe_db_slow_statements_total", Tags.of("statement", statement)).increment();
            logger.warning(() -> "Slow SQL statement '" + statement + "' took " + Duration.ofNanos(nanos).toMillis()
                + " ms (limit " + Duration.ofNanos(slowNanos).toMillis() + " ms)");
        }
    }
}
