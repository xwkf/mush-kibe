package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PumpkinExplosionFinalKillTest {

    private final Vec victim = new Vec(6.5, 11, 17.5);

    @Test
    void aFireworkPopsAboveTheVictimAndThrowsFivePiesAndThreePumpkins() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new PumpkinExplosionFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(2));

        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:PUMPKIN_PIE")).hasSize(5);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Item:PUMPKIN")).hasSize(3);
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("Item:"))
            .allSatisfy(item -> assertThat(item.position).isEqualTo(victim.add(0, 1, 0)));
        timeline.run(0);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Firework")).singleElement()
            .satisfies(rocket -> assertThat(rocket.position).isEqualTo(victim.add(0, 1, 0)));
        assertThat(stage.sounds).containsExactly("fireworks.launch");
    }

    @Test
    void theFoodStaysForFiveSecondsAndThenEverythingIsGone() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();
        new PumpkinExplosionFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(2));

        for (int tick = 0; tick < PumpkinExplosionFinalKill.END; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("Item:")).noneMatch(RecordingStage.FakeActor::removed);
        timeline.run(PumpkinExplosionFinalKill.END);

        assertThat(timeline.length()).isEqualTo(PumpkinExplosionFinalKill.END);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theFoodIsTossedUpAndALittleSideways() {
        Random random = new Random(9);
        for (int toss = 0; toss < 200; toss++) {
            Vec velocity = Spill.scattered(random, PumpkinExplosionFinalKill.SIDEWAYS, PumpkinExplosionFinalKill.MIN_UP, PumpkinExplosionFinalKill.MAX_UP);

            assertThat(velocity.y()).isBetween(0.15, 0.36);
            assertThat(Math.abs(velocity.x())).isLessThanOrEqualTo(0.14);
            assertThat(Math.abs(velocity.z())).isLessThanOrEqualTo(0.14);
        }
    }
}
