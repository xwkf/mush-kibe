package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class FireworksFinalKillTest {

    @Test
    void oneRocketBurstsAboveTheVictimAndIsGone() {
        Vec victim = new Vec(10.5, 27, -3.5);
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new FireworksFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(1));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).singleElement().satisfies(rocket -> {
            assertThat(rocket.type).isEqualTo("Firework");
            assertThat(rocket.position).isEqualTo(victim.add(0, 1, 0));
            assertThat(rocket.removed()).isTrue();
        });
        assertThat(stage.sounds).containsExactly("fireworks.launch");
    }
}
