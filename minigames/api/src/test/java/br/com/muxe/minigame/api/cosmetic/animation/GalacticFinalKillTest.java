package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GalacticFinalKillTest {

    @Test
    void twelveNetherStarsFountainOutWithARisingChime() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new GalacticFinalKill().script(Scene.at(new Vec(10.5, 27, -3.5), new Vec(0, 0, 1)), stage, timeline, new Random(4));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:NETHER_STAR")).hasSize(GalacticFinalKill.STARS);
        assertThat(stage.sounds).filteredOn("note.pling"::equals).hasSize(GalacticFinalKill.STARS);
        assertThat(stage.sounds).endsWith("random.levelup");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void eachStarIsThrownUpAndATwelfthOfATurnFurther() {
        Vec first = GalacticFinalKill.throwing(0, 0);
        Vec fourth = GalacticFinalKill.throwing(0, 3);

        assertThat(first.y()).isEqualTo(0.5);
        assertThat(first.x()).isCloseTo(0.18, offset(1e-9));
        assertThat(fourth.z()).isCloseTo(0.18, offset(1e-9));
        assertThat(GalacticFinalKill.pitch(GalacticFinalKill.STARS - 1)).isCloseTo(1.93f, offset(0.01f));
    }
}
