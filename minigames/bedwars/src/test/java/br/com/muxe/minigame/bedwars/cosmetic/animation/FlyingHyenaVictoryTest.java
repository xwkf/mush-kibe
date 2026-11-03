package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeMount;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FlyingHyenaVictoryTest {

    private static final double FLOOR_REST = 27 - 0.4375;

    private static Scene scene() {
        return new Scene(new Vec(0, 27, 0), new Vec(0, 0, 1), Optional.empty(), List.of(UUID.randomUUID()));
    }

    private static List<FakeActor> presents(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).skip(1).toList();
    }

    private static Timeline script(RecordingStage stage) {
        Timeline timeline = new Timeline();
        new FlyingHyenaVictory().script(scene(), stage, timeline, new Random(3));
        return timeline;
    }

    @Test
    void itIsTheFelizNatalEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(new RecordingStage(block -> block[1] < 27));

        assertThat(new FlyingHyenaVictory().id()).isEqualTo("feliz-natal");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void nineDifferentGiftsAreHandedOutRoundRobin() {
        assertThat(FlyingHyenaVictory.GIFTS).hasSize(9).doesNotHaveDuplicates();
        assertThat(FlyingHyenaVictory.skin(0)).isEqualTo(FlyingHyenaVictory.GIFTS.getFirst());
        assertThat(FlyingHyenaVictory.skin(9)).isEqualTo(FlyingHyenaVictory.GIFTS.getFirst());
        assertThat(FlyingHyenaVictory.skin(13)).isEqualTo(FlyingHyenaVictory.GIFTS.get(4));
    }

    @Test
    void aPresentFallsOneAndAHalfBlocksPlusFourHundredthsMoreEachTick() {
        assertThat(FlyingHyenaVictory.fallen(0)).isZero();
        assertThat(FlyingHyenaVictory.fallen(1)).isCloseTo(1.54, offset(1e-9));
        assertThat(FlyingHyenaVictory.fallen(10)).isCloseTo(17.2, offset(1e-9));
        assertThat(FlyingHyenaVictory.fallen(11) - FlyingHyenaVictory.fallen(10)).isCloseTo(1.5 + 0.04 * 11, offset(1e-9));
    }

    @Test
    void presentsSpawnInsideTheDiscAboveTheSeat() {
        Vec seat = new Vec(10, 30, -4);
        Random random = new Random(11);
        for (int index = 0; index < 500; index++) {
            Vec spot = FlyingHyenaVictory.presentSpot(seat, random);
            assertThat(Math.hypot(spot.x() - seat.x(), spot.z() - seat.z())).isLessThanOrEqualTo(25);
            assertThat(spot.y() - seat.y()).isBetween(8.0, 13.5);
        }
    }

    @Test
    void fireworksGoOffOnTheRingAroundTheRiderAtFeetHeight() {
        Vec seat = new Vec(10, 30, -4);
        Random random = new Random(5);
        for (int index = 0; index < 100; index++) {
            Vec spot = FlyingHyenaVictory.fireworkSpot(seat, random);
            assertThat(Math.hypot(spot.x() - seat.x(), spot.z() - seat.z())).isCloseTo(15, offset(1e-9));
            assertThat(spot.y()).isCloseTo(29.65, offset(1e-9));
        }
    }

    @Test
    void theRidersSitOnOneMountAndTheHyenaHangsBelowTheSeat() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        timeline.run(0);

        assertThat(stage.mounts).hasSize(1);
        assertThat(stage.mounts.getFirst().seat()).isEqualTo(new Vec(0, 28.375, 0));
        FakeActor hyena = stage.actors.getFirst();
        assertThat(hyena.position).isEqualTo(new Vec(0, 28.375 - 1.8125, 0));
    }

    @Test
    void idleTheSeatOnlyBobsUpAndDown() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        FakeMount mount = stage.mounts.getFirst();
        assertThat(mount.path).hasSize(199);
        assertThat(mount.path).allSatisfy(seat -> {
            assertThat(seat.x()).isEqualTo(0);
            assertThat(seat.z()).isEqualTo(0);
            assertThat(seat.y()).isBetween(27.8, 29.2);
        });
    }

    @Test
    void holdingForwardFliesTheSeatAlongTheLookDirectionAndTheHyenaFollows() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        mount.pushing = true;
        mount.yaw = 90;
        for (int tick = 1; tick <= 30; tick++) {
            timeline.run(tick);
        }

        assertThat(mount.seat().x()).isLessThan(-20);
        assertThat(Math.abs(mount.seat().z())).isLessThan(1e-6);
        FakeActor hyena = stage.actors.getFirst();
        assertThat(hyena.position.x()).isCloseTo(mount.seat().x(), offset(1e-9));
        assertThat(hyena.position.y()).isCloseTo(mount.seat().y() - 1.8125, offset(1e-9));
    }

    @Test
    void presentsRainInBatchesOfSixAndLandOnTheFloor() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        for (int tick = 0; tick <= 6; tick++) {
            timeline.run(tick);
        }
        assertThat(presents(stage)).hasSize(12);

        for (int tick = 7; tick <= 100; tick++) {
            timeline.run(tick);
        }
        assertThat(presents(stage).stream().filter(actor -> !actor.removed && actor.position.y() == FLOOR_REST).count()).isGreaterThan(0);
        assertThat(presents(stage).stream().filter(actor -> !actor.removed).mapToDouble(actor -> actor.position.y()).min().orElseThrow()).isGreaterThanOrEqualTo(FLOOR_REST);
    }

    @Test
    void aLandedPresentIsRemovedTwentyTicksLater() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);
        int landed = -1;
        int removed = -1;

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
            if (presents(stage).isEmpty()) {
                continue;
            }
            FakeActor present = presents(stage).getFirst();
            if (landed < 0 && present.position.y() == FLOOR_REST) {
                landed = tick;
            }
            if (removed < 0 && present.removed) {
                removed = tick;
            }
        }

        assertThat(landed).isGreaterThan(1);
        assertThat(removed - landed).isEqualTo(20);
    }

    @Test
    void overTheVoidPresentsKeepFallingUntilTheEnd() {
        RecordingStage stage = new RecordingStage(block -> false);
        Timeline timeline = script(stage);

        for (int tick = 0; tick <= 150; tick++) {
            timeline.run(tick);
        }
        FakeActor first = presents(stage).getFirst();
        assertThat(first.removed).isFalse();
        assertThat(first.position.y()).isLessThan(-100);

        for (int tick = 151; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theWholeTimelineSpawnsTheEffectAndCleansUpAfterItself() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = script(stage);

        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).count()).isEqualTo(1 + 38 * 6);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Firework")).count()).isEqualTo(20);
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(20);
        assertThat(stage.alive()).isZero();
    }
}
