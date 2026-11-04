package br.com.muxe.core.common.activity;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdleTrackerTest {

    private final MutableClock clock = MutableClock.startingAt("2026-11-03T20:00:00Z");
    private final IdleTracker tracker = new IdleTracker(clock, Duration.ofMinutes(5));
    private final UUID player = UUID.randomUUID();

    @Test
    void aPlayerBecomesIdleAfterStandingStillForTheWholeTime() {
        tracker.active(player);

        clock.advance(Duration.ofMinutes(5).minusSeconds(1));
        assertThat(tracker.isIdle(player)).isFalse();

        clock.advance(Duration.ofSeconds(1));
        assertThat(tracker.isIdle(player)).isTrue();
    }

    @Test
    void anyActivityStartsTheCountAgain() {
        tracker.active(player);
        clock.advance(Duration.ofMinutes(4));

        tracker.active(player);
        clock.advance(Duration.ofMinutes(4));

        assertThat(tracker.isIdle(player)).isFalse();
    }

    @Test
    void anUnknownOrForgottenPlayerIsNeverIdle() {
        tracker.active(player);
        tracker.forget(player);
        clock.advance(Duration.ofHours(1));

        assertThat(tracker.isIdle(player)).isFalse();
        assertThat(tracker.isIdle(UUID.randomUUID())).isFalse();
    }
}
