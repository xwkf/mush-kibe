package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class TntExplosionFinalKillTest {

    private final Vec victim = new Vec(6.5, 11, 17.5);

    @Test
    void eachTntIsThrownUpAndOutAtTheSameSpeed() {
        Vec launch = TntExplosionFinalKill.launch(73);

        assertThat(launch.y()).isEqualTo(0.6);
        assertThat(Math.hypot(launch.x(), launch.z())).isCloseTo(0.2, offset(1e-9));
    }

    @Test
    void threeTntsAreLitFiveTicksApartAndExplodeAtTheVictimOneByOne() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();
        new TntExplosionFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(5));

        for (int tick = 0; tick <= 10; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("PrimedTnt")).hasSize(3)
            .allSatisfy(tnt -> assertThat(tnt.position).isEqualTo(victim)).noneMatch(RecordingStage.FakeActor::removed);
        assertThat(stage.sounds).filteredOn("creeper.primed"::equals).hasSize(3);

        for (int tick = 11; tick <= 19; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("PrimedTnt")).filteredOn(RecordingStage.FakeActor::removed).hasSize(1);
        assertThat(stage.particles).containsEntry("EXPLOSION_HUGE", 1);

        for (int tick = 20; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        assertThat(timeline.length()).isEqualTo(29);
        assertThat(stage.particles).containsEntry("EXPLOSION_HUGE", 3);
        assertThat(stage.sounds).filteredOn("random.explode"::equals).hasSize(3);
        assertThat(stage.alive()).isZero();
    }
}
