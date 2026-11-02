package br.com.muxe.minigame.engine.match;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class HealthBelowNameTest {

    @Test
    void healthIsShownAsWholePointsRoundedUp() {
        assertThat(HealthBelowName.points(20.0)).isEqualTo(20);
        assertThat(HealthBelowName.points(13.5)).isEqualTo(14);
        assertThat(HealthBelowName.points(0.4)).isEqualTo(1);
    }

    @Test
    void aPlayerWhoStopsBeingAliveIsRemovedFromTheTags() {
        assertThat(HealthBelowName.mustRebuild(Set.of("tadeu", "lia"), Set.of("tadeu", "lia", "rui"))).isFalse();
        assertThat(HealthBelowName.mustRebuild(Set.of("tadeu", "lia"), Set.of("tadeu"))).isTrue();
        assertThat(HealthBelowName.mustRebuild(Set.of(), Set.of("tadeu"))).isFalse();
    }
}
