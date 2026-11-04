package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ExplodingSquidFinalKillTest {

    private final Vec victim = new Vec(18.5, 15, 8.5);

    @Test
    void theSquidRisesFasterEveryTickUpToNineBlocks() {
        assertThat(ExplodingSquidFinalKill.height(0)).isEqualTo(0.5);
        assertThat(ExplodingSquidFinalKill.height(1)).isCloseTo(0.78, offset(1e-9));
        assertThat(ExplodingSquidFinalKill.height(2) - ExplodingSquidFinalKill.height(1)).isCloseTo(0.31, offset(1e-9));
        assertThat(ExplodingSquidFinalKill.height(ExplodingSquidFinalKill.POP)).isCloseTo(9.34, offset(1e-9));
    }

    @Test
    void theSquidSplashesWithRisingNotesThenBurstsIntoInkSacs() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();
        new ExplodingSquidFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(6));

        for (int tick = 0; tick < ExplodingSquidFinalKill.POP; tick++) {
            timeline.run(tick);
        }
        RecordingStage.FakeActor squid = stage.actors.getFirst();
        assertThat(squid.type).isEqualTo("Squid");
        assertThat(squid.position.y()).isCloseTo(victim.y() + 9.34, offset(1e-9));
        assertThat(stage.sounds).hasSize(ExplodingSquidFinalKill.POP).containsOnly("note.pling");
        assertThat(stage.pitches.getFirst()).isEqualTo(1.05f);
        assertThat(stage.pitches.getLast()).isCloseTo(1.85f, offset(1e-6f));
        assertThat(stage.particles).containsEntry("DRIP_WATER", ExplodingSquidFinalKill.POP).containsEntry("WATER_SPLASH", ExplodingSquidFinalKill.POP);

        timeline.run(ExplodingSquidFinalKill.POP);
        assertThat(squid.removed()).isTrue();
        assertThat(stage.sounds.getLast()).isEqualTo("random.explode");
        assertThat(stage.particles).containsKey("SMOKE_LARGE");
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:INK_SACK")).hasSize(12)
            .allSatisfy(sac -> assertThat(sac.position.y()).isCloseTo(victim.y() + 9.34, offset(1e-9)));

        for (int tick = ExplodingSquidFinalKill.POP + 1; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        assertThat(timeline.length()).isEqualTo(75);
        assertThat(stage.alive()).isZero();
    }
}
