package br.com.muxe.core.common.match;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.InMemoryExpiringStore;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MatchReturnsTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-03T20:00:00Z"));
    private final MatchReturns returns = new MatchReturns(new InMemoryExpiringStore(clock));
    private final UUID player = UUID.randomUUID();

    @Test
    void aPlayerWhoLeftAMatchKnowsWhichServerToReturnTo() {
        returns.remember(player, "bedwars-1", Duration.ofSeconds(180));

        assertThat(returns.serverOf(player)).contains("bedwars-1");
        assertThat(returns.serverOf(UUID.randomUUID())).isEmpty();
    }

    @Test
    void theReturnExpiresWithTheRejoinTime() {
        returns.remember(player, "bedwars-1", Duration.ofSeconds(180));
        clock.advance(Duration.ofSeconds(181));

        assertThat(returns.serverOf(player)).isEmpty();
    }

    @Test
    void aForgottenReturnLeadsNowhere() {
        returns.remember(player, "bedwars-1", Duration.ofSeconds(180));
        returns.forget(player);

        assertThat(returns.serverOf(player)).isEmpty();
    }
}
