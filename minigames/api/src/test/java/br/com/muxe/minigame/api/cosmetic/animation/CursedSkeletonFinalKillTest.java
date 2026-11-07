package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class CursedSkeletonFinalKillTest {

    private static final Vec VICTIM = new Vec(8.5, 33, -5.5);

    @Test
    void theCursedCircleIsAThickRingTwoBlocksAround() {
        List<Vec> points = CursedSkeletonFinalKill.ringPoints(VICTIM);

        assertThat(points).hasSize(CursedSkeletonFinalKill.RING_POINTS * CursedSkeletonFinalKill.RING_LAYERS);
        assertThat(points).allSatisfy(point -> {
            assertThat(Math.hypot(point.x() - VICTIM.x(), point.z() - VICTIM.z())).isBetween(2.0 - 1e-9, 2.2 + 1e-9);
            assertThat(point.y()).isCloseTo(VICTIM.y() + 0.1, offset(1e-9));
        });
    }

    @Test
    void bonesAndSkullsAreThrownAroundUnderAStrikeAndVanishTogether() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 33);
        Timeline timeline = new Timeline();
        new CursedSkeletonFinalKill().script(Scene.at(VICTIM, new Vec(0, 0, 1)), stage, timeline, new Random(4));

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:BONE"))).hasSize(CursedSkeletonFinalKill.BONES);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:SKULL_ITEM"))).hasSize(CursedSkeletonFinalKill.SKULLS + 1);
        assertThat(stage.bolts).containsExactly(VICTIM);
        assertThat(stage.sounds).contains("mob.blaze.death", "ambient.weather.thunder", "mob.skeleton.say", "mob.skeleton.death");
        assertThat(stage.sounds.stream().filter("mob.skeleton.step"::equals)).hasSize(CursedSkeletonFinalKill.BONES);
        assertThat(stage.particles.get("REDSTONE")).isEqualTo(10 * CursedSkeletonFinalKill.RING_POINTS * CursedSkeletonFinalKill.RING_LAYERS);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void bonesFlyOutwardsAndUp() {
        Random random = new Random(3);
        Vec east = CursedSkeletonFinalKill.bone(0, random);

        assertThat(east.x()).isBetween(CursedSkeletonFinalKill.MIN_BONE_PUSH, CursedSkeletonFinalKill.MAX_BONE_PUSH);
        assertThat(east.z()).isCloseTo(0, offset(1e-9));
        assertThat(east.y()).isBetween(CursedSkeletonFinalKill.MIN_BONE_LIFT, CursedSkeletonFinalKill.MAX_BONE_LIFT);
    }
}
