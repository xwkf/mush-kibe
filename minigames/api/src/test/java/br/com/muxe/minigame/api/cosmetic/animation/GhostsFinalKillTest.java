package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GhostsFinalKillTest {

    private final Vec victim = new Vec(8.5, 33, -5.5);

    @Test
    void threeGhostsRiseFromTheVictimAndFizzOutAfterTheirFlight() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();
        new GhostsFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(7));

        assertThat(stage.particles).containsEntry("CLOUD", 1);
        for (int tick = 0; tick <= 10; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.actors).hasSize(3).allSatisfy(ghost -> {
            assertThat(ghost.style).isEqualTo(StandStyle.HIDDEN);
            assertThat(ghost.skulls).containsEntry(Slot.HELMET, HauntedVictory.GHOST);
            assertThat(Math.hypot(ghost.position.x() - victim.x(), ghost.position.z() - victim.z())).isLessThan(2.5);
        });
        assertThat(stage.sounds).filteredOn("mob.bat.death"::equals).hasSize(3);
        assertThat(stage.sounds).filteredOn("mob.bat.loop"::equals).hasSize(3);

        for (int tick = 11; tick < 48; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.alive()).isEqualTo(3);
        timeline.run(48);
        assertThat(stage.alive()).isEqualTo(2);

        for (int tick = 49; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        assertThat(timeline.length()).isEqualTo(57);
        assertThat(stage.alive()).isZero();
        assertThat(stage.sounds).filteredOn("random.fizz"::equals).hasSize(3);
        assertThat(stage.particles.get("CLOUD")).isGreaterThan(1 + 3 + 3 + 3 * 8);
    }
}
