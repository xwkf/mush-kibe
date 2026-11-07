package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class GhorAnimationTest {

    private static final BedFrame BED = BedFrame.of(18, 15, 8, 17, 8);
    private static final Vec BREAKER = new Vec(22.5, 15, 8.5);
    private static final int FLOOR = 14;

    private final RecordingStage stage = new RecordingStage(block -> block[1] <= FLOOR);
    private final Timeline timeline = new Timeline();
    private final List<String> schedule = new ArrayList<>();

    @Test
    void itLastsFromTheBrokenBedUntilTheRiftSealsAndLeavesNothingBehind() {
        runUntil(Integer.MAX_VALUE);

        assertThat(timeline.length()).isEqualTo(GhorAnimation.END);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theSoundsFollowTheRecordedSchedule() {
        runUntil(Integer.MAX_VALUE);

        assertThat(schedule).containsExactly(
            "0 mob.endermen.portal", "0 portal.trigger",
            "6 mob.zombie.unfect",
            "21 mob.slime.attack", "21 mob.endermen.stare",
            "35 mob.enderdragon.growl",
            "41 mob.enderdragon.wings",
            "50 mob.slime.attack",
            "51 mob.zombie.wood", "55 mob.zombie.wood", "59 mob.zombie.wood", "64 mob.zombie.wood",
            "64 mob.enderdragon.growl",
            "78 mob.zombie.woodbreak", "78 random.eat",
            "92 random.burp",
            "98 mob.slime.attack",
            "108 mob.zombie.unfect",
            "120 mob.endermen.portal",
            "126 random.fizz");
    }

    @Test
    void everySoundReachesTheClientWithTheRecordedPitchByte() {
        runUntil(Integer.MAX_VALUE);

        assertThat(stage.pitches).extracting(pitch -> (int) (pitch * 63)).containsExactly(
            31, 100, 31, 31, 75, 50, 100, 31, 53, 44, 53, 37, 75, 50, 37, 37, 94, 44, 37, 31);
    }

    @Test
    void theEyeOpensCalmGlaresTurnsHungryAndBlinksBeforeLeaving() {
        new GhorAnimation().script(BED, BREAKER, stage, timeline, new Random(42));
        List<String> faces = new ArrayList<>();
        for (int tick = 0; tick < GhorAnimation.LEAVE; tick++) {
            timeline.run(tick);
            if (tick >= GhorAnimation.APPEAR) {
                List<RecordingStage.FakeActor> eye = stage.actors.stream().filter(actor -> GhorEye.SKIN.contains(skull(actor)) || isFace(skull(actor))).toList();
                faces.add(tick + " " + face(eye));
                assertThat(eye.subList(GhorEye.FRONT, eye.size())).extracting(GhorAnimationTest::skull).containsExactlyElementsOf(GhorEye.SKIN.subList(GhorEye.FRONT, GhorEye.SKIN.size()));
            }
        }

        assertThat(faces).containsSubsequence("5 CALM", "20 CALM", "21 GLARE", "34 GLARE", "35 HUNGRY", "91 HUNGRY", "92 GLARE", "97 GLARE",
            "98 CALM", "100 CALM", "101 GLARE", "107 GLARE", "108 CALM", "119 CALM");
        assertThat(faces.stream().map(entry -> entry.substring(entry.indexOf(' ') + 1)).distinct()).containsExactly("CALM", "GLARE", "HUNGRY");
        assertThat(GhorAnimation.EXPRESSIONS).extracting(GhorAnimation.Expression::tick).containsExactly(21, 35, 92, 98, 101, 108);
    }

    @Test
    void theEyeIsACubeOfTwentySixDifferentHeadsWithFourTentaclesOfEighteen() {
        runUntil(Integer.MAX_VALUE);

        List<RecordingStage.FakeActor> eye = stage.actors.stream().filter(actor -> GhorEye.SKIN.contains(skull(actor))).toList();
        List<RecordingStage.FakeActor> tentacles = stage.actors.stream().filter(actor -> GhorTentacle.SKIN.equals(skull(actor))).toList();

        assertThat(eye).hasSize(26).allMatch(actor -> actor.style.equals(GhorEye.PIECE));
        assertThat(new HashSet<>(eye.stream().map(GhorAnimationTest::skull).toList())).hasSize(26);
        assertThat(tentacles).hasSize(4 * GhorTentacle.SEGMENTS);
        assertThat(tentacles.stream().filter(actor -> actor.style.equals(GhorEye.PIECE))).hasSize(4 * GhorTentacle.THICK);
        assertThat(tentacles.stream().filter(actor -> actor.style.equals(GhorEye.SMALL_PIECE))).hasSize(4 * (GhorTentacle.SEGMENTS - GhorTentacle.THICK));
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:BED_BLOCK")).extracting(actor -> actor.type)
            .containsExactly("FallingBlock:BED_BLOCK:1", "FallingBlock:BED_BLOCK:9");
        assertThat(stage.actors).filteredOn(actor -> actor.equipment.get(Slot.HELMET) == Material.JACK_O_LANTERN).hasSize(1)
            .allMatch(actor -> actor.style.equals(StandStyle.HIDDEN_SMALL));
    }

    @Test
    void theCubeStaysRigidWhileTheEyeTurns() {
        Gaze gaze = new Gaze(-75, 12);
        Vec center = new Vec(13.5, 16.8, 8.5);

        assertThat(GhorEye.CELLS).hasSize(26).doesNotContain(new GhorEye.Cell(0, 0, 0));
        assertThat(GhorEye.at(center, gaze, new GhorEye.Cell(1, 0, 0)).distance(GhorEye.at(center, gaze, new GhorEye.Cell(-1, 0, 0)))).isCloseTo(2 * GhorEye.CELL, offset(1.0E-9));
        assertThat(GhorEye.at(center, gaze, new GhorEye.Cell(1, 1, 1)).distance(center)).isCloseTo(Math.sqrt(3) * GhorEye.CELL, offset(1.0E-9));
        assertThat(gaze.ahead().length()).isCloseTo(1, offset(1.0E-9));
        assertThat(dot(gaze.ahead(), gaze.up())).isCloseTo(0, offset(1.0E-9));
        assertThat(dot(gaze.ahead(), gaze.side())).isCloseTo(0, offset(1.0E-9));
        assertThat(dot(gaze.up(), gaze.side())).isCloseTo(0, offset(1.0E-9));
        Gaze down = Gaze.toward(Vec.ZERO, new Vec(1, -1, 0));
        assertThat(down.yaw()).isCloseTo(-90, offset(1.0E-9));
        assertThat(down.pitch()).isCloseTo(45, offset(1.0E-9));
    }

    @Test
    void theRiftOpensHoldsAndSealsAroundTheEye() {
        runUntil(Integer.MAX_VALUE);

        assertThat(GhorRift.radius(0, GhorAnimation.LEAVE)).isCloseTo(0.57, offset(0.01));
        for (int tick = 1; tick < 8; tick++) {
            assertThat(GhorRift.radius(tick, GhorAnimation.LEAVE)).isGreaterThan(GhorRift.radius(tick - 1, GhorAnimation.LEAVE));
        }
        assertThat(GhorRift.radius(GhorAnimation.LEAVE, GhorAnimation.LEAVE)).isCloseTo(GhorRift.RADIUS, offset(0.001));
        assertThat(GhorRift.radius(GhorAnimation.END - 1, GhorAnimation.LEAVE)).isLessThan(0.2);
        int rift = GhorAnimation.END * (GhorRift.RING + GhorRift.CORE);
        assertThat(stage.particles.get("REDSTONE")).isGreaterThanOrEqualTo(rift + GhorAnimation.BLOOD);
        assertThat(stage.particles).containsEntry("DRIP_LAVA", 38).containsEntry("PORTAL", 2).containsEntry("BLOCK_CRACK", 6).containsEntry("FIREWORKS_SPARK", 1);
    }

    @Test
    void theEyeComesOutFromBehindTheRiftAndGoesBackThroughIt() {
        Vec rift = BED.at(GhorAnimation.RIFT_AHEAD, GhorAnimation.RIFT_HEIGHT, 0);
        Vec rest = GhorHunt.rest(BED);
        Gaze start = Gaze.toward(GhorEye.neck(rest), BED.middle());
        Vec behind = rest.subtract(start.ahead().multiply(GhorHunt.EMERGE_DISTANCE));

        assertThat(GhorEye.neck(behind).distance(BED.middle())).isGreaterThan(rift.distance(BED.middle()));
        assertThat(GhorEye.neck(rest).distance(BED.middle())).isLessThan(rift.distance(BED.middle()));
        assertThat(GhorHunt.emerging(GhorAnimation.APPEAR)).isEqualTo(1);
        assertThat(GhorHunt.emerging(GhorAnimation.STARE)).isLessThan(0.01);
        assertThat(GhorHunt.retreat(GhorAnimation.GROAN)).isZero();
        assertThat(GhorHunt.retreat(GhorAnimation.LEAVE)).isGreaterThan(GhorHunt.EMERGE_DISTANCE);

        runUntil(Integer.MAX_VALUE);
        RecordingStage.FakeActor piece = stage.actors.stream().filter(actor -> GhorEye.SKIN.contains(skull(actor))).findFirst().orElseThrow();
        assertThat(piece.position.distance(BED.middle())).isGreaterThan(rift.distance(BED.middle()));
    }

    @Test
    void theEyeWatchesTheBedThenTheBreakerThenTheBedUntilItEatsIt() {
        assertThat(GhorHunt.watchesBed(GhorAnimation.EMERGE)).isTrue();
        assertThat(GhorHunt.watchesBed(GhorAnimation.STARE)).isTrue();
        assertThat(GhorHunt.watchesBed(GhorAnimation.STARE + 1)).isFalse();
        assertThat(GhorHunt.watchesBed(GhorAnimation.GROWL)).isFalse();
        assertThat(GhorHunt.watchesBed(GhorAnimation.GROWL + 1)).isTrue();
        assertThat(GhorHunt.watchesBed(GhorAnimation.EAT)).isTrue();
        assertThat(GhorHunt.watchesBed(GhorAnimation.EAT + 1)).isFalse();
    }

    @Test
    void theEyeHoldsItsHeightWhileItDragsTheBedIn() {
        double held = GhorHunt.drift(BED, GhorAnimation.PULL).y();

        for (int tick = GhorAnimation.PULL; tick <= GhorAnimation.EAT; tick++) {
            assertThat(GhorHunt.drift(BED, tick).y()).as("tick " + tick).isCloseTo(held, offset(1.0E-9));
        }
        assertThat(GhorHunt.drift(BED, GhorAnimation.STARE).y()).isNotCloseTo(held, offset(0.1));
    }

    @Test
    void eachTentacleShootsOutAndGripsACornerOfTheBed() {
        runUntil(GhorAnimation.GRAB);

        List<RecordingStage.FakeActor> segments = stage.actors.stream().filter(actor -> GhorTentacle.SKIN.equals(skull(actor))).toList();
        for (int limb = 0; limb < GhorAnimation.LIMBS.length; limb++) {
            Vec tip = segments.get(limb * GhorTentacle.SEGMENTS + GhorTentacle.SEGMENTS - 1).position.add(0, -GhorTentacle.SMALL_LIFT, 0);
            Vec grip = GhorHunt.grip(BED, GhorAnimation.LIMBS[limb][0], GhorAnimation.LIMBS[limb][1]);
            assertThat(tip.distance(grip)).as("tentacle " + limb).isLessThan(0.1);
        }
        assertThat(GhorHunt.reach(GhorAnimation.LAUNCHES[0] - 1, GhorAnimation.LAUNCHES[0])).isZero();
        assertThat(GhorHunt.reach(GhorAnimation.LAUNCHES[0], GhorAnimation.LAUNCHES[0])).isCloseTo(0.6, offset(1.0E-9));
        assertThat(GhorHunt.reach(GhorAnimation.EAT + 1, GhorAnimation.LAUNCHES[0])).isZero();
    }

    @Test
    void theTentaclesBowOutwardsAndFoldBackIntoTheEyeAfterTheMeal() {
        Vec root = new Vec(12.4, 17.5, 9.2);
        Vec tip = new Vec(17.6, 13.6, 8.8);
        List<Vec> points = GhorTentacle.points(root, tip, -1, 1, 0);

        assertThat(points).hasSize(GhorTentacle.SEGMENTS);
        assertThat(points.getFirst()).isEqualTo(root);
        assertThat(points.getLast().distance(tip)).isLessThan(1.0E-9);
        assertThat(awayFromLine(points.get(GhorTentacle.SEGMENTS / 3), root, tip)).isGreaterThan(1);
        assertThat(points.get(GhorTentacle.SEGMENTS / 3).z()).isGreaterThan(root.z());
        assertThat(GhorTentacle.bow(0)).isZero();
        assertThat(GhorTentacle.bow(1)).isZero();
        assertThat(GhorTentacle.points(root, root, 1, 1, 0)).containsOnly(root);

        runUntil(GhorAnimation.EAT + 1);
        List<RecordingStage.FakeActor> segments = stage.actors.stream().filter(actor -> GhorTentacle.SKIN.equals(skull(actor))).toList();
        Vec base = segments.getFirst().position;
        assertThat(segments.get(GhorTentacle.SEGMENTS - 1).position.add(0, -GhorTentacle.SMALL_LIFT, 0).distance(base)).isLessThan(1.0E-9);
    }

    @Test
    void theBedShakesUpAndIsDraggedFasterAndFasterIntoTheMouth() {
        assertThat(GhorHunt.lift(BED, GhorAnimation.GRAB)).isEqualTo(Vec.ZERO);
        assertThat(GhorHunt.lift(BED, GhorAnimation.PULL).y()).isCloseTo(0.6, offset(1.0E-9));
        assertThat(GhorHunt.pull(GhorAnimation.EAT - GhorAnimation.PULL)).isCloseTo(3.87, offset(0.01));
        assertThat(GhorHunt.pull(2) - GhorHunt.pull(1)).isLessThan(GhorHunt.pull(12) - GhorHunt.pull(11));
        Vec swallowed = BED.head().add(GhorHunt.lift(BED, GhorAnimation.EAT));
        Vec neck = GhorEye.neck(GhorHunt.rest(BED).add(GhorHunt.drift(BED, GhorAnimation.EAT)));
        assertThat(swallowed.distance(neck)).isLessThan(1);

        runUntil(GhorAnimation.EAT);
        assertThat(stage.actors).filteredOn(actor -> actor.type.startsWith("FallingBlock:BED_BLOCK")).allMatch(RecordingStage.FakeActor::removed);
    }

    private void runUntil(int last) {
        new GhorAnimation().script(BED, BREAKER, stage, timeline, new Random(42));
        for (int tick = 0; tick <= Math.min(last, timeline.length()); tick++) {
            int before = stage.sounds.size();
            timeline.run(tick);
            for (String sound : stage.sounds.subList(before, stage.sounds.size())) {
                schedule.add(tick + " " + sound);
            }
        }
    }

    private static boolean isFace(String texture) {
        return Arrays.stream(GhorEye.Face.values()).anyMatch(face -> face.skin().contains(texture));
    }

    private static String face(List<RecordingStage.FakeActor> eye) {
        List<String> front = eye.subList(0, GhorEye.FRONT).stream().map(GhorAnimationTest::skull).toList();
        return Arrays.stream(GhorEye.Face.values()).filter(face -> face.skin().equals(front)).map(Enum::name).findFirst().orElse("MIXED");
    }

    private static String skull(RecordingStage.FakeActor actor) {
        return actor.skulls.getOrDefault(Slot.HELMET, "");
    }

    private static double awayFromLine(Vec point, Vec from, Vec to) {
        Vec along = to.subtract(from).normalize();
        Vec relative = point.subtract(from);
        return relative.subtract(along.multiply(dot(relative, along))).length();
    }

    private static double dot(Vec first, Vec second) {
        return first.x() * second.x() + first.y() * second.y() + first.z() * second.z();
    }
}
