package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage.FakeActor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class NecromancerVictoryTest {

    private final Vec origin = new Vec(10.5, 27, -3.5);

    private Scene scene() {
        return new Scene(origin, new Vec(0, 0, 1), Optional.empty(), List.of(UUID.randomUUID()));
    }

    private RecordingStage stage() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyes = origin.add(0, 1.62, 0);
        return stage;
    }

    private Timeline script(RecordingStage stage, long seed) {
        Timeline timeline = new Timeline();
        new NecromancerVictory().script(scene(), stage, timeline, new Random(seed));
        return timeline;
    }

    private static double angle(Vec centre, Vec point) {
        return Math.toDegrees(Math.atan2(point.z() - centre.z(), point.x() - centre.x()));
    }

    @Test
    void theThreeSkullsOrbitTheWinnerClockwiseOneAndAThirdBlocksOutBobbingAroundTheChest() {
        Vec winner = new Vec(4, 30, 4);

        for (int tick = 0; tick < 120; tick += 11) {
            for (int index = 0; index < 3; index++) {
                Vec skull = NecromancerVictory.skull(winner, tick, index);

                assertThat(Math.hypot(skull.x() - winner.x(), skull.z() - winner.z())).isCloseTo(1.3, offset(1e-9));
                assertThat(skull.y() - winner.y()).isBetween(0.59 - 1e-9, 0.88 + 1e-9);
            }
        }
        double step = angle(winner, NecromancerVictory.skull(winner, 1, 0)) - angle(winner, NecromancerVictory.skull(winner, 0, 0));
        assertThat(step).isCloseTo(-7.2, offset(1e-9));
        double apart = angle(winner, NecromancerVictory.skull(winner, 5, 1)) - angle(winner, NecromancerVictory.skull(winner, 5, 0));
        assertThat((apart + 360) % 360).isCloseTo(120, offset(1e-9));
    }

    @Test
    void thePurpleRingAndTheWitchTrioTurnInOppositeDirections() {
        Vec winner = new Vec(0, 30, 0);

        Vec ring = NecromancerVictory.ring(winner, 0, 0);
        Vec turnedRing = NecromancerVictory.ring(winner, 1, 0);
        Vec trio = NecromancerVictory.trio(winner, 0, 0);
        Vec turnedTrio = NecromancerVictory.trio(winner, 1, 0);

        assertThat(Math.hypot(ring.x(), ring.z())).isCloseTo(3, offset(1e-9));
        assertThat(ring.y()).isCloseTo(30.1, offset(1e-9));
        assertThat(angle(winner, turnedRing) - angle(winner, ring)).isCloseTo(5.7, offset(1e-9));
        assertThat(Math.hypot(trio.x(), trio.z())).isCloseTo(1.8, offset(1e-9));
        assertThat(trio.y()).isCloseTo(30.2, offset(1e-9));
        assertThat(angle(winner, turnedTrio) - angle(winner, trio)).isCloseTo(-11.4, offset(1e-9));
    }

    @Test
    void theTenRisenDeadHaveDistinctShortNames() {
        List<String> names = NecromancerVictory.names(new Random(2));

        assertThat(names).hasSize(10).doesNotHaveDuplicates().allSatisfy(name -> assertThat(name.length()).isLessThanOrEqualTo(16));
        assertThat(new HashSet<>(NecromancerVictory.NAMES)).hasSize(NecromancerVictory.NAMES.size());
    }

    @Test
    void theWholeShowRaisesTenNamedDeadThatKeepRisingAndSinkingUntilItEndsAndCleansUp() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, 6);
        double lowest = Double.MAX_VALUE;

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            for (FakeActor actor : stage.actors) {
                if (actor.type.equals("Player") && !actor.removed()) {
                    lowest = Math.min(lowest, actor.position.y());
                }
            }
        }

        Map<String, Long> sounds = stage.sounds.stream().collect(Collectors.groupingBy(sound -> sound, Collectors.counting()));
        List<FakeActor> labels = stage.actors.stream().filter(actor -> actor.watched.get(2) != null).toList();
        assertThat(stage.times).hasSize(Nightfall.TICKS + 1).startsWith(Nightfall.MIDDAY).endsWith(Nightfall.NIGHT);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Player"))).hasSize(10);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand"))).hasSize(13);
        assertThat(labels).hasSize(10).allSatisfy(label -> assertThat((String) label.watched.get(2)).startsWith(NecromancerVictory.GREY));
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
        assertThat(lowest).isLessThanOrEqualTo(27 - NecromancerVictory.SINK_DEPTH + 1e-9);
        assertThat(sounds.get("dig.gravel")).isGreaterThan(10);
        assertThat(sounds).containsKeys("mob.endermen.portal", "random.fizz", "mob.ghast.moan", "ambient.weather.thunder");
        assertThat(sounds.get("portal.portal")).isEqualTo(4L);
        assertThat(sounds.get("lightning")).isGreaterThanOrEqualTo(3);
        assertThat(stage.particles).containsKeys("REDSTONE", "SPELL_WITCH", "BLOCK_CRACK", "CLOUD");
    }

    @Test
    void theRisenDeadWalkAtTheWinnersCurrentPositionWhileTheyAreAboveGround() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, 8);
        for (int tick = 0; tick <= 30; tick++) {
            timeline.run(tick);
        }
        Vec moved = new Vec(50.5, 28.62, 40.5);
        stage.eyes = moved;
        Vec winner = moved.add(0, -1.62, 0);
        Map<FakeActor, Vec> before = stage.actors.stream()
            .filter(actor -> actor.type.equals("Player") && !actor.removed() && actor.position.y() > 24.9)
            .collect(Collectors.toMap(actor -> actor, actor -> actor.position));

        for (int tick = 31; tick <= 35; tick++) {
            timeline.run(tick);
        }

        assertThat(before).isNotEmpty();
        assertThat(before.keySet()).allSatisfy(actor ->
            assertThat(ZombieApocalypseVictory.flat(actor.position, winner)).isLessThanOrEqualTo(ZombieApocalypseVictory.flat(before.get(actor), winner) + 1e-9));
        assertThat(before.keySet()).anySatisfy(actor ->
            assertThat(ZombieApocalypseVictory.flat(actor.position, winner)).isLessThan(ZombieApocalypseVictory.flat(before.get(actor), winner) - 0.1));
    }

    @Test
    void withoutAnyFloorTheDeadStayUndergroundAndEverythingStillCleansUp() {
        RecordingStage stage = new RecordingStage(block -> false);
        stage.eyes = origin.add(0, 1.62, 0);
        Timeline timeline = script(stage, 1);
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.sounds).doesNotContain("dig.gravel");
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
