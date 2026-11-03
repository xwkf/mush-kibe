package br.com.muxe.minigame.bedwars.cosmetic.aura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.junit.jupiter.api.Test;

class AuraPatternTest {

    private static final double EPSILON = 1.0E-9;

    @Test
    void theFirstHelixPointIsOneBlockBehindTheCentreAndHalfwayUpTheFirstTurn() {
        Vec point = AuraPattern.helix(0);

        assertThat(point.x()).isEqualTo(0.0, within(EPSILON));
        assertThat(point.y()).isEqualTo(0.6, within(EPSILON));
        assertThat(point.z()).isEqualTo(-1.0, within(EPSILON));
    }

    @Test
    void theAngleGrowsEighteenDegreesPerTickSoAQuarterTurnTakesFiveTicks() {
        Vec quarter = AuraPattern.helix(5);
        Vec half = AuraPattern.helix(10);

        assertThat(quarter.x()).isEqualTo(1.0, within(EPSILON));
        assertThat(quarter.z()).isEqualTo(0.0, within(EPSILON));
        assertThat(half.x()).isEqualTo(0.0, within(EPSILON));
        assertThat(half.z()).isEqualTo(1.0, within(EPSILON));
    }

    @Test
    void theRadiusIsOneBlockOnEveryTick() {
        for (long counter = 0; counter < 200; counter++) {
            Vec point = AuraPattern.helix(counter);

            assertThat(Math.hypot(point.x(), point.z())).as("counter " + counter).isEqualTo(1.0, within(EPSILON));
        }
    }

    @Test
    void theHeightClimbsOneThirtiethPerTickFromTheBaseToAboutTwoBlocksAndStartsOver() {
        assertThat(AuraPattern.helix(45).y()).isEqualTo(0.1, within(EPSILON));
        assertThat(AuraPattern.helix(46).y()).isEqualTo(0.1 + 1.0 / 30, within(EPSILON));
        assertThat(AuraPattern.helix(44).y()).isEqualTo(0.1 + 59.0 / 30, within(EPSILON));
        assertThat(AuraPattern.helix(105).y()).isEqualTo(AuraPattern.helix(45).y(), within(EPSILON));
    }

    @Test
    void theHelixMakesThreeTurnsBeforeItWrapsAndRepeatsExactly() {
        for (long counter = 0; counter < 60; counter++) {
            Vec now = AuraPattern.helix(counter);
            Vec later = AuraPattern.helix(counter + 60);

            assertThat(later.x()).isEqualTo(now.x(), within(EPSILON));
            assertThat(later.y()).isEqualTo(now.y(), within(EPSILON));
            assertThat(later.z()).isEqualTo(now.z(), within(EPSILON));
        }
        assertThat(AuraPattern.helix(20).x()).isEqualTo(AuraPattern.helix(0).x(), within(EPSILON));
        assertThat(AuraPattern.helix(20).y()).isNotEqualTo(AuraPattern.helix(0).y());
    }

    @Test
    void aHugeCounterStillGivesAPointOnTheHelix() {
        Vec point = AuraPattern.helix(Long.MAX_VALUE);

        assertThat(Math.hypot(point.x(), point.z())).isEqualTo(1.0, within(EPSILON));
        assertThat(point.y()).isBetween(0.1, 0.1 + 59.0 / 30);
    }

    @Test
    void theFeetParticleIsATenthOfABlockAboveThePlayer() {
        assertThat(AuraPattern.feet(new Vec(3.5, 64, -2.25))).isEqualTo(new Vec(3.5, 64.1, -2.25));
    }

    @Test
    void onlyHorizontalMovementCounts() {
        Vec before = new Vec(1, 64, 1);

        assertThat(AuraPattern.moved(before, new Vec(1, 64, 1))).isFalse();
        assertThat(AuraPattern.moved(before, new Vec(1, 70, 1))).isFalse();
        assertThat(AuraPattern.moved(before, new Vec(1.2, 64, 1))).isTrue();
        assertThat(AuraPattern.moved(before, new Vec(1, 64, 0.8))).isTrue();
    }
}
