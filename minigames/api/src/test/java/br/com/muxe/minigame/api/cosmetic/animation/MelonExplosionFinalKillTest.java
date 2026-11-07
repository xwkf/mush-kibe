package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MelonExplosionFinalKillTest {

    private final Vec victim = new Vec(8.5, 33, -5.5);

    @Test
    void eightMelonPiecesFlyOutEvenlyAroundTheVictim() {
        for (int piece = 0; piece < MelonExplosionFinalKill.PIECES; piece++) {
            Vec launch = MelonExplosionFinalKill.launch(piece, 0.18);

            assertThat(launch.y()).isEqualTo(0.4);
            assertThat(Math.hypot(launch.x(), launch.z())).isCloseTo(0.18, offset(1e-9));
        }
        assertThat(MelonExplosionFinalKill.launch(0, 0.18).x()).isCloseTo(0.18, offset(1e-9));
        assertThat(MelonExplosionFinalKill.launch(2, 0.18).z()).isCloseTo(0.18, offset(1e-9));
        assertThat(MelonExplosionFinalKill.launch(5, 0.18).x()).isCloseTo(MelonExplosionFinalKill.launch(5, 0.18).z(), offset(1e-9)).isNegative();
    }

    @Test
    void wholeMelonsAndSlicesAlternateAndBlowUpWithAPlop() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new MelonExplosionFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(4));
        for (int tick = 0; tick < MelonExplosionFinalKill.END; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:MELON_BLOCK")).hasSize(4).noneMatch(RecordingStage.FakeActor::removed);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:MELON")).hasSize(4);
        assertThat(stage.sounds).containsExactlyInAnyOrder("random.explode", "mob.chicken.plop", "fireworks.launch");
        assertThat(stage.pitches.get(stage.sounds.indexOf("random.explode"))).isEqualTo(1.2f);
        timeline.run(MelonExplosionFinalKill.END);
        assertThat(stage.alive()).isZero();
    }
}
