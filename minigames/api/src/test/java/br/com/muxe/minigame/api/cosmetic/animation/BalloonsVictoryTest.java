package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage.FakeMount;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BalloonsVictoryTest {

    private final Vec origin = new Vec(0.5, 27, 0.5);

    @Test
    void theSeatRisesAboutTenBlocksInTwoHundredTicks() {
        double total = 0;
        for (int tick = 1; tick <= BalloonsVictory.LENGTH; tick++) {
            total += BalloonsVictory.lift(tick);
        }

        assertThat(total).isBetween(8.0, 11.0);
        assertThat(BalloonsVictory.lift(0)).isEqualTo(BalloonsVictory.SLOW_START_LIFT);
        assertThat(BalloonsVictory.lift(30)).isCloseTo(0.12, offset(1e-9));
        assertThat(BalloonsVictory.lift(51)).isCloseTo(-0.02, offset(1e-9));
    }

    @Test
    void theSeatOnlyMovesSidewaysWhileTheLeaderPushesAlongTheLookDirection() {
        Vec seat = new Vec(0, 40, 0);

        Vec idle = BalloonsVictory.step(seat, 90, false, 30);
        Vec south = BalloonsVictory.step(seat, 0, true, 30);
        Vec west = BalloonsVictory.step(seat, 90, true, 30);

        assertThat(idle.x()).isZero();
        assertThat(idle.z()).isZero();
        assertThat(south.z()).isCloseTo(0.25, offset(1e-9));
        assertThat(south.x()).isCloseTo(0, offset(1e-9));
        assertThat(west.x()).isCloseTo(-0.25, offset(1e-9));
        assertThat(west.y()).isCloseTo(40 + BalloonsVictory.lift(30), offset(1e-9));
    }

    @Test
    void theSwayStaysWithinTwentyFiveDegreesAndTheCircleStartsAtTheSpawnPoint() {
        for (int tick = 0; tick < 300; tick++) {
            assertThat(Math.abs(BalloonsVictory.sway(tick, 1.3))).isLessThanOrEqualTo(25.0001f);
            Vec circle = BalloonsVictory.circle(tick, 1.3);
            assertThat(circle.length()).isLessThanOrEqualTo(2 * BalloonsVictory.CIRCLE_RADIUS + 1e-9);
        }
        assertThat(BalloonsVictory.circle(0, 2.2)).isEqualTo(Vec.ZERO);
        assertThat(BalloonsVictory.drift(0, 2.2)).isZero();
    }

    @Test
    void theHandBalloonsShootUpFastThenFollowTheSlowLaw() {
        double before = BalloonsVictory.rise(BalloonsVictory.BURST_TICKS, 0.7);
        double after = BalloonsVictory.rise(BalloonsVictory.BURST_TICKS + 1, 0.7);

        assertThat(before).isCloseTo(6.2, offset(1e-9));
        assertThat(after - before).isBetween(0.025, 0.075);
        assertThat(BalloonsVictory.rise(10, 0.7)).isCloseTo(3.1, offset(1e-9));
    }

    @Test
    void aTrailingBalloonAcceleratesGentlyAndNeverExceedsItsSpeedLimit() {
        Vec gap = new Vec(40, 0, 0);
        Vec velocity = BalloonsVictory.chase(Vec.ZERO, gap);

        assertThat(velocity.x()).isCloseTo(BalloonsVictory.CHASE_ACCELERATION, offset(1e-9));
        for (int tick = 0; tick < 100; tick++) {
            velocity = BalloonsVictory.chase(velocity, gap);
        }
        assertThat(velocity.x()).isCloseTo(BalloonsVictory.CHASE_LIMIT, offset(1e-9));
        assertThat(BalloonsVictory.chase(Vec.ZERO, Vec.ZERO)).isEqualTo(Vec.ZERO);
    }

    @Test
    void spawnOffsetsAreRandomInsideTheRecordedRanges() {
        Random random = new Random(11);
        Set<Double> seen = new HashSet<>();
        for (int index = 0; index < 200; index++) {
            Vec hand = BalloonsVictory.handSpawn(random);
            assertThat(hand.x()).isBetween(BalloonsVictory.HAND_MIN_X, BalloonsVictory.HAND_MAX_X);
            assertThat(hand.y()).isBetween(BalloonsVictory.HAND_MIN_Y, BalloonsVictory.HAND_MAX_Y);
            assertThat(hand.z()).isBetween(BalloonsVictory.HAND_MIN_Z, BalloonsVictory.HAND_MAX_Z);
            Vec loose = BalloonsVictory.looseSpawn(random);
            assertThat(Math.abs(loose.x())).isLessThanOrEqualTo(10);
            assertThat(Math.abs(loose.z())).isLessThanOrEqualTo(10);
            assertThat(loose.y()).isBetween(BalloonsVictory.LOOSE_MIN_Y, BalloonsVictory.LOOSE_MAX_Y);
            assertThat(BalloonsVictory.popTick(random)).isBetween(82, 120);
            seen.add(hand.x());
        }
        assertThat(seen).hasSizeGreaterThan(150);
    }

    @Test
    void theNineSkinsCycleAndRepeat() {
        assertThat(BalloonsVictory.SKINS).hasSize(9).doesNotHaveDuplicates();
        assertThat(BalloonsVictory.skin(9)).isEqualTo(BalloonsVictory.skin(0));
        assertThat(BalloonsVictory.skin(24)).isEqualTo(BalloonsVictory.SKINS[6]);
    }

    @Test
    void theWinnerRidesASeatThatOnlyMovesWithWAndKeepsRising() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new BalloonsVictory().script(new Scene(origin, new Vec(0, 0, 1), Optional.empty(), List.of()), stage, timeline, new Random(4));

        timeline.run(0);
        assertThat(stage.mounts).hasSize(1);
        FakeMount mount = stage.mounts.getFirst();
        assertThat(mount.seat).isEqualTo(origin);
        for (int tick = 1; tick <= 20; tick++) {
            timeline.run(tick);
        }
        assertThat(mount.seat.x()).isEqualTo(origin.x());
        assertThat(mount.seat.z()).isEqualTo(origin.z());
        assertThat(mount.seat.y()).isGreaterThan(origin.y());

        mount.yaw = 90;
        mount.pushing = true;
        for (int tick = 21; tick <= 30; tick++) {
            timeline.run(tick);
        }
        assertThat(mount.seat.x()).isCloseTo(origin.x() - 2.5, offset(1e-9));
        assertThat(mount.seat.z()).isCloseTo(origin.z(), offset(1e-9));
        mount.pushing = false;
        double stopped = mount.seat.x();
        for (int tick = 31; tick <= BalloonsVictory.LENGTH; tick++) {
            timeline.run(tick);
        }
        assertThat(mount.seat.x()).isEqualTo(stopped);
        assertThat(mount.seat.y() - origin.y()).isBetween(8.0, 11.0);
    }

    @Test
    void everyBalloonIsLeashedAndTheLooseOnesPopInsideTheWindow() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new BalloonsVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(9));

        timeline.run(0);
        assertThat(stage.actors).hasSize(141);
        long leashed = stage.actors.stream().filter(actor -> actor.watched.containsKey(-1)).count();
        assertThat(leashed).isEqualTo(55);
        assertThat(stage.actors).filteredOn(actor -> actor.type.equals("Chicken")).allSatisfy(actor -> assertThat(actor.watched.get(0)).isEqualTo((byte) 32));
        for (int tick = 1; tick < BalloonsVictory.POP_FROM; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.sounds).doesNotContain("mob.chicken.plop");
        assertThat(stage.alive()).isEqualTo(141);
        for (int tick = BalloonsVictory.POP_FROM; tick <= BalloonsVictory.POP_UNTIL; tick++) {
            timeline.run(tick);
        }
        assertThat(stage.sounds.stream().filter("mob.chicken.plop"::equals).count()).isEqualTo(30);
        assertThat(stage.alive()).isEqualTo(51);
    }

    @Test
    void theWholeTimelineEmitsParticlesAndLaunchSoundsThenCleansUp() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27);
        Timeline timeline = new Timeline();
        new BalloonsVictory().script(Scene.at(origin, new Vec(0, 0, 1)), stage, timeline, new Random(2));

        assertThat(timeline.length()).isEqualTo(BalloonsVictory.LENGTH);
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.particles).containsKeys("CRIT", "CRIT_MAGIC");
        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(20);
        assertThat(stage.actors).isNotEmpty().allSatisfy(actor -> assertThat(actor.removed()).isTrue());
    }
}
