package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LightningFinalKillTest {

    @Test
    void theBoltStrikesTheCornerOfTheBlockTheVictimDiedOn() {
        assertThat(LightningFinalKill.strike(new Vec(10.5, 27, -3.5))).isEqualTo(new Vec(10, 27, -4));
    }

    @Test
    void aBoltStrikesWithAnExplosionAndARingOfFlames() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new LightningFinalKill().script(Scene.at(new Vec(10.5, 27, -3.5), new Vec(0, 0, 1)), stage, timeline, new Random(3));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.sounds).containsExactly("lightning", "random.explode");
        assertThat(stage.pitches.getLast()).isBetween(0.5f, 0.7f);
        assertThat(stage.particles).containsOnlyKeys("FLAME").containsEntry("FLAME", LightningFinalKill.FLAMES);
        assertThat(stage.actors).isEmpty();
    }

    @Test
    void theFlamesSpreadFlatAndOutwards() {
        Random random = new Random(7);
        for (int flame = 0; flame < 100; flame++) {
            Vec direction = LightningFinalKill.FLAME_REACH.pick(random);

            assertThat(Math.hypot(direction.x(), direction.z())).isBetween(0.25, 0.5 + 1e-9);
            assertThat(direction.y()).isBetween(0.0, 0.05);
        }
        assertThat(LightningFinalKill.FLAME_HEIGHT).isCloseTo(0.5, offset(1e-9));
    }
}
