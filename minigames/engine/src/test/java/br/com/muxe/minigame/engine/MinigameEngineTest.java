package br.com.muxe.minigame.engine;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.metrics.simple.SimpleMetricRegistry;
import br.com.muxe.minigame.engine.mode.GameMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MinigameEngineTest {

    @Test
    void theMetricsFollowWhatTheGameReports() {
        SimpleMetricRegistry metrics = new SimpleMetricRegistry();
        MinigameEngine engine = new MinigameEngine("bedwars", List.of(new GameMode("solo", "Solo", 2, 8, 1)), metrics);

        assertThat(metrics.snapshot().gaugeTotal("muxe_matches_active")).isZero();

        AtomicInteger matches = new AtomicInteger(3);
        engine.track(matches::get, () -> 7);
        matches.incrementAndGet();

        assertThat(metrics.snapshot().gaugeTotal("muxe_matches_active")).isEqualTo(4);
        assertThat(metrics.snapshot().gaugeTotal("muxe_arenas_available")).isEqualTo(7);
        assertThat(engine.modes()).containsOnlyKeys("solo");
    }
}
