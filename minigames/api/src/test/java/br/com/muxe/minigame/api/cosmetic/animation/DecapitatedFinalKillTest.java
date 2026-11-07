package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class DecapitatedFinalKillTest {

    private final Vec victim = new Vec(8.5, 33, -5.5);

    @Test
    void theHeadRisesFiveBlocksInOneAndAHalfSeconds() {
        assertThat(DecapitatedFinalKill.neck(victim, 0).y()).isCloseTo(32.917, offset(1e-3));
        assertThat(DecapitatedFinalKill.neck(victim, DecapitatedFinalKill.FLIGHT).y()).isCloseTo(37.917, offset(1e-3));
        assertThat(DecapitatedFinalKill.stand(victim, 0).y()).isCloseTo(31.729, offset(1e-3));
    }

    @Test
    void theVictimsHeadSpinsUpBleedingWithARisingNoteAndExplodes() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new DecapitatedFinalKill().script(new Scene(victim, new Vec(0, 0, 1), Optional.of("skin")), stage, timeline, new Random(1));
        RecordingStage.FakeActor head = stage.actors.getFirst();
        timeline.run(0);
        float startYaw = head.yaw;
        timeline.run(1);
        float turn = head.yaw - startYaw;
        for (int tick = 2; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(head.skulls).containsEntry(Slot.HELMET, "skin");
        assertThat(startYaw).isCloseTo(180, offset(1e-3f));
        assertThat(turn).isCloseTo(23.9f, offset(0.01f));
        assertThat(stage.particles).containsEntry("BLOCK_CRACK", 15).containsEntry("EXPLOSION_HUGE", 1);
        assertThat(Collections.frequency(stage.sounds, "note.pling")).isEqualTo(15);
        assertThat(stage.pitches.subList(0, 15)).isSorted();
        assertThat(stage.sounds).endsWith("random.explode");
        assertThat(head.removed()).isTrue();
    }

    @Test
    void withoutASkinTheHeadIsAPlainPlayerHead() {
        RecordingStage stage = new RecordingStage(block -> false);

        new DecapitatedFinalKill().script(Scene.at(victim, new Vec(1, 0, 0)), stage, new Timeline(), new Random(2));

        assertThat(stage.actors.getFirst().equipment).containsEntry(Slot.HELMET, Material.SKULL_ITEM);
    }
}
