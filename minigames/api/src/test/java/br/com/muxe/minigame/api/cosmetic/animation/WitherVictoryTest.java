package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage.FakeActor;
import br.com.muxe.minigame.api.cosmetic.animation.RecordingStage.FakeMount;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WitherVictoryTest {

    private static final Vec ORIGIN = new Vec(0.5, 27, 0.5);
    private final UUID winner = UUID.randomUUID();

    private Scene scene(UUID... riders) {
        return new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), List.of(riders));
    }

    private static RecordingStage stage(Predicate<int[]> solid) {
        return new RecordingStage(solid);
    }

    private static RecordingStage ground() {
        return stage(block -> block[1] < 27);
    }

    private Timeline script(RecordingStage stage, Scene scene) {
        Timeline timeline = new Timeline();
        new WitherVictory().script(scene, stage, timeline, new Random(11));
        return timeline;
    }

    private static void run(Timeline timeline, int from, int to) {
        for (int tick = from; tick <= to; tick++) {
            timeline.run(tick);
        }
    }

    private static FakeActor wither(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("WitherBoss")).findFirst().orElseThrow();
    }

    private static long count(RecordingStage stage, String sound) {
        return stage.sounds.stream().filter(sound::equals).count();
    }

    @Test
    void itIsTheWitherEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(ground(), scene(winner));

        assertThat(new WitherVictory().id()).isEqualTo("wither");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void theWinnerSitsOnTheWitherSpawnedAtTheirFeetAndItSetsOffAtOnce() {
        RecordingStage stage = ground();
        Timeline timeline = script(stage, scene(winner));

        timeline.run(0);

        FakeMount mount = stage.mounts.getFirst();
        assertThat(mount.riders).containsExactly(winner);
        assertThat(mount.start.x()).isCloseTo(0.5, within(1e-9));
        assertThat(mount.start.y()).isCloseTo(30.375, within(1e-9));
        assertThat(mount.start.z()).isCloseTo(0.025, within(1e-9));
        Vec position = wither(stage).position;
        assertThat(position.y()).isEqualTo(27);
        assertThat(position.z()).isCloseTo(1.0, within(1e-9));
        assertThat(mount.seat().z()).isCloseTo(0.525, within(1e-9));
    }

    @Test
    void itAlwaysFliesHalfABlockATickWhereTheRiderLooksAndStaysLevelNearTheHorizon() {
        Vec level = WitherVictory.velocity(0, 4);
        assertThat(level.y()).isZero();
        assertThat(level.z()).isCloseTo(0.5 * Math.cos(Math.toRadians(4)), within(1e-9));
        Vec climb = WitherVictory.velocity(90, -30);
        assertThat(climb.y()).isCloseTo(0.25, within(1e-9));
        assertThat(climb.x()).isCloseTo(-0.5 * Math.cos(Math.toRadians(30)), within(1e-9));
        assertThat(climb.length()).isCloseTo(0.5, within(1e-9));
        assertThat(WitherVictory.velocity(0, 6).y()).isNegative();
    }

    @Test
    void theRiderSteersItWithTheirLook() {
        RecordingStage stage = ground();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        Vec start = wither(stage).position;

        mount.yaw = -90;
        mount.pitch = -30;
        run(timeline, 1, 10);

        Vec now = wither(stage).position;
        assertThat(now.x() - start.x()).isCloseTo(10 * 0.5 * Math.cos(Math.toRadians(30)), within(1e-9));
        assertThat(now.y() - start.y()).isCloseTo(10 * 0.25, within(1e-9));
        assertThat(now.z()).isCloseTo(start.z(), within(1e-9));
        assertThat(mount.seat().y() - now.y()).isCloseTo(WitherVictory.SEAT_ABOVE, within(1e-9));
    }

    @Test
    void everyTwentyTicksItShootsAWitherSkullTenBlocksAheadOnTheNextTick() {
        RecordingStage stage = ground();
        Timeline timeline = script(stage, scene(winner));

        run(timeline, 0, 20);
        assertThat(count(stage, "mob.wither.shoot")).isEqualTo(1);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand"))).isEmpty();
        Vec wither = wither(stage).position;
        timeline.run(21);

        FakeActor skull = stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand")).findFirst().orElseThrow();
        assertThat(skull.skulls.get(Slot.HELMET)).isEqualTo(WitherSkull.TEXTURE);
        assertThat(skull.style).isEqualTo(WitherSkull.STYLE);
        Vec expected = wither.add(0, 0, 0.5).add(0, WitherSkull.LIFT, 10);
        assertThat(skull.position.z()).isCloseTo(expected.z(), within(1e-9));
        assertThat(skull.position.y()).isCloseTo(expected.y(), within(1e-9));
    }

    @Test
    void aSkullThatHitsNothingBlowsUpAfterEightStridesAndTheWitherKeepsFiring() {
        RecordingStage stage = ground();
        Timeline timeline = script(stage, scene(winner));

        run(timeline, 0, 28);
        assertThat(stage.particles).doesNotContainKey("EXPLOSION_HUGE");
        timeline.run(29);
        assertThat(stage.particles).containsEntry("EXPLOSION_HUGE", 1);
        assertThat(count(stage, "random.explode")).isEqualTo(1);
        run(timeline, 30, timeline.length());

        assertThat(count(stage, "mob.wither.shoot")).isEqualTo(9);
        assertThat(count(stage, "mob.wither.idle")).isEqualTo(2);
        assertThat(count(stage, "fireworks.launch")).isEqualTo(20);
        assertThat(stage.actors).allMatch(actor -> actor.removed);
    }

    @Test
    void theStridesShrinkByAFifthOfATenthEveryTick() {
        assertThat(WitherSkull.stride(0)).isEqualTo(10);
        assertThat(WitherSkull.stride(1)).isCloseTo(9.5, within(1e-9));
        assertThat(WitherSkull.stride(7)).isCloseTo(10 * Math.pow(0.95, 7), within(1e-9));
    }

    @Test
    void aSkullBlowsUpOnTheFirstBlockInItsWay() {
        RecordingStage stage = stage(block -> block[1] < 27 || block[2] >= 20);
        Timeline timeline = script(stage, scene(winner));

        run(timeline, 0, 20);
        assertThat(stage.particles).doesNotContainKey("EXPLOSION_HUGE");
        timeline.run(21);

        assertThat(stage.particles).containsEntry("EXPLOSION_HUGE", 1);
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("ArmorStand"))).allMatch(actor -> actor.removed);
    }

    @Test
    void theBlastBlightsTheBlocksAroundItIntoClayAndSandAFewAtATime() {
        RecordingStage stage = ground();
        Blight blight = new Blight(stage, new Random(4));
        Vec centre = new Vec(0.5, 28, 0.5);

        blight.spread(centre);
        blight.step();
        assertThat(stage.bulk).hasSize(Blight.PER_TICK);
        List<BlockChange> first = List.copyOf(stage.bulk);
        while (blight.spreading()) {
            blight.step();
        }
        blight.spread(centre);
        while (blight.spreading()) {
            blight.step();
        }

        Set<String> seen = new HashSet<>();
        for (BlockChange change : stage.bulk) {
            assertThat(change.material()).isIn(Material.CLAY, Material.SAND);
            assertThat(change.y()).isLessThan(27);
            assertThat(centre.distance(new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5))).isLessThanOrEqualTo(Blight.RADIUS);
            assertThat(seen.add(change.x() + "," + change.y() + "," + change.z())).isTrue();
        }
        assertThat(stage.bulk.stream().filter(change -> change.material() == Material.CLAY)).isNotEmpty();
        assertThat(stage.bulk.stream().filter(change -> change.material() == Material.SAND)).isNotEmpty();
        double nearFirst = first.stream().mapToDouble(change -> centre.distance(new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5))).average().orElseThrow();
        List<BlockChange> last = stage.bulk.subList(stage.bulk.size() - Blight.PER_TICK, stage.bulk.size());
        double farLast = last.stream().mapToDouble(change -> centre.distance(new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5))).average().orElseThrow();
        assertThat(nearFirst).isLessThan(farLast);
    }

    @Test
    void everyLivingPlayerRidesTheirOwnWither() {
        UUID other = UUID.randomUUID();
        RecordingStage stage = ground();
        stage.eyesOf.put(other, new Vec(60.5, 27 + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(1, 0, 0));
        Timeline timeline = script(stage, scene(winner, other));

        run(timeline, 0, timeline.length());

        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        assertThat(stage.mounts.get(1).start.x()).isCloseTo(60.5 - WitherVictory.SEAT_BEHIND, within(1e-9));
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("WitherBoss"))).hasSize(2);
        assertThat(count(stage, "mob.wither.shoot")).isEqualTo(18);
        assertThat(stage.actors).allMatch(actor -> actor.removed);
    }

    @Test
    void thePreviewWitherFliesStraightAheadWithoutARider() {
        RecordingStage stage = ground();
        Timeline timeline = script(stage, Scene.at(ORIGIN, new Vec(0, 0, 1)));

        run(timeline, 0, 9);

        assertThat(stage.mounts.getFirst().riders).isEmpty();
        assertThat(wither(stage).position.z()).isCloseTo(ORIGIN.z() + 5, within(1e-9));
    }
}
