package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.PaparazziVictory.FacePoint;
import br.com.muxe.minigame.api.cosmetic.animation.PaparazziVictory.Placement;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class PaparazziVictoryTest {

    private static final List<double[]> PALETTE = List.of(
        PaparazziVictory.YELLOW, PaparazziVictory.BLUE, PaparazziVictory.WHITE, PaparazziVictory.BROWN, PaparazziVictory.ORANGE
    );

    @Test
    void theFaceIsSeventySevenPointsSplitByFeature() {
        assertThat(PaparazziVictory.FACE).hasSize(77);
        assertThat(count(PaparazziVictory.YELLOW)).isEqualTo(16);
        assertThat(count(PaparazziVictory.BLUE)).isEqualTo(10);
        assertThat(count(PaparazziVictory.WHITE)).isEqualTo(16);
        assertThat(count(PaparazziVictory.BROWN)).isEqualTo(15);
        assertThat(count(PaparazziVictory.ORANGE)).isEqualTo(20);
        assertThat(PaparazziVictory.FACE).allSatisfy(point -> assertThat(PALETTE).anySatisfy(color -> assertThat(color).isSameAs(point.color())));
    }

    @Test
    void theFaceIsAnOutlineCircleWithTwoSideRingsAndTheCapOnTop() {
        List<FacePoint> outline = PaparazziVictory.FACE.subList(0, 26);

        assertThat(outline).allSatisfy(point -> assertThat(Math.hypot(point.u(), point.v())).isCloseTo(0.7, offset(1e-9)));
        assertThat(outline.getFirst().u()).isCloseTo(0.7, offset(1e-9));
        assertThat(outline).filteredOn(point -> point.color() == PaparazziVictory.BLUE).allSatisfy(point -> assertThat(point.v()).isGreaterThan(0.3));
        assertThat(PaparazziVictory.FACE).allSatisfy(point -> {
            assertThat(Math.abs(point.u())).isLessThanOrEqualTo(0.89);
            assertThat(point.v()).isBetween(-0.7 - 1e-9, 0.7 + 1e-9);
        });
        assertThat(PaparazziVictory.FACE).anySatisfy(point -> assertThat(point.u()).isCloseTo(0.889, offset(1e-3)));
    }

    @Test
    void theFaceIsMirroredLeftToRight() {
        assertThat(PaparazziVictory.FACE).allSatisfy(point -> assertThat(PaparazziVictory.FACE).anySatisfy(mirror -> {
            assertThat(mirror.u()).isCloseTo(-point.u(), offset(1e-9));
            assertThat(mirror.v()).isCloseTo(point.v(), offset(1e-9));
        }));
    }

    @Test
    void theFillStaysInsideTheDiscAndFollowsTheCapColour() {
        Random random = new Random(3);
        for (int index = 0; index < 500; index++) {
            FacePoint fill = PaparazziVictory.fill(random);
            assertThat(Math.hypot(fill.u(), fill.v())).isLessThanOrEqualTo(0.7 + 1e-9);
            assertThat(fill.color()).isSameAs(fill.v() >= 0.24 ? PaparazziVictory.BLUE : PaparazziVictory.YELLOW);
        }
    }

    @Test
    void aFaceSpawnsThreeToFiveBlocksAheadWithinFortyFiveDegreesAndAtChestToHeadHeight() {
        Vec feet = new Vec(10, 27, -4);
        Vec forward = PaparazziVictory.rotate(new Vec(0, 0, 1), 37);
        Random random = new Random(11);
        for (int index = 0; index < 300; index++) {
            Vec anchor = PaparazziVictory.place(feet, forward, random).anchor();
            Vec across = new Vec(anchor.x() - feet.x(), 0, anchor.z() - feet.z());
            double bearing = Math.toDegrees(Math.atan2(forward.x() * across.z() - forward.z() * across.x(), forward.x() * across.x() + forward.z() * across.z()));
            assertThat(across.length()).isBetween(3.1, 5.0);
            assertThat(Math.abs(bearing)).isLessThanOrEqualTo(45 + 1e-9);
            assertThat(anchor.y() - feet.y()).isBetween(1.27, 2.22);
        }
    }

    @Test
    void theFacePlaneIsVerticalAndTurnedTowardTheWinner() {
        Random random = new Random(5);
        for (int index = 0; index < 100; index++) {
            Vec feet = new Vec(random.nextDouble() * 50 - 25, 27, random.nextDouble() * 50 - 25);
            Vec forward = PaparazziVictory.rotate(new Vec(0, 0, 1), random.nextDouble() * 360);
            Placement placement = PaparazziVictory.place(feet, forward, random);
            Vec lookAtFace = new Vec(placement.anchor().x() - feet.x(), 0, placement.anchor().z() - feet.z());
            Vec viewersRight = Scene.at(Vec.ZERO, lookAtFace).right();

            assertThat(placement.right().y()).isZero();
            assertThat(placement.right().length()).isCloseTo(1, offset(1e-9));
            assertThat(placement.right().x()).isCloseTo(viewersRight.x(), offset(1e-9));
            assertThat(placement.right().z()).isCloseTo(viewersRight.z(), offset(1e-9));
            Vec toward = new Vec(feet.x() - placement.anchor().x(), 0, feet.z() - placement.anchor().z()).normalize();
            for (FacePoint point : PaparazziVictory.FACE) {
                Vec spot = PaparazziVictory.world(placement, point.u(), point.v());
                Vec fromAnchor = spot.subtract(placement.anchor());
                assertThat(fromAnchor.x() * toward.x() + fromAnchor.z() * toward.z()).isCloseTo(0, offset(1e-9));
                assertThat(spot.y() - placement.anchor().y()).isCloseTo(point.v(), offset(1e-9));
            }
        }
    }

    @Test
    void theRightSideOfTheFaceIsTheViewersRight() {
        Vec feet = new Vec(0, 27, 0);
        Vec anchor = new Vec(0, 28.6, 4);
        Placement placement = new Placement(anchor, PaparazziVictory.rightToward(anchor, feet));

        assertThat(PaparazziVictory.world(placement, 1, 0).x()).isCloseTo(-1, offset(1e-9));
    }

    @Test
    void facesComeInBurstsOfThreeSixTicksApartEveryFortyTwoTicks() {
        assertThat(List.of(1, 7, 13, 43, 49, 55, 85, 91, 97, 127, 133, 139, 169, 175, 181)).allSatisfy(tick -> assertThat(PaparazziVictory.startsFace(tick)).isTrue());
        assertThat(List.of(0, 2, 4, 19, 25, 31, 37, 42, 44, 61, 62)).allSatisfy(tick -> assertThat(PaparazziVictory.startsFace(tick)).isFalse());
        assertThat(IntStream.rangeClosed(0, PaparazziVictory.LENGTH).filter(PaparazziVictory::startsFace).count()).isEqualTo(15);
    }

    @Test
    void theFlashesLandInAWideArcInFrontOfTheWinnerBetweenFourAndNineBlocks() {
        Vec feet = new Vec(-8, 40, 12);
        Vec forward = new Vec(1, 0, 0);
        Random random = new Random(9);
        for (int index = 0; index < 300; index++) {
            Vec spot = PaparazziVictory.flashSpot(feet, forward, random);
            double distance = Math.hypot(spot.x() - feet.x(), spot.z() - feet.z());
            double bearing = Math.toDegrees(Math.atan2(spot.z() - feet.z(), spot.x() - feet.x()));
            assertThat(distance).isBetween(4.0, 9.0);
            assertThat(Math.abs(bearing)).isLessThanOrEqualTo(75 + 1e-9);
            assertThat(spot.y() - feet.y()).isBetween(0.5, 3.5);
        }
    }

    @Test
    void theWholeEffectDrawsFacesAndFlashesAroundTheWinnerWhereverHeIsAndCleansUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        stage.eyes = new Vec(100.5, 28.62, -50.5);
        stage.look = new Vec(1, 0, 1);
        Timeline timeline = new Timeline();
        Scene scene = new Scene(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(UUID.randomUUID()));

        new PaparazziVictory().script(scene, stage, timeline, new Random(1));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        int refreshes = 12 * 20 + 16 + 13 + 10;
        assertThat(timeline.length()).isEqualTo(200);
        assertThat(stage.particles.get("REDSTONE")).isEqualTo(refreshes * 99);
        assertThat(stage.particles.get("FIREWORKS_SPARK")).isEqualTo(15 + refreshes * 4);
        assertThat(stage.sounds.stream().filter("mob.chicken.plop"::equals).count()).isEqualTo(15);
        assertThat(stage.sounds.stream().filter("random.click"::equals).count()).isEqualTo(66);
        assertThat(stage.actors).hasSize(66).allSatisfy(actor -> {
            assertThat(actor.type).isEqualTo("Firework");
            assertThat(actor.removed()).isTrue();
            assertThat(Math.hypot(actor.position.x() - 100.5, actor.position.z() + 50.5)).isBetween(4.0, 9.0);
        });
        assertThat(stage.alive()).isZero();
    }

    @Test
    void withoutAnyRiderTheSceneOriginAndFacingAreUsed() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();

        new PaparazziVictory().script(Scene.at(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1)), stage, timeline, new Random(2));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors).isNotEmpty().allSatisfy(actor -> assertThat(Math.hypot(actor.position.x() - 0.5, actor.position.z() - 0.5)).isBetween(4.0, 9.0));
        assertThat(stage.alive()).isZero();
    }

    private static long count(double[] color) {
        return PaparazziVictory.FACE.stream().filter(point -> point.color() == color).count();
    }
}
