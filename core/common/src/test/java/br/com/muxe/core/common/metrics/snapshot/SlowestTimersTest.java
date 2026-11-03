package br.com.muxe.core.common.metrics.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.metrics.MetricId;
import br.com.muxe.core.common.metrics.Tags;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SlowestTimersTest {

    private static final double[] BOUNDS = {0.1};

    @Test
    void listsTheTimersWithTheHighestAverageFirst() {
        MetricsSnapshot snapshot = snapshot(Map.of(
            new MetricId("muxe_cqrs_handle_seconds", Tags.of("kind", "query", "message", "find_account")), timer(4, 8_000_000, 5_000_000),
            new MetricId("muxe_executor_queue_wait_seconds", Tags.of("executor", "queries")), timer(2, 600_000_000, 400_000_000),
            new MetricId("muxe_packet_latency_seconds", Tags.EMPTY), timer(10, 30_000_000, 9_000_000)));

        List<SlowestTimers.Line> lines = SlowestTimers.of(snapshot, 2);

        assertThat(lines).containsExactly(
            new SlowestTimers.Line("executor_queue_wait queries", 2, 300.0, 400.0),
            new SlowestTimers.Line("packet_latency", 10, 3.0, 9.0));
    }

    @Test
    void skipsTimersThatNeverRan() {
        MetricsSnapshot snapshot = snapshot(Map.of(new MetricId("muxe_idle_seconds", Tags.EMPTY), timer(0, 0, 0)));

        assertThat(SlowestTimers.of(snapshot, 5)).isEmpty();
    }

    @Test
    void namesATimerByItsMetricAndTagValues() {
        MetricId id = new MetricId("muxe_cqrs_handle_seconds", Tags.of("kind", "query", "message", "find_account", "outcome", "success"));

        assertThat(SlowestTimers.label(id)).isEqualTo("cqrs_handle query/find_account/success");
    }

    private static MetricsSnapshot snapshot(Map<MetricId, TimerSnapshot> timers) {
        return new MetricsSnapshot(Map.of(), timers, Map.of());
    }

    private static TimerSnapshot timer(long count, long totalNanos, long maxNanos) {
        return new TimerSnapshot(count, totalNanos, maxNanos, BOUNDS, new long[] {count});
    }
}
