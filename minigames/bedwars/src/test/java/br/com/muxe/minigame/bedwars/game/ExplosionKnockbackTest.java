package br.com.muxe.minigame.bedwars.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.game.ExplosionKnockback.Source;
import org.junit.jupiter.api.Test;

class ExplosionKnockbackTest {

    @Test
    void aFireballLaunchesTheThrowerAwayAlongTheLineFromTheBlastToTheirFeet() {
        Vec push = ExplosionKnockback.of(Source.FIREBALL, new Vec(0, 64, 0), new Vec(0, 67, 4), true);

        assertThat(push.x()).isCloseTo(0, within(1e-9));
        assertThat(push.z()).isCloseTo(3.125 * 0.8, within(1e-9));
        assertThat(push.y()).isEqualTo(0.7);
    }

    @Test
    void anAirbornePlayerIsLiftedHarder() {
        assertThat(ExplosionKnockback.of(Source.FIREBALL, new Vec(0, 64, 0), new Vec(1, 64, 0), false).y()).isEqualTo(1.0);
    }

    @Test
    void tntPushesOnlyAlongTheGroundWithItsOwnStrength() {
        Vec push = ExplosionKnockback.of(Source.TNT, new Vec(10, 64, 10), new Vec(6, 70, 13), true);

        assertThat(Math.hypot(push.x(), push.z())).isCloseTo(1.875, within(1e-9));
        assertThat(push.x()).isCloseTo(-1.5, within(1e-9));
        assertThat(push.z()).isCloseTo(1.125, within(1e-9));
        assertThat(push.y()).isEqualTo(0.7);
    }

    @Test
    void standingRightOnTheBlastOnlyLifts() {
        assertThat(ExplosionKnockback.of(Source.FIREBALL, new Vec(0, 64, 0), new Vec(0, 64, 0), true)).isEqualTo(new Vec(0, 0.7, 0));
    }

    @Test
    void tntWaitsThreeSecondsAndHitsHarderThanAFireball() {
        assertThat(ExplosionKnockback.TNT_FUSE_TICKS).isEqualTo(60);
        assertThat(ExplosionKnockback.TNT_YIELD).isEqualTo(3.0f);
        assertThat(ExplosionKnockback.FIREBALL_YIELD).isEqualTo(2.0f);
    }
}
