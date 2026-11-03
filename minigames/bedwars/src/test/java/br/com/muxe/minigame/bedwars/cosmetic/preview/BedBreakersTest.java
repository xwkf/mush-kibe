package br.com.muxe.minigame.bedwars.cosmetic.preview;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class BedBreakersTest {

    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();
    private final BedBreakers breakers = new BedBreakers();

    @Test
    void theFirstBreakerTakesTheBedOutOfTheWorld() {
        assertThat(breakers.broke(first)).isTrue();
        assertThat(breakers.broken()).isTrue();
        assertThat(breakers.broke(second)).isFalse();
        assertThat(breakers.broke(first)).isFalse();
    }

    @Test
    void theBedComesBackOnlyWhenTheLastBreakerLeaves() {
        breakers.broke(first);
        breakers.broke(second);

        assertThat(breakers.left(first)).isFalse();
        assertThat(breakers.broken()).isTrue();
        assertThat(breakers.left(second)).isTrue();
        assertThat(breakers.broken()).isFalse();
    }

    @Test
    void someoneWhoNeverBrokeTheBedLeavingChangesNothing() {
        breakers.broke(first);

        assertThat(breakers.left(second)).isFalse();
        assertThat(breakers.broken()).isTrue();
        assertThat(breakers.breaker(first)).isTrue();
        assertThat(breakers.breaker(second)).isFalse();
    }
}
