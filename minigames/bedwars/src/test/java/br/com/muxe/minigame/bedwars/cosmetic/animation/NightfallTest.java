package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NightfallTest {

    @Test
    void theSkyGoesFromMiddayToMidnightInThirtyTicksLikeTheRecording() {
        assertThat(Nightfall.dayTime(0)).isEqualTo(6000);
        assertThat(Nightfall.dayTime(1)).isEqualTo(6033);
        assertThat(Nightfall.dayTime(2)).isEqualTo(6131);
        assertThat(Nightfall.dayTime(10)).isEqualTo(9000);
        assertThat(Nightfall.dayTime(15)).isEqualTo(12000);
        assertThat(Nightfall.dayTime(29)).isEqualTo(17967);
        assertThat(Nightfall.dayTime(30)).isEqualTo(18000);
        assertThat(Nightfall.dayTime(45)).isEqualTo(18000);
    }
}
