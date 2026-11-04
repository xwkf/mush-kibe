package br.com.muxe.afk.session;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AfkSessionsTest {

    private static final Duration GRACE = Duration.ofSeconds(1);
    private static final Duration RETRY_AFTER = Duration.ofSeconds(5);

    private final MutableClock clock = MutableClock.startingAt("2026-11-03T20:00:00Z");
    private final AfkSessions sessions = new AfkSessions(clock, GRACE, RETRY_AFTER);
    private final UUID player = UUID.randomUUID();

    @Test
    void movingRightAfterArrivingDoesNotSendThePlayerBack() {
        sessions.arrive(player);
        clock.advance(GRACE.minusMillis(1));

        assertThat(sessions.startReturn(player)).isFalse();
    }

    @Test
    void movingAfterTheGraceSendsThePlayerBackOnce() {
        sessions.arrive(player);
        clock.advance(GRACE);

        assertThat(sessions.startReturn(player)).isTrue();
        assertThat(sessions.startReturn(player)).isFalse();
    }

    @Test
    void aReturnThatFailedCanBeTriedAgainRightAway() {
        sessions.arrive(player);
        clock.advance(GRACE);
        sessions.startReturn(player);

        sessions.returnFailed(player);

        assertThat(sessions.startReturn(player)).isTrue();
    }

    @Test
    void aReturnThatNeverHappenedCanBeTriedAgainLater() {
        sessions.arrive(player);
        clock.advance(GRACE);
        sessions.startReturn(player);

        clock.advance(RETRY_AFTER.minusMillis(1));
        assertThat(sessions.startReturn(player)).isFalse();

        clock.advance(Duration.ofMillis(1));
        assertThat(sessions.startReturn(player)).isTrue();
    }

    @Test
    void playersWhoLeftOrNeverArrivedAreNotSent() {
        sessions.arrive(player);
        sessions.leave(player);
        clock.advance(GRACE);

        assertThat(sessions.startReturn(player)).isFalse();
        assertThat(sessions.startReturn(UUID.randomUUID())).isFalse();
    }
}
