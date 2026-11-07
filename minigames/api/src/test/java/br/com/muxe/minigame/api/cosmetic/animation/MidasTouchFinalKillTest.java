package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MidasTouchFinalKillTest {

    private final Vec victim = new Vec(10.5, 27, -3.5);

    @Test
    void fifteenSunflowersPopOutWithARisingPitch() {
        assertThat(MidasTouchFinalKill.pitch(0)).isEqualTo(1);
        assertThat(MidasTouchFinalKill.pitch(14)).isCloseTo(1.933f, offset(1e-3f));
        assertThat(MidasTouchFinalKill.flowerTick(0)).isEqualTo(MidasTouchFinalKill.FIRST_FLOWER);
        assertThat(MidasTouchFinalKill.flowerTick(1)).isEqualTo(MidasTouchFinalKill.FIRST_FLOWER);
        assertThat(MidasTouchFinalKill.flowerTick(14)).isEqualTo(16);
    }

    @Test
    void aGoldBlockSmashesTheVictimAndLeavesSunflowersUntilTheEnd() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new MidasTouchFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(3));

        for (int tick = 0; tick <= MidasTouchFinalKill.IMPACT; tick++) {
            timeline.run(tick);
        }
        RecordingStage.FakeActor gold = stage.actors.getFirst();

        assertThat(gold.type).isEqualTo("FallingBlock:GOLD_BLOCK:0");
        assertThat(gold.position).isEqualTo(victim);
        assertThat(stage.sounds).containsExactly("dig.stone", "random.break");
        assertThat(stage.particles).containsEntry("BLOCK_DUST", MidasTouchFinalKill.DUST);

        for (int tick = MidasTouchFinalKill.IMPACT + 1; tick < MidasTouchFinalKill.END; tick++) {
            timeline.run(tick);
        }

        assertThat(gold.removed()).isTrue();
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:DOUBLE_PLANT")).hasSize(MidasTouchFinalKill.FLOWERS)
            .noneMatch(RecordingStage.FakeActor::removed);
        assertThat(stage.sounds).filteredOn("random.pop"::equals).hasSize(MidasTouchFinalKill.FLOWERS);

        timeline.run(MidasTouchFinalKill.END);

        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(timeline.length()).isEqualTo(MidasTouchFinalKill.END);
    }
}
