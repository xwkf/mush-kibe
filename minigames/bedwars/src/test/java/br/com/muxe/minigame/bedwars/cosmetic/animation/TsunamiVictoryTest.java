package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class TsunamiVictoryTest {

    private static final Vec ORIGIN = new Vec(0.5, 27, 0.5);
    private static final Area AREA = Area.around(ORIGIN, 12);

    private static Scene scene() {
        return scene(AREA);
    }

    private static Scene scene(Area area) {
        return new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), List.of(UUID.randomUUID()), area);
    }

    private static final int TOP = 41;

    private static boolean island(int[] block) {
        return Math.abs(block[0]) <= 6 && Math.abs(block[2]) <= 6 && block[1] >= 20 && block[1] < 27;
    }

    private static boolean pillar(int[] block) {
        return block[0] == 3 && block[2] == 3 && block[1] < 40;
    }

    private static RecordingStage stage() {
        RecordingStage stage = new RecordingStage(block -> island(block) || pillar(block));
        stage.types = block -> {
            if (block[0] == -2 && block[1] == 27 && block[2] == -2) {
                return new BlockType(Material.LONG_GRASS, 1);
            }
            if (block[0] == -3 && block[1] == 27 && block[2] == -3) {
                return new BlockType(Material.TORCH, 5);
            }
            return new BlockType(island(block) || pillar(block) ? Material.STONE : Material.AIR, 0);
        };
        return stage;
    }

    private static Timeline script(RecordingStage stage) {
        return script(stage, scene());
    }

    private static Timeline script(RecordingStage stage, Scene scene) {
        Timeline timeline = new Timeline();
        new TsunamiVictory().script(scene, stage, timeline, new Random(7));
        return timeline;
    }

    private static void run(Timeline timeline, int from, int to) {
        for (int tick = from; tick <= to; tick++) {
            timeline.run(tick);
        }
    }

    @Test
    void itIsTheTsunamiEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(stage());

        assertThat(new TsunamiVictory().id()).isEqualTo("tsunami");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void everyRiderCanFlyOverTheFloodFromTheStart() {
        RecordingStage stage = stage();
        UUID winner = UUID.randomUUID();
        UUID teammate = UUID.randomUUID();
        Timeline timeline = script(stage, new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), List.of(winner, teammate), AREA));

        run(timeline, 0, timeline.length());

        assertThat(stage.flyers).containsExactly(winner, teammate);
    }

    @Test
    void theRainFadesInOneHundredthEveryTickUntilItPours() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, timeline.length());

        assertThat(stage.rains).hasSize(101);
        assertThat(stage.rains.getFirst()).isCloseTo(0.01f, within(1e-6f));
        assertThat(stage.rains.get(49)).isCloseTo(0.5f, within(1e-6f));
        assertThat(stage.rains.get(99)).isEqualTo(1f);
        assertThat(stage.rains.getLast()).isEqualTo(1f);
    }

    @Test
    void aGrowlOpensItAndSplashesRiseInPitchEverySevenTicks() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        timeline.run(0);
        assertThat(stage.sounds).containsExactly("mob.enderdragon.growl");
        run(timeline, 1, timeline.length());

        List<Float> splashes = splashPitches(stage);
        assertThat(splashes).hasSize(15);
        assertThat(splashes.getFirst()).isCloseTo(SoundPitch.of(37), within(1e-6f));
        assertThat(splashes.getLast()).isCloseTo(SoundPitch.of(126), within(1e-6f));
        assertThat(splashes).isSorted();
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals)).hasSize(20);
    }

    @Test
    void theFirstSplashComesSevenTicksIn() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, 6);
        assertThat(stage.sounds).doesNotContain("random.splash");
        timeline.run(7);
        assertThat(stage.sounds).contains("random.splash");
    }

    @Test
    void theBaseRunsFourRowsATickAndTheCrestTwoBothFromTheFirstTick() {
        assertThat(Flood.layers(0, 0, 49)).isZero();
        assertThat(Flood.layers(0, 1, 49)).isEqualTo(49);
        assertThat(Flood.layers(1, 1, 49)).isZero();
        assertThat(Flood.layers(4, 3, 49)).isEqualTo(49);
        assertThat(Flood.layers(6, 3, 49)).isEqualTo(25);
        assertThat(Flood.layers(8, 3, 49)).isEqualTo(1);
        assertThat(Flood.layers(9, 3, 49)).isZero();
        assertThat(Flood.layers(64, 17, 49)).isEqualTo(1);
        assertThat(Flood.layers(64, 33, 49)).isEqualTo(49);
        for (int tick = 0; tick < 100; tick++) {
            assertThat(Flood.layers(50, tick + 1, 49)).isGreaterThanOrEqualTo(Flood.layers(50, tick, 49));
        }
    }

    @Test
    void theWaterRisesEveryOtherTickFromTheFirstOne() {
        assertThat(Flood.rises(0)).isFalse();
        assertThat(Flood.rises(1)).isTrue();
        assertThat(Flood.rises(2)).isFalse();
        assertThat(Flood.rises(3)).isTrue();
    }

    @Test
    void theWaveStartsOnTheFarSideOfTheWinnersNearestAxis() {
        assertThat(Flood.rows(new Vec(0, 0, 1), 3, 24, 25, 25)).isZero();
        assertThat(Flood.rows(new Vec(0, 0, 1), 3, 0, 25, 25)).isEqualTo(24);
        assertThat(Flood.rows(new Vec(-0.38, 0, -0.92), 3, 0, 25, 25)).isZero();
        assertThat(Flood.rows(new Vec(-0.9, 0, 0.4), 0, 7, 25, 25)).isZero();
        assertThat(Flood.rows(new Vec(0.9, 0, -0.4), 0, 7, 25, 25)).isEqualTo(24);
    }

    @Test
    void theFirstRowFloodsWholeOnTheFirstTick() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, 1);

        assertThat(stage.sectioned).hasSize(25 * (TOP + 1));
        assertThat(stage.sectioned).allMatch(change -> change.z() == 12);
        assertThat(stage.bulk).isEmpty();
    }

    @Test
    void theWholeMapFloodsFromTheBottomToTwoBlocksAboveItsHighestBlockReplacingOnlyAirAndPlants() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, timeline.length());

        Set<String> placed = new HashSet<>();
        for (BlockChange change : stage.sectioned) {
            assertThat(change.material()).isEqualTo(Material.STATIONARY_WATER);
            assertThat(change.y()).isBetween(0, TOP);
            assertThat(stage.solid(change.x(), change.y(), change.z())).isFalse();
            assertThat(placed.add(change.x() + "," + change.y() + "," + change.z())).isTrue();
        }
        assertThat(placed).contains("-2,27,-2").doesNotContain("-3,27,-3");
        int island = 13 * 13 * 7;
        int pillar = 40 - 7;
        assertThat(placed).hasSize(25 * 25 * (TOP + 1) - island - pillar - 1);
    }

    @Test
    void squidsAndGuardiansHatchOnlyOnceTheWaterReachesThem() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        timeline.run(0);
        assertThat(stage.actors).isEmpty();
        run(timeline, 1, timeline.length());

        int creatures = SeaLife.count(AREA);
        assertThat(stage.actors).hasSize(creatures);
        assertThat(stage.actors).allMatch(actor -> actor.type.equals("Squid") || actor.type.equals("Guardian"));
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Squid"))).isNotEmpty();
        for (FakeActor creature : stage.actors) {
            Vec at = creature.position;
            assertThat(at.y()).isBetween(0.0, TOP + 1.0);
            assertThat(stage.solid((int) Math.floor(at.x()), (int) Math.floor(at.y()), (int) Math.floor(at.z()))).isFalse();
        }
    }

    @Test
    void thereIsOneCreatureEveryHundredAndTwentyFiveSquareBlocksUpToTwoHundredAndForty() {
        assertThat(SeaLife.count(Area.around(Vec.ZERO, 16))).isEqualTo(8);
        assertThat(SeaLife.count(new Area(-65, -65, 81, 81))).isEqualTo(171);
        assertThat(SeaLife.count(new Area(-200, -200, 200, 200))).isEqualTo(240);
        assertThat(SeaLife.count(new Area(0, 0, 1, 1))).isEqualTo(1);
    }

    @Test
    void aSquidPushesOffAtTheEndOfItsStrokeAndThenSlowsDown() {
        assertThat(SwimmingSquid.speed(0.3, 5, 12, 16)).isCloseTo(0.27, within(1e-9));
        assertThat(SwimmingSquid.speed(0.3, 12, 12, 16)).isEqualTo(1);
        assertThat(SwimmingSquid.speed(1, 15, 12, 16)).isEqualTo(1);
        assertThat(SwimmingSquid.speed(1, 16, 12, 16)).isCloseTo(0.9, within(1e-9));
        Random random = new Random(3);
        for (int draw = 0; draw < 100; draw++) {
            Vec heading = SwimmingSquid.heading(random);
            assertThat(Math.hypot(heading.x(), heading.z())).isCloseTo(0.2, within(1e-9));
            assertThat(heading.y()).isBetween(-0.1, 0.1);
        }
    }

    @Test
    void creaturesSwimButNeverLeaveTheWater() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, 60);
        List<Vec> before = stage.actors.stream().map(actor -> actor.position).toList();
        run(timeline, 61, timeline.length());

        long moved = 0;
        for (int index = 0; index < before.size(); index++) {
            FakeActor creature = stage.actors.get(index);
            Vec at = creature.position;
            assertThat(at.y()).isGreaterThanOrEqualTo(0).isLessThan(TOP + 1);
            assertThat(stage.solid((int) Math.floor(at.x()), (int) Math.floor(at.y()), (int) Math.floor(at.z()))).isFalse();
            assertThat(AREA.minX() - 1 <= at.x() && at.x() <= AREA.maxX() + 1).isTrue();
            if (!at.equals(before.get(index))) {
                moved++;
            }
        }
        assertThat(moved).isPositive();
    }

    @Test
    void guardiansLockTheirBeamOnANearbySquidForFourSeconds() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(Area.around(ORIGIN, 24)));

        run(timeline, 0, timeline.length());

        List<FakeActor> hunters = stage.actors.stream().filter(actor -> actor.type.equals("Guardian") && actor.target != null).toList();
        assertThat(hunters).isNotEmpty();
        assertThat(hunters).allMatch(guardian -> ((FakeActor) guardian.target).type.equals("Squid"));
    }

    @Test
    void aBeamEndsAfterEightyTicksAndTheGuardianRestsBeforeTheNextOne() {
        RecordingStage stage = stage();
        FakeActor body = (FakeActor) stage.mob("Guardian", new Vec(0.5, 30, 0.5), 0);
        FakeActor prey = (FakeActor) stage.mob("Squid", new Vec(2.5, 30, 0.5), 0);
        Flood flood = new Flood(stage, AREA, new Vec(0, 0, 1), 27);
        for (int tick = 0; tick < 100; tick++) {
            flood.rise(tick);
        }
        SwimmingSquid squid = new SwimmingSquid(prey, prey.position, new Random(1), 0);
        PatrolGuardian guardian = new PatrolGuardian(body, body.position, 0, new Random(2), 0);

        int locked = -1;
        for (int tick = 0; tick < 40 && locked < 0; tick++) {
            guardian.patrol(flood, List.of(squid));
            if (guardian.beaming()) {
                locked = tick;
            }
        }
        assertThat(locked).isBetween(PatrolGuardian.FIRST_LOCK_MIN, PatrolGuardian.FIRST_LOCK_MAX);
        assertThat(body.target).isSameAs(prey);
        for (int tick = 1; tick < PatrolGuardian.BEAM; tick++) {
            guardian.patrol(flood, List.of(squid));
        }
        assertThat(guardian.beaming()).isTrue();
        guardian.patrol(flood, List.of(squid));
        assertThat(guardian.beaming()).isFalse();
        assertThat(body.watched.get(PatrolGuardian.TARGET)).isEqualTo(PatrolGuardian.NO_TARGET);
    }

    @Test
    void aGuardianIgnoresSquidsOutOfReach() {
        RecordingStage stage = stage();
        FakeActor far = (FakeActor) stage.mob("Squid", new Vec(40, 30, 0), 0);
        SwimmingSquid squid = new SwimmingSquid(far, far.position, new Random(1), 0);

        assertThat(PatrolGuardian.prey(new Vec(0, 30, 0), List.of(squid))).isEmpty();
        assertThat(PatrolGuardian.prey(new Vec(30, 30, 0), List.of(squid))).contains(squid);
    }

    private static List<Float> splashPitches(RecordingStage stage) {
        return IntStream.range(0, stage.sounds.size()).filter(index -> stage.sounds.get(index).equals("random.splash"))
            .mapToObj(stage.pitches::get).toList();
    }
}
