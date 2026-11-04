package br.com.muxe.core.common.afk;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.InMemoryExpiringStore;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AfkReturnsTest {

    private final MutableClock clock = MutableClock.startingAt("2026-11-03T20:00:00Z");
    private final AfkReturns returns = new AfkReturns(new InMemoryExpiringStore(clock));
    private final UUID player = UUID.randomUUID();

    @Test
    void anAfkPlayerKnowsWhichServerTheyCameFrom() {
        returns.remember(player, "bedwars-lobby-1", Duration.ofHours(12));

        assertThat(returns.originOf(player)).contains("bedwars-lobby-1");
        assertThat(returns.originOf(UUID.randomUUID())).isEmpty();
    }

    @Test
    void theOriginExpires() {
        returns.remember(player, "lobby-1", Duration.ofHours(12));
        clock.advance(Duration.ofHours(12).plusSeconds(1));

        assertThat(returns.originOf(player)).isEmpty();
    }

    @Test
    void aForgottenOriginLeadsNowhere() {
        returns.remember(player, "lobby-1", Duration.ofHours(12));
        returns.forget(player);

        assertThat(returns.originOf(player)).isEmpty();
    }
}
