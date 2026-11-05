package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Collections;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PumpkinHarvestFinalKillTest {

    private final Vec victim = new Vec(0.5, 24, 2.5);

    @Test
    void theLanternFacesTheKillerAndGrowsBeforeItBursts() {
        Vec right = Scene.at(victim, new Vec(1, 0, 0)).right();
        Vec center = victim.add(0, PumpkinHarvestFinalKill.CENTER, 0);

        Vec topLeft = PumpkinHarvestFinalKill.pixel(center, right, 0, 0, 0.1);
        Vec bottomRight = PumpkinHarvestFinalKill.pixel(center, right, 12, 10, 0.1);

        assertThat(topLeft.x()).isCloseTo(center.x(), offset(1e-9));
        assertThat(topLeft.z()).isCloseTo(center.z() - 0.6, offset(1e-9));
        assertThat(topLeft.y()).isCloseTo(center.y() + 0.5, offset(1e-9));
        assertThat(bottomRight.z()).isCloseTo(center.z() + 0.6, offset(1e-9));
        assertThat(bottomRight.y()).isCloseTo(center.y() - 0.5, offset(1e-9));
        assertThat(PumpkinHarvestFinalKill.PIXEL).isSorted();
    }

    @Test
    void theHarvestRisesInPitch() {
        assertThat(PumpkinHarvestFinalKill.plop(0)).isCloseTo(0.89f, offset(1e-6f));
        assertThat(PumpkinHarvestFinalKill.plop(PumpkinHarvestFinalKill.THROWS.length - 1)).isCloseTo(1.79f, offset(1e-6f));
    }

    @Test
    void theLanternBurstsIntoSeedsAndAHarvestThatLandsAroundTheVictim() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 24);
        Timeline timeline = new Timeline();

        new PumpkinHarvestFinalKill().script(Scene.at(victim, new Vec(1, 0, 0)), stage, timeline, new Random(6));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:PUMPKIN_SEEDS")).hasSize(PumpkinHarvestFinalKill.SEEDS);
        assertThat(stage.actors).hasSize(PumpkinHarvestFinalKill.SEEDS + PumpkinHarvestFinalKill.THROWS.length);
        assertThat(stage.particles).containsEntry("EXPLOSION_LARGE", 1).containsKey("REDSTONE");
        assertThat(Collections.frequency(stage.sounds, "random.pop")).isEqualTo(1 + PumpkinHarvestFinalKill.THROWS.length);
        assertThat(Collections.frequency(stage.sounds, "note.pling")).isEqualTo(PumpkinHarvestFinalKill.BEATS.length);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
