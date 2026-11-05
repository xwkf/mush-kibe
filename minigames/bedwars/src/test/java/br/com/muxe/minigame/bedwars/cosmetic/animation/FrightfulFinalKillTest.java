package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class FrightfulFinalKillTest {

    private final Vec victim = new Vec(8.5, 27, -1.5);

    @Test
    void theGhostsRiseOutOfTheGroundAndSlowDown() {
        assertThat(FrightfulFinalKill.rise(0)).isZero();
        assertThat(FrightfulFinalKill.rise(1)).isCloseTo(0.46, offset(0.02));
        assertThat(FrightfulFinalKill.rise(FrightfulFinalKill.RISE_STEPS)).isEqualTo(2);
        assertThat(FrightfulFinalKill.bigGhost(victim, 0, 0).y() - victim.y()).isEqualTo(-2.0625);
        assertThat(FrightfulFinalKill.littleGhost(victim, 0, 1, 0).y() - victim.y()).isEqualTo(-1.40625);
    }

    @Test
    void theBigGhostSpiralsOutAndBackWhileClimbingThreeBlocks() {
        Vec widest = FrightfulFinalKill.bigGhost(victim, 0, FrightfulFinalKill.SPIRAL_START + FrightfulFinalKill.SPIRAL_STEPS / 2);
        Vec top = FrightfulFinalKill.bigGhost(victim, 0, FrightfulFinalKill.LAST_STEP);

        assertThat(Math.hypot(widest.x() - victim.x(), widest.z() - victim.z())).isCloseTo(1.38, offset(1e-6));
        assertThat(Math.hypot(top.x() - victim.x(), top.z() - victim.z())).isCloseTo(0, offset(1e-6));
        assertThat(top.y() - victim.y()).isCloseTo(2.9375, offset(1e-9));
    }

    @Test
    void theLittleGhostsCircleTogetherWidenThenCloseIn() {
        assertThat(FrightfulFinalKill.orbit(FrightfulFinalKill.ORBIT_START)).isZero();
        assertThat(FrightfulFinalKill.orbit(FrightfulFinalKill.ORBIT_START + FrightfulFinalKill.WIDEN_STEPS)).isCloseTo(1.97, offset(1e-9));
        assertThat(FrightfulFinalKill.orbit(FrightfulFinalKill.LAST_STEP)).isCloseTo(0.9, offset(1e-9));
        assertThat(FrightfulFinalKill.littleAngle(0, 1, 10) - FrightfulFinalKill.littleAngle(0, 0, 10)).isCloseTo(120, offset(1e-9));
        assertThat(FrightfulFinalKill.littleHeight(FrightfulFinalKill.CLIMB_END)).isCloseTo(3.8, offset(1e-9));
        assertThat(FrightfulFinalKill.littleHeight(FrightfulFinalKill.LAST_STEP)).isCloseTo(3.45, offset(1e-9));
    }

    @Test
    void theBigGhostHeadRocksSideToSide() {
        double highest = 0;
        double lowest = 0;
        for (int offset = 0; offset <= FrightfulFinalKill.WOBBLE_END - FrightfulFinalKill.WOBBLE_START; offset++) {
            highest = Math.max(highest, FrightfulFinalKill.wobble(offset));
            lowest = Math.min(lowest, FrightfulFinalKill.wobble(offset));
        }
        assertThat(highest).isCloseTo(12, offset(0.2));
        assertThat(lowest).isCloseTo(-12, offset(0.2));
    }

    @Test
    void theGhostsBurstIntoCandyThatIsClearedAtTheEnd() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new FrightfulFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(4));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")))
            .hasSize(4)
            .allSatisfy(ghost -> assertThat(ghost.skulls).containsValue(FrightfulFinalKill.GHOST));
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Item:"))).hasSize(8);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.sounds).contains("mob.ghast.moan", "note.pling", "mob.ghast.scream", "fireworks.largeBlast", "fireworks.twinkle_far");
        assertThat(stage.particles).containsKeys("REDSTONE", "FIREWORKS_SPARK", "SPELL_WITCH", "EXPLOSION_NORMAL");
    }
}
