package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class FlowerGardenFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void thirteenTulipsAreThrownEvenlyAllAroundAndSlightlyUp() {
        Vec first = FlowerGardenFinalKill.launch(0, 0.3);
        Vec second = FlowerGardenFinalKill.launch(1, 0.3);

        assertThat(first.x()).isCloseTo(0.3, offset(1e-9));
        assertThat(first.z()).isCloseTo(0, offset(1e-9));
        assertThat(first.y()).isCloseTo(0.1, offset(1e-9));
        assertThat(Math.toDegrees(Math.atan2(second.z(), second.x()))).isCloseTo(360.0 / 13, offset(1e-9));
    }

    @Test
    void aPinkFireworkPopsThenTheTulipsBloomAndFadeAway() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new FlowerGardenFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(2));

        for (int tick = 0; tick < FlowerGardenFinalKill.END; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors.getFirst().type).isEqualTo("Firework");
        assertThat(stage.actors.getFirst().position.y()).isEqualTo(28);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Item:RED_ROSE"))).hasSize(13);
        assertThat(stage.sounds).containsExactly("fireworks.launch");
        assertThat(stage.alive()).isEqualTo(13);

        timeline.run(FlowerGardenFinalKill.END);

        assertThat(stage.alive()).isZero();
    }
}
