package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PumpkinHarvestVictoryTest {

    private final Vec origin = new Vec(0.5, 27, 0.5);

    private static long count(RecordingStage stage, String sound) {
        return stage.sounds.stream().filter(sound::equals).count();
    }

    private static void run(Timeline timeline, int from, int until) {
        for (int tick = from; tick <= until; tick++) {
            timeline.run(tick);
        }
    }

    @Test
    void thePumpkinSpeedsUpEachTickAndReachesTheFloorInAboutFifteenTicks() {
        assertThat(PumpkinHarvestVictory.fallen(0)).isZero();
        assertThat(PumpkinHarvestVictory.fallen(1)).isCloseTo(0.625, offset(1e-9));
        assertThat(PumpkinHarvestVictory.fallen(3) - PumpkinHarvestVictory.fallen(2)).isCloseTo(0.625 + 0.056 * 2, offset(1e-9));
        int steps = 0;
        while (PumpkinHarvestVictory.fallen(steps) < 14.4) {
            steps++;
        }
        assertThat(steps).isBetween(13, 17);
    }

    @Test
    void thePumpkinSpinsEighteenDegreesAndALittleMoreEachTick() {
        assertThat(PumpkinHarvestVictory.spin(1)).isCloseTo(-18.28f, offset(1e-4f));
        assertThat(PumpkinHarvestVictory.spin(5)).isCloseTo(-91.4f, offset(1e-3f));
        assertThat(Math.abs(PumpkinHarvestVictory.spin(100))).isLessThan(360);
    }

    @Test
    void theHeadRestsOnTheFloor() {
        assertThat(PumpkinHarvestVictory.rest(27)).isCloseTo(25.625, offset(1e-9));
    }

    @Test
    void pumpkinsFallAllOverTheMapButNotOnTheWinner() {
        Random random = new Random(3);
        Area map = Area.around(origin, 50);
        for (int index = 0; index < 200; index++) {
            Vec spot = Kit.drop(map, origin, PumpkinHarvestVictory.MIN_AHEAD, random);
            assertThat(Math.abs(spot.x() - origin.x())).isLessThanOrEqualTo(50);
            assertThat(Math.abs(spot.z() - origin.z())).isLessThanOrEqualTo(50);
            assertThat(Math.hypot(spot.x() - origin.x(), spot.z() - origin.z())).isGreaterThanOrEqualTo(2.0);
        }
    }

    @Test
    void theTrailPointsAreEvenlySpacedAlongTheFall() {
        List<Vec> points = PumpkinHarvestVictory.trail(new Vec(0, 40, 0), new Vec(0, 38.5, 0));

        assertThat(points).hasSize(7);
        assertThat(points.getFirst().y()).isCloseTo(40 - 0.205, offset(1e-9));
        assertThat(PumpkinHarvestVictory.trail(new Vec(1, 2, 3), new Vec(1, 2, 3))).isEmpty();
    }

    @Test
    void theBurstPiecesFlyUpwardAtTheRecordedSpeeds() {
        Random random = new Random(8);
        for (int index = 0; index < 100; index++) {
            Vec item = PumpkinHarvestVictory.itemVelocity(random);
            assertThat(Math.hypot(item.x(), item.z())).isCloseTo(0.15, offset(1e-9));
            assertThat(item.y()).isBetween(0.30, 0.44);
            Vec small = PumpkinHarvestVictory.smallVelocity(random);
            assertThat(Math.hypot(small.x(), small.z())).isBetween(0.06 - 1e-9, 0.22 + 1e-9);
            assertThat(small.y()).isBetween(0.5, 0.8);
        }
    }

    @Test
    void noFloorMeansNoFloorFound() {
        RecordingStage empty = new RecordingStage(block -> false);
        RecordingStage ground = new RecordingStage(block -> block[1] < 27);

        RecordingStage highIsland = new RecordingStage(block -> block[1] < 27 || block[1] < 40 && block[0] > 10);

        assertThat(PumpkinHarvestVictory.floorAt(empty, 3.5, 27, 3.5)).isEqualTo(OptionalDouble.empty());
        assertThat(PumpkinHarvestVictory.floorAt(ground, 3.5, 27, 3.5)).isEqualTo(OptionalDouble.of(27));
        assertThat(PumpkinHarvestVictory.floorBelow(empty, 3.5, 43, 3.5)).isEqualTo(OptionalDouble.empty());
        assertThat(PumpkinHarvestVictory.floorBelow(ground, 3.5, 43, 3.5)).isEqualTo(OptionalDouble.of(27));
        assertThat(PumpkinHarvestVictory.floorBelow(highIsland, 12.5, 43, 3.5)).isEqualTo(OptionalDouble.of(40));
    }

    @Test
    void thePumpkinLandsOnTheFloorAndTheBurstHappensThere() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new PumpkinHarvestVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(5));

        int tick = 0;
        while (stage.actors.stream().noneMatch(actor -> actor.type.startsWith("Item")) && tick <= 60) {
            timeline.run(tick++);
        }

        FakeActor pumpkin = stage.actors.getFirst();
        assertThat(pumpkin.removed()).isTrue();
        assertThat(pumpkin.position.y()).isCloseTo(PumpkinHarvestVictory.rest(27), offset(1e-9));
        List<FakeActor> items = stage.actors.stream().filter(actor -> actor.type.startsWith("Item")).toList();
        assertThat(items).hasSize(3).allSatisfy(item -> assertThat(item.position.y()).isBetween(27.0, 28.5));
        assertThat(count(stage, "dig.wood")).isEqualTo(1);
        assertThat(stage.particles).containsKeys("BLOCK_DUST", "FLAME", "SMOKE_NORMAL");
        assertThat(tick - 1).isBetween(2 + 14 + 2, 2 + 18 + 2);
    }

    @Test
    void theSmallPumpkinsFlyUpFallAndBreakOnTheFloor() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new PumpkinHarvestVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(6));

        run(timeline, 0, 60);

        assertThat(stage.particles).containsKey("FIREWORKS_SPARK");
        assertThat(count(stage, "mob.chicken.plop")).isGreaterThanOrEqualTo(3);
        assertThat(count(stage, "dig.wood")).isGreaterThanOrEqualTo(4);
        long brokenSmalls = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).skip(1).filter(FakeActor::removed).count();
        assertThat(brokenSmalls).isGreaterThanOrEqualTo(3);
    }

    @Test
    void theWholeTimelineBurstsRepeatedlyAndRemovesEverything() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new PumpkinHarvestVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(7));

        assertThat(timeline.length()).isEqualTo(PumpkinHarvestVictory.LENGTH);
        run(timeline, 0, timeline.length());

        assertThat(count(stage, "dig.wood")).isGreaterThanOrEqualTo(18);
        assertThat(count(stage, "mob.chicken.plop")).isGreaterThanOrEqualTo(30);
        assertThat(count(stage, "fireworks.launch")).isGreaterThanOrEqualTo(39);
        assertThat(stage.actors.stream().filter(actor -> actor.type.startsWith("Item")).count()).isGreaterThanOrEqualTo(54);
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }

    @Test
    void aPumpkinOverTheVoidFallsForeverAndNeverBursts() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = new Timeline();
        new PumpkinHarvestVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(1));

        run(timeline, 0, 2 + 98);
        assertThat(stage.actors.getFirst().removed()).isFalse();
        run(timeline, 2 + 99, timeline.length());

        assertThat(stage.actors.getFirst().removed()).isTrue();
        assertThat(count(stage, "dig.wood")).isZero();
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.type).isEqualTo("ArmorStand"));
        assertThat(stage.actors).allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
