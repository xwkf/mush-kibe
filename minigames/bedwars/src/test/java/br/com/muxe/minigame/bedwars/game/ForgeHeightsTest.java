package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class ForgeHeightsTest {

    @Test
    void theSpinningBlockSitsBelowTheThreeHologramLines() {
        assertThat(ForgeHeights.DROP_ABOVE_POINT + ForgeHeights.BLOCK_ABOVE_DROP).isCloseTo(1.65, within(1e-9));
        assertThat(ForgeHeights.HOLOGRAM_TOP_ABOVE_DROP).isCloseTo(0.5 + 0.6 + 2.38125, within(1e-9));
    }

    @Test
    void theBlockTurnsOncePerThreeSecondsAndBobsSlightly() {
        assertThat(ForgeHeights.BLOCK_MOTION.yaw(0f, 60)).isZero();
        assertThat(ForgeHeights.BLOCK_MOTION.yaw(0f, 15)).isEqualTo(90f);
        assertThat(Math.abs(ForgeHeights.BLOCK_MOTION.lift(11))).isCloseTo(0.08, within(0.001));
    }
}
