package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
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

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < 27 || (block[0] == 3 && block[2] == 3 && block[1] < 30));
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
    void theToeRunsAheadOfTheCrestAndTheWaterRampsBetweenThem() {
        assertThat(Flood.layers(10, 3)).isZero();
        assertThat(Flood.layers(0, 3)).isBetween(1, 63);
        assertThat(Flood.layers(0, 10)).isEqualTo(64);
        assertThat(Flood.layers(30, 20)).isBetween(1, 63);
        assertThat(Flood.layers(20, 20)).isGreaterThan(Flood.layers(40, 20));
        assertThat(Flood.layers(76, 20)).isZero();
        for (int tick = 0; tick < 100; tick++) {
            assertThat(Flood.layers(50, tick + 1)).isGreaterThanOrEqualTo(Flood.layers(50, tick));
        }
    }

    @Test
    void theWaveComesFromTheFarSideTheWinnerFaces() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, 5);

        assertThat(stage.bulk).isNotEmpty();
        assertThat(stage.bulk).allMatch(change -> change.z() >= 8);
    }

    @Test
    void theWholeMapFloodsWithStillWaterUpToThirtySixBlocksAboveTheWinnerWithoutTouchingBlocks() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, timeline.length());

        Set<String> placed = new HashSet<>();
        for (BlockChange change : stage.bulk) {
            assertThat(change.material()).isEqualTo(Material.STATIONARY_WATER);
            assertThat(change.y()).isBetween(27, 63);
            assertThat(stage.solid(change.x(), change.y(), change.z())).isFalse();
            assertThat(placed.add(change.x() + "," + change.y() + "," + change.z())).isTrue();
        }
        int columns = 25 * 25;
        assertThat(placed).hasSize(columns * 37 - 3);
    }

    @Test
    void squidsAndGuardiansHatchOnlyOnceTheWaterReachesThem() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage);

        run(timeline, 0, 2);
        assertThat(stage.actors).isEmpty();
        run(timeline, 3, timeline.length());

        int creatures = SeaLife.count(AREA);
        assertThat(stage.actors).hasSize(creatures);
        assertThat(stage.actors).allMatch(actor -> actor.type.equals("Squid") || actor.type.equals("Guardian"));
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Squid"))).isNotEmpty();
        for (FakeActor creature : stage.actors) {
            Vec at = creature.position;
            assertThat(at.y()).isBetween(27.0, 64.0);
            assertThat(stage.solid((int) Math.floor(at.x()), (int) Math.floor(at.y()), (int) Math.floor(at.z()))).isFalse();
        }
    }

    @Test
    void thereIsOneCreatureEveryHundredAndFourSquareBlocksUpToTwoHundredAndForty() {
        assertThat(SeaLife.count(Area.around(Vec.ZERO, 16))).isEqualTo(10);
        assertThat(SeaLife.count(new Area(-71, -71, 87, 95))).isEqualTo(240);
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
            assertThat(at.y()).isGreaterThanOrEqualTo(27).isLessThan(64);
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
