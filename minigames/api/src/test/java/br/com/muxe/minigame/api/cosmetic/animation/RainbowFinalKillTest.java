package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RainbowFinalKillTest {

    private final Vec victim = new Vec(8.5, 33, -5.5);

    @Test
    void eachWaveThrowsTheRainbowTwiceAllAround() {
        assertThat(RainbowFinalKill.color(0)).isEqualTo(14);
        assertThat(RainbowFinalKill.color(6)).isEqualTo(10);
        assertThat(RainbowFinalKill.color(7)).isEqualTo(14);
        assertThat(RainbowFinalKill.direction(0, 7)).isCloseTo(180, offset(1e-9));
        assertThat(RainbowFinalKill.direction(10, 1) - RainbowFinalKill.direction(10, 0)).isCloseTo(360.0 / 14, offset(1e-9));
    }

    @Test
    void theSparklesCycleThroughTheHues() {
        assertThat(RainbowFinalKill.hue(0)).isZero();
        assertThat(RainbowFinalKill.hue(1)).isEqualTo(15);
        assertThat(RainbowFinalKill.hue(24)).isZero();
    }

    @Test
    void twoWavesOfWoolAreThrownAndClearedAtTheEnd() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 33);
        Timeline timeline = new Timeline();

        new RainbowFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(5));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).hasSize(28).allSatisfy(actor -> {
            assertThat(actor.type).isEqualTo("Item:WOOL");
            assertThat(actor.removed()).isTrue();
        });
        assertThat(stage.sounds).containsExactly("random.levelup", "random.levelup", "random.orb");
        assertThat(stage.particles).containsEntry("REDSTONE", (RainbowFinalKill.SPARKLE_END + 1) * RainbowFinalKill.SPARKLES);
    }
}
