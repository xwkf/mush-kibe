package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class EnchantedFinalKillTest {

    private static final Vec VICTIM = new Vec(18.5, 15, 8.5);

    @Test
    void aMushroomHopsOutOfTheGroundSpinningAndSettlesBack() {
        assertThat(Mushroom.lift(0)).isZero();
        assertThat(Mushroom.lift(4)).isGreaterThan(0.85);
        assertThat(Mushroom.lift(Mushroom.HOP.length)).isZero();
        assertThat(Mushroom.drift(Mushroom.HOP.length)).isCloseTo(10 / 32.0, offset(1e-9));
        assertThat(Mushroom.turned(Mushroom.SPINNING)).isBetween(340.0, 400.0);
    }

    @Test
    void leavesBurstUpAndPuffWhenTheyLandWhileNineMushroomsSproutAround() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 15);
        Timeline timeline = new Timeline();
        new EnchantedFinalKill().script(Scene.at(VICTIM, new Vec(-1, 0, 0)), stage, timeline, new Random(8));

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        int leaves = EnchantedFinalKill.LEAF_HEIGHTS.length * EnchantedFinalKill.LEAVES_PER_HEIGHT;
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:LEAVES"))).hasSize(leaves);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand"))).hasSize(EnchantedFinalKill.MUSHROOMS)
            .allSatisfy(stand -> assertThat(Math.hypot(stand.position.x() - VICTIM.x(), stand.position.z() - VICTIM.z())).isBetween(0.6, 1.25));
        assertThat(stage.particles.get("CLOUD")).isEqualTo(leaves + EnchantedFinalKill.MUSHROOMS);
        assertThat(stage.sounds.stream().filter("random.pop"::equals)).hasSize(EnchantedFinalKill.MUSHROOMS);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void leavesAreThrownUpWithASmallOutwardPush() {
        Vec north = EnchantedFinalKill.throwing(270, new Random(1));

        assertThat(north.z()).isNegative();
        assertThat(north.x()).isCloseTo(0, offset(1e-9));
        assertThat(north.y()).isBetween(EnchantedFinalKill.MIN_LEAF_LIFT, EnchantedFinalKill.MAX_LEAF_LIFT);
    }
}
