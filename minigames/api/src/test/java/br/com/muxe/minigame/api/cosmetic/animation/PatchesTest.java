package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PatchesTest {

    private static final double PRECISION = 1e-9;

    @Test
    void theDepthIsHowFarInsideTheDeepestBallABlockCentreIs() {
        Patches patches = new Patches(List.of(new Patches.Ball(0.5, 10.5, 0.5, 4), new Patches.Ball(3.5, 10.5, 0.5, 2)));

        assertThat(patches.depth(0, 10, 0)).isCloseTo(4, within(PRECISION));
        assertThat(patches.depth(3, 10, 0)).isCloseTo(2, within(PRECISION));
        assertThat(patches.depth(0, 14, 0)).isCloseTo(0, within(PRECISION));
        assertThat(patches.depth(0, 20, 0)).isNegative();
    }

    @Test
    void ballsAcrossChunkBordersAreFoundFromEitherSide() {
        Patches patches = new Patches(List.of(new Patches.Ball(15.5, 10.5, 15.5, 3)));

        assertThat(patches.depth(16, 10, 16)).isPositive();
        assertThat(patches.depth(14, 10, 14)).isPositive();
        assertThat(patches.depth(40, 10, 40)).isEqualTo(Double.NEGATIVE_INFINITY);
    }

    @Test
    void randomBallsFillAboutTheShareTheDensityAsksFor() {
        int[] box = {0, 0, 199, 199};
        Patches patches = new Patches(box, 0, 127, InfernoPalette.LAVA_PER_COLUMN, new Random(4));
        int inside = 0;
        int total = 0;
        for (int x = 20; x < 180; x += 3) {
            for (int z = 20; z < 180; z += 3) {
                for (int y = 20; y < 108; y += 3) {
                    total++;
                    if (patches.depth(x, y, z) > 0) {
                        inside++;
                    }
                }
            }
        }

        assertThat((double) inside / total).isBetween(0.10, 0.19);
    }
}
