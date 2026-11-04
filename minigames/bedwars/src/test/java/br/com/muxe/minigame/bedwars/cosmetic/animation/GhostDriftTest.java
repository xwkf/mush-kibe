package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GhostDriftTest {

    private final Vec home = new Vec(8.5, 33, -5.5);

    @Test
    void aGhostSpeedsUpGentlyAndNeverFliesFasterThanItsTopSpeed() {
        GhostDrift drift = new GhostDrift(home, home.add(0, 0.7, 0), 0, new Random(11));
        double previous = 0;
        for (int tick = 0; tick < 200; tick++) {
            drift.step();
            double speed = drift.velocity().length();

            assertThat(speed).isLessThanOrEqualTo(GhostDrift.TOP_SPEED + 1e-9);
            assertThat(speed - previous).isLessThanOrEqualTo(GhostDrift.STEERING + 1e-9);
            previous = speed;
        }
    }

    @Test
    void aGhostWandersAroundTheVictimWithoutLeavingOrSinking() {
        for (long seed = 0; seed < 20; seed++) {
            GhostDrift drift = new GhostDrift(home, home.add(0, 0.7, 0), 0, new Random(seed));
            for (int tick = 0; tick < GhostsFinalKill.LIFE; tick++) {
                drift.step();
                Vec at = drift.position();

                assertThat(Math.hypot(at.x() - home.x(), at.z() - home.z())).isLessThan(GhostDrift.REACH + 1);
                assertThat(at.y() - home.y()).isBetween(0.0, GhostDrift.MAX_RISE + 0.5);
            }
        }
    }

    @Test
    void aGhostTurnsToFaceWhereItFliesALittleAtATime() {
        GhostDrift drift = new GhostDrift(home, home.add(0, 0.7, 0), 180, new Random(3));
        float before = drift.yaw();
        for (int tick = 0; tick < 30; tick++) {
            drift.step();
            assertThat(Math.abs(Flight.wrap(drift.yaw() - before))).isLessThanOrEqualTo(GhostDrift.TURN + 1e-3);
            before = drift.yaw();
        }
        Vec velocity = drift.velocity();
        assertThat(Math.abs(Flight.wrap(drift.yaw() - Vec.ZERO.yawTowards(velocity)))).isLessThan(GhostDrift.TURN * 2);
    }
}
