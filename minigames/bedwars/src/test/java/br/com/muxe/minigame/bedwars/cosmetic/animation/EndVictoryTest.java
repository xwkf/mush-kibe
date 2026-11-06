package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class EndVictoryTest {

    private static final Vec ORIGIN = new Vec(0.5, 27, 0.5);
    private static final Vec RIDER = new Vec(3.5, 27, -2.5);

    private final UUID winner = UUID.randomUUID();

    private RecordingStage stage() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyesOf.put(winner, RIDER.add(0, Riders.EYE_HEIGHT, 0));
        return stage;
    }

    private Timeline script(RecordingStage stage, List<UUID> riders) {
        Timeline timeline = new Timeline();
        new EndVictory().script(new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), riders), stage, timeline, new Random(3));
        return timeline;
    }

    private static void run(Timeline timeline, int from, int to) {
        for (int tick = from; tick <= to; tick++) {
            timeline.run(tick);
        }
    }

    private static List<FakeActor> endermen(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("Enderman")).toList();
    }

    private static long sounds(RecordingStage stage, String name) {
        return stage.sounds.stream().filter(name::equals).count();
    }

    @Test
    void itIsTheEndEffectAndLastsTwoHundredTicksWithoutTouchingTheTime() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, timeline.length());

        assertThat(new EndVictory().id()).isEqualTo("o-end");
        assertThat(timeline.length()).isEqualTo(EndVictory.LENGTH);
        assertThat(stage.times).isEmpty();
        assertThat(stage.alive()).isZero();
        assertThat(sounds(stage, "fireworks.launch")).isEqualTo(20);
    }

    @Test
    void theMapTurnsIntoEndStoneFromTheWinnerOutwards() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 0);
        assertThat(stage.bulk).isEmpty();
        run(timeline, 1, 1);
        int first = stage.bulk.size();
        run(timeline, 2, 2);

        assertThat(stage.bulk.subList(0, first)).anyMatch(change -> change.x() == 0 && change.y() == 26 && change.z() == 0 && change.material() == Material.ENDER_STONE)
            .allMatch(change -> MapWave.distance(change.x(), change.z(), 0, 0) <= EndVictory.WAVE.radius(1));
        assertThat(stage.bulk.subList(first, stage.bulk.size())).isNotEmpty()
            .allMatch(change -> MapWave.distance(change.x(), change.z(), 0, 0) > EndVictory.WAVE.radius(1));
    }

    @Test
    void twoEndermenAppearOnTheGroundAwayFromTheWinner() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, 4);
        assertThat(endermen(stage)).isEmpty();
        run(timeline, 5, 5);
        assertThat(endermen(stage)).hasSize(1);
        run(timeline, 6, 8);

        assertThat(endermen(stage)).hasSize(2).allSatisfy(enderman -> {
            assertThat(enderman.position.y()).isEqualTo(27.0);
            assertThat(Math.hypot(enderman.position.x() - ORIGIN.x(), enderman.position.z() - ORIGIN.z()))
                .isBetween(EndVictory.ENDERMAN_MIN_DISTANCE - 1, EndVictory.ENDERMAN_MAX_DISTANCE + 1);
        });
    }

    @Test
    void oneEndermanTeleportsBesideThePlayerStaresAndThenWalksAway() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, List.of(winner));

        run(timeline, 0, EndVictory.TELEPORT_TICK - 1);
        assertThat(sounds(stage, "mob.endermen.portal")).isZero();
        run(timeline, EndVictory.TELEPORT_TICK, EndVictory.TELEPORT_TICK);

        FakeActor visitor = endermen(stage).getFirst();
        Vec beside = visitor.position;
        assertThat(sounds(stage, "mob.endermen.portal")).isEqualTo(1);
        assertThat(beside.y()).isEqualTo(27.0);
        assertThat(Math.hypot(beside.x() - RIDER.x(), beside.z() - RIDER.z())).isBetween(EndVictory.BESIDE_MIN - 1, EndVictory.BESIDE_MAX + 1);
        assertThat(visitor.yaw).isEqualTo(beside.yawTowards(RIDER));

        run(timeline, EndVictory.TELEPORT_TICK + 1, EndVictory.WALK_FROM - 1);
        assertThat(visitor.position).isEqualTo(beside);
        assertThat(sounds(stage, "mob.endermen.idle")).isEqualTo(1);
        run(timeline, EndVictory.WALK_FROM, EndVictory.LENGTH - 1);

        assertThat(visitor.position.distance(beside)).isGreaterThan(10);
        assertThat(visitor.position.y()).isEqualTo(27.0);
        assertThat(sounds(stage, "step.stone")).isGreaterThan(5);
        assertThat(endermen(stage).get(1).position.distance(ORIGIN)).isGreaterThan(EndVictory.ENDERMAN_MIN_DISTANCE - 1);
    }

    @Test
    void itAlsoPlaysWithNobodyToVisit() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage, List.of());

        run(timeline, 0, timeline.length());

        assertThat(endermen(stage)).hasSize(2).allMatch(FakeActor::removed);
        assertThat(sounds(stage, "mob.endermen.portal")).isEqualTo(1);
    }
}
