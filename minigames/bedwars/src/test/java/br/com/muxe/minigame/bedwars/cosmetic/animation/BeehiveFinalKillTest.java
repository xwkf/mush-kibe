package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class BeehiveFinalKillTest {

    private final Vec victim = new Vec(8.5, 27, -1.5);

    @Test
    void tenBeesLeaveTheHiveAboutEveryFiveTicks() {
        List<Integer> spawns = IntStream.range(0, BeehiveFinalKill.BEES).mapToObj(BeehiveFinalKill::spawn).toList();

        assertThat(spawns).containsExactly(5, 9, 14, 19, 23, 28, 33, 37, 42, 47);
        assertThat(spawns.getLast() + BeehiveFinalKill.BEE_LIFE).isLessThan(BeehiveFinalKill.END);
    }

    @Test
    void aBeeBobsUpThenDownThenClimbsAboveWhereItStarted() {
        assertThat(BeehiveFinalKill.bob(0)).isCloseTo(0.59, offset(0.02));
        assertThat(BeehiveFinalKill.bob(7)).isCloseTo(1.06, offset(0.02));
        assertThat(BeehiveFinalKill.bob(22)).isCloseTo(0.22, offset(0.02));
        assertThat(BeehiveFinalKill.bob(BeehiveFinalKill.BEE_LIFE - 1)).isCloseTo(1.38, offset(0.02));
    }

    @Test
    void theHiveReleasesBeesThatWanderNearbyAndPopBeforeTheHiveGoes() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();

        new BeehiveFinalKill().script(Scene.at(victim, new Vec(0, 0, 1)), stage, timeline, new Random(5));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        List<RecordingStage.FakeActor> stands = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).toList();
        assertThat(stands).hasSize(1 + BeehiveFinalKill.BEES);
        assertThat(stands.getFirst().skulls).containsEntry(Slot.HELMET, BeehiveFinalKill.HIVE);
        List<RecordingStage.FakeActor> bees = stands.subList(1, stands.size());
        assertThat(bees).allSatisfy(bee -> {
            assertThat(bee.skulls).containsEntry(Slot.HELMET, BeehiveFinalKill.BEE);
            assertThat(Math.hypot(bee.position.x() - victim.x(), bee.position.z() - victim.z())).isLessThan(BeehiveFinalKill.BEE_LIFE * BeehiveFinalKill.MAX_SPEED);
            assertThat(bee.pose.head().x()).isBetween(-BeehiveFinalKill.MAX_HEAD_TILT, BeehiveFinalKill.MAX_HEAD_TILT);
        });
        assertThat(Collections.frequency(stage.sounds, "mob.chicken.plop")).isEqualTo(BeehiveFinalKill.BEES);
        assertThat(Collections.frequency(stage.sounds, "mob.bat.loop")).isEqualTo(BeehiveFinalKill.BUZZES);
        assertThat(stage.particles).containsEntry("CLOUD", BeehiveFinalKill.BEES + 1);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
