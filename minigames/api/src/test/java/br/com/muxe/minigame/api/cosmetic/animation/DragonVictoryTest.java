package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
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

class DragonVictoryTest {

    private static final double PRECISION = 1e-9;
    private static final Predicate<int[]> FLAT = block -> block[1] < 27;
    private static final Predicate<int[]> WALL = block -> block[1] < 27 || block[2] >= 6 && block[2] <= 9 && block[1] < 45;

    private final UUID winner = UUID.randomUUID();

    private Scene scene(UUID... riders) {
        return new Scene(new Vec(0.5, 27, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(riders));
    }

    private Timeline script(RecordingStage stage, Scene scene) {
        Timeline timeline = new Timeline();
        new DragonVictory().script(scene, stage, timeline, new Random(3));
        return timeline;
    }

    private static void run(Timeline timeline, int from, int until) {
        for (int tick = from; tick <= until; tick++) {
            timeline.run(tick);
        }
    }

    private static FakeActor dragon(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("EnderDragon")).findFirst().orElseThrow();
    }

    @Test
    void itIsTheDragaoEffectAndLastsTwoHundredTicks() {
        Timeline timeline = script(new RecordingStage(FLAT), scene(winner));

        assertThat(new DragonVictory().id()).isEqualTo("dragao");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void theDragonAppearsFiveBlocksAboveTheWinnerWithTheirSeatSixBlocksAboveIt() {
        RecordingStage stage = new RecordingStage(FLAT);
        Timeline timeline = script(stage, scene(winner));

        timeline.run(0);

        assertThat(stage.actors).hasSize(1);
        assertThat(dragon(stage).position).isEqualTo(new Vec(0.5, 32, 0.5));
        assertThat(stage.mounts).hasSize(1);
        assertThat(stage.mounts.getFirst().riders).containsExactly(winner);
        assertThat(stage.mounts.getFirst().seat()).isEqualTo(new Vec(0.5, 38, 0.5));
    }

    @Test
    void theDragonFliesOneBlockATickWhereTheRiderLooksWithoutHoldingForward() {
        RecordingStage stage = new RecordingStage(FLAT);
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        mount.yaw = 90;

        run(timeline, 1, 10);

        FakeActor dragon = dragon(stage);
        assertThat(mount.pushing).isFalse();
        assertThat(dragon.position.x()).isCloseTo(-9.5, within(PRECISION));
        assertThat(dragon.position.y()).isCloseTo(32, within(PRECISION));
        assertThat(dragon.position.z()).isCloseTo(0.5, within(PRECISION));
        assertThat(dragon.yaw).isEqualTo(270);
        assertThat(mount.seat()).isEqualTo(dragon.position.add(0, 6, 0));
    }

    @Test
    void strafingTurnsTheDragonAQuarterToThatSide() {
        RecordingStage stage = new RecordingStage(FLAT);
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        stage.mounts.getFirst().strafing = 1;

        run(timeline, 1, 4);

        assertThat(DragonRide.heading(0, 1)).isEqualTo(-90);
        assertThat(DragonRide.heading(30, -1)).isEqualTo(120);
        assertThat(dragon(stage).position.x()).isCloseTo(4.5, within(PRECISION));
        assertThat(dragon(stage).position.z()).isCloseTo(0.5, within(PRECISION));
    }

    @Test
    void onlyASteepLookMakesTheDragonClimbOrDive() {
        assertThat(DragonRide.climb(20)).isZero();
        assertThat(DragonRide.climb(-22)).isZero();
        assertThat(DragonRide.climb(30)).isEqualTo(30);
        assertThat(DragonRide.climb(-40)).isEqualTo(-40);

        RecordingStage stage = new RecordingStage(FLAT);
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        stage.mounts.getFirst().pitch = 30;
        run(timeline, 1, 2);

        assertThat(dragon(stage).position.y()).isCloseTo(32 - 2 * Math.sin(Math.toRadians(30)), within(PRECISION));
        assertThat(dragon(stage).position.z()).isCloseTo(0.5 + 2 * Math.cos(Math.toRadians(30)), within(PRECISION));
    }

    @Test
    void theDragonStaysInsideTheWorldHeight() {
        assertThat(DragonRide.step(new Vec(0, 1.5, 0), 0, 90).y()).isEqualTo(1);
        assertThat(DragonRide.step(new Vec(0, 239.5, 0), 0, -90).y()).isEqualTo(240);
    }

    @Test
    void theDragonDigsATunnelThroughEveryBlockItTouchesOnlyOnce() {
        RecordingStage stage = new RecordingStage(WALL);
        Timeline timeline = script(stage, scene(winner));

        run(timeline, 0, 12);

        assertThat(stage.bulk).isNotEmpty().allMatch(change -> change.material() == Material.AIR);
        Set<String> dug = new HashSet<>();
        for (BlockChange change : stage.bulk) {
            assertThat(dug.add(change.x() + "," + change.y() + "," + change.z())).isTrue();
            assertThat(change.x()).isBetween(-3, 3);
            assertThat(change.y()).isBetween(32, 40);
            assertThat(change.z()).isBetween(6, 9);
        }
        assertThat(dug).hasSize(7 * 9 * 4);
    }

    @Test
    void someDugBlocksCrumbleAndOthersAreThrownOutwardTwoTicksLater() {
        RecordingStage stage = new RecordingStage(WALL);
        Timeline timeline = script(stage, scene(winner));

        run(timeline, 0, 199);

        assertThat(stage.breakEffects).isNotEmpty().allMatch(effect -> effect.material() == Material.STONE)
            .allMatch(effect -> stage.bulk.stream().anyMatch(change -> change.x() == effect.x() && change.y() == effect.y() && change.z() == effect.z()));
        assertThat(stage.breakEffects.size()).isLessThan(stage.bulk.size() / 6);
        List<FakeActor> debris = stage.actors.stream().filter(actor -> actor.type.equals("FallingBlock:STONE:0")).toList();
        assertThat(debris.size()).isBetween(stage.bulk.size() / 12, stage.bulk.size() / 3);
        assertThat(debris).allMatch(actor -> actor.removed);
    }

    @Test
    void thrownBlocksFlyAwayFromTheDragonAndUp() {
        Vec velocity = Tunnel.launch(new Vec(0, 32, 0), new Vec(3, 32, 4));

        assertThat(velocity.x()).isCloseTo(1.2, within(PRECISION));
        assertThat(velocity.y()).isEqualTo(1);
        assertThat(velocity.z()).isCloseTo(1.6, within(PRECISION));
    }

    @Test
    void aThrownBlockFallsBackAndDisappearsWhenItLands() {
        RecordingStage stage = new RecordingStage(FLAT);
        Debris debris = Debris.launch(stage, new Vec(0.5, 32, 0.5), new BlockType(Material.STONE, 0), new Vec(0, 1, 0));
        int ticks = 0;

        while (!debris.step(stage::solid)) {
            ticks++;
        }

        assertThat(ticks).isBetween(20, 99);
        assertThat(debris.position().y()).isLessThan(27);
        assertThat(stage.actors.getFirst().type).isEqualTo("FallingBlock:STONE:0");
        assertThat(stage.alive()).isZero();
    }

    @Test
    void theDragonGrowlsNowAndThenAndEverythingIsGoneAtTheEnd() {
        RecordingStage stage = new RecordingStage(WALL);
        Timeline timeline = script(stage, scene(winner));

        run(timeline, 0, timeline.length());

        assertThat(stage.sounds).allMatch("mob.enderdragon.growl"::equals);
        assertThat(stage.sounds).hasSizeBetween(1, 4);
        assertThat(stage.pitches).allMatch(pitch -> pitch >= 0.8f && pitch <= 1.2f);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void everyLivingPlayerRidesTheirOwnDragon() {
        UUID other = UUID.randomUUID();
        RecordingStage stage = new RecordingStage(FLAT);
        stage.eyesOf.put(other, new Vec(60.5, 27 + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(1, 0, 0));
        Timeline timeline = script(stage, scene(winner, other));

        run(timeline, 0, timeline.length());

        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        assertThat(stage.mounts.get(1).start).isEqualTo(new Vec(60.5, 38, -40.5));
        assertThat(stage.actors).hasSize(2).allMatch(actor -> actor.type.equals("EnderDragon") && actor.removed);
    }
}
