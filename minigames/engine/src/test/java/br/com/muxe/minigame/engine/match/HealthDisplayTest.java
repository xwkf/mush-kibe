package br.com.muxe.minigame.engine.match;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class HealthDisplayTest {

    @Test
    void healthIsShownAsWholePointsRoundedUp() {
        assertThat(HealthDisplay.points(20.0)).isEqualTo(20);
        assertThat(HealthDisplay.points(13.5)).isEqualTo(14);
        assertThat(HealthDisplay.points(0.4)).isEqualTo(1);
    }

    @Test
    void aPlayerWhoStopsBeingAliveIsRemovedFromTheTags() {
        assertThat(HealthDisplay.mustRebuild(Set.of("tadeu", "lia"), Set.of("tadeu", "lia", "rui"))).isFalse();
        assertThat(HealthDisplay.mustRebuild(Set.of("tadeu", "lia"), Set.of("tadeu"))).isTrue();
        assertThat(HealthDisplay.mustRebuild(Set.of(), Set.of("tadeu"))).isFalse();
    }
}
