package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DrownFinalKillTest {

    private final Vec victim = new Vec(0.5, 24, 2.5);

    @Test
    void theWaveIsSevenRowsOfTwentyOnePixels() {
        assertThat(DrownFinalKill.WAVE).hasSize(7).allSatisfy(row -> assertThat(row).hasSize(21));
        long pixels = DrownFinalKill.WAVE.stream().flatMapToInt(String::chars).filter(pixel -> pixel != '.').count();
        assertThat(pixels).isEqualTo(108);
    }

    @Test
    void theWaveRisesTwoRowsATime() {
        assertThat(DrownFinalKill.rows(0)).isEqualTo(2);
        assertThat(DrownFinalKill.rows(1)).isEqualTo(4);
        assertThat(DrownFinalKill.rows(2)).isEqualTo(6);
        assertThat(DrownFinalKill.rows(3)).isEqualTo(7);
        assertThat(DrownFinalKill.rows(30)).isEqualTo(7);
    }

    @Test
    void theWaveStandsAcrossTheHitterSightAndSwaysAtTheTop() {
        Vec right = new Vec(1, 0, 0);

        Vec bottomLeft = DrownFinalKill.pixel(victim, right, 0, 0, 0);
        Vec crest = DrownFinalKill.pixel(victim, right, 6, 10, 0);

        assertThat(bottomLeft.z()).isEqualTo(victim.z());
        assertThat(bottomLeft.y() - victim.y()).isCloseTo(1.25 / 7, offset(1e-9));
        assertThat(crest.y() - victim.y()).isCloseTo(1.25, offset(1e-9));
        double leaningRight = DrownFinalKill.pixel(victim, right, 6, 10, 6).x() - DrownFinalKill.pixel(victim, right, 0, 10, 6).x();
        double leaningLeft = DrownFinalKill.pixel(victim, right, 6, 10, 18).x() - DrownFinalKill.pixel(victim, right, 0, 10, 18).x();
        assertThat(leaningRight).isCloseTo(0.25 * 6 / 7, offset(0.01));
        assertThat(leaningLeft).isCloseTo(-0.25 * 6 / 7, offset(0.01));
    }

    @Test
    void aSquidSinksIntoTheGroundAndLeavesWithASplash() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 24);
        Timeline timeline = new Timeline();

        new DrownFinalKill().script(Scene.at(victim, new Vec(1, 0, 0)), stage, timeline, new Random(1));
        RecordingStage.FakeActor squid = stage.actors.getFirst();
        timeline.run(0);
        assertThat(squid.position.y()).isEqualTo(25);
        for (int tick = 1; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            if (tick == 4) {
                assertThat(squid.position.y()).isEqualTo(23);
            }
        }

        assertThat(squid.type).isEqualTo("Squid");
        assertThat(stage.actors).hasSize(1).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(stage.sounds).containsSubsequence("random.splash", "liquid.water", "liquid.water", "random.splash");
        assertThat(stage.particles).containsKeys("DRIP_WATER", "BLOCK_DUST", "REDSTONE");
        assertThat(timeline.length()).isEqualTo(DrownFinalKill.END + 1);
    }
}
