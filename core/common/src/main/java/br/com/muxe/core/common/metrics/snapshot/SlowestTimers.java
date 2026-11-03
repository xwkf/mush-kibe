package br.com.muxe.core.common.metrics.snapshot;

import br.com.muxe.core.common.metrics.MetricId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class SlowestTimers {

    private static final double NANOS_PER_MILLI = 1_000_000.0;

    private SlowestTimers() {
    }

    public static List<Line> of(MetricsSnapshot snapshot, int limit) {
        return snapshot.timers().entrySet().stream()
            .filter(entry -> entry.getValue().count() > 0)
            .map(SlowestTimers::line)
            .sorted(Comparator.comparingDouble(Line::meanMillis).reversed().thenComparing(Line::label))
            .limit(Math.max(0, limit))
            .toList();
    }

    static String label(MetricId id) {
        String name = id.name().replaceFirst("^muxe_", "").replaceFirst("_seconds$", "");
        if (id.tags().isEmpty()) {
            return name;
        }
        return name + " " + String.join("/", id.tags().values().values());
    }

    private static Line line(Map.Entry<MetricId, TimerSnapshot> entry) {
        TimerSnapshot timer = entry.getValue();
        return new Line(label(entry.getKey()), timer.count(), timer.totalNanos() / NANOS_PER_MILLI / timer.count(), timer.maxNanos() / NANOS_PER_MILLI);
    }

    public record Line(String label, long count, double meanMillis, double maxMillis) {
    }
}
