package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeMount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class FestivalVictoryTest {

    private static final int FLOOR = 27;

    private final UUID winner = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void itIsTheFestivalDouradoVictoryAndLastsTwoHundredTicks() {
        Timeline timeline = script(stage(), scene(winner));

        assertThat(new FestivalVictory().id()).isEqualTo("festival-dourado");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void theRiderSitsOneBlockAboveARainbowSheepThatChangesWoolEveryFourTicks() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);

        FakeMount mount = stage.mounts.getFirst();
        FakeActor sheep = sheep(stage);
        assertThat(mount.riders).containsExactly(winner);
        assertThat(mount.start).isEqualTo(new Vec(0.5, FLOOR + 2.125, 0.5));
        assertThat(sheep.position).isEqualTo(new Vec(0.5, FLOOR + 1.125, 0.5));
        assertThat(sheep.watched).containsEntry(16, (byte) 14);

        List<Byte> wools = new ArrayList<>();
        for (int tick = 1; tick <= 28; tick++) {
            timeline.run(tick);
            if (tick % 4 == 0) {
                wools.add((Byte) sheep.watched.get(16));
            }
            assertThat(sheep.position.y()).isCloseTo(mount.seat.y() - 1, offset(1e-9));
        }
        assertThat(wools).containsExactly((byte) 1, (byte) 4, (byte) 5, (byte) 3, (byte) 11, (byte) 10, (byte) 14);
        assertThat(stage.particles.get("REDSTONE")).isEqualTo(29 * 5);
    }

    @Test
    void theGlowUnderTheSheepHasTheColourOfItsWool() {
        assertThat(FestivalVictory.wool(4)).isEqualTo(1);
        assertThat(FestivalVictory.glow(4)).containsExactly(0.847, 0.498, 0.2);
        assertThat(FestivalVictory.wool(8)).isEqualTo(4);
        assertThat(FestivalVictory.glow(8)).containsExactly(0.898, 0.898, 0.2);
        assertThat(FestivalVictory.wool(28)).isEqualTo(14);
    }

    @Test
    void holdingWFliesTheSheepWhereTheRiderLooksAndTheSheepTurnsWithIt() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        mount.pushing = true;
        mount.yaw = 90;
        for (int tick = 1; tick <= 40; tick++) {
            timeline.run(tick);
        }

        Vec step = mount.path.getLast().subtract(mount.path.get(mount.path.size() - 2));
        assertThat(step.x()).isCloseTo(-1, offset(0.11));
        assertThat(step.z()).isCloseTo(0, offset(1e-6));
        assertThat((double) sheep(stage).yaw).isCloseTo(90, offset(0.1));
    }

    @Test
    void theSheepDigsAFiveByFiveTunnelThroughEverythingItCrossesWithAPopAndADustOfSnowPerBlock() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        FakeMount mount = stage.mounts.getFirst();
        mount.pushing = true;
        mount.pitch = 60;
        int tick = 1;
        while (stage.bulk.isEmpty()) {
            timeline.run(tick++);
        }

        assertThat(stage.bulk).allSatisfy(change -> {
            assertThat(change.material()).isEqualTo(Material.AIR);
            assertThat(change.y()).isLessThan(FLOOR);
        });
        assertThat(stage.particles.get("SNOW_SHOVEL")).isEqualTo(stage.bulk.size());
        assertThat(stage.sounds).contains("random.pop");
        assertThat(stage.pitches.get(stage.sounds.indexOf("random.pop"))).isCloseTo(31.5f / 63, offset(1e-6f));
    }

    @Test
    void theTunnelIsFiveWideAndSevenTallFromOneBlockUnderTheSheepAndNeverDigsABlockTwice() {
        RecordingStage stage = new RecordingStage(block -> true);
        WoolTunnel tunnel = new WoolTunnel();

        List<BlockChange> first = tunnel.dig(stage, new Vec(10.5, 40.2, -3.5));
        List<BlockChange> again = tunnel.dig(stage, new Vec(10.5, 40.2, -3.5));
        List<BlockChange> ahead = tunnel.dig(stage, new Vec(11.5, 40.2, -3.5));

        assertThat(first).hasSize(5 * 7 * 5);
        assertThat(first).extracting(BlockChange::x).containsOnly(8, 9, 10, 11, 12);
        assertThat(first).extracting(BlockChange::y).containsOnly(39, 40, 41, 42, 43, 44, 45);
        assertThat(first).extracting(BlockChange::z).containsOnly(-6, -5, -4, -3, -2);
        assertThat(again).isEmpty();
        assertThat(ahead).hasSize(7 * 5).extracting(BlockChange::x).containsOnly(13);
    }

    @Test
    void airIsNeverDug() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 40);

        assertThat(new WoolTunnel().dig(stage, new Vec(0.5, 40.5, 0.5))).hasSize(5 * 5).extracting(BlockChange::y).containsOnly(39);
    }

    @Test
    void everyFiveTicksThirtySunflowersGoldBlocksAndDiamondsAreTossedUpInFrontOfTheRiderAndVanishAboutFourSecondsLater() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        timeline.run(0);
        assertThat(items(stage)).isEmpty();
        timeline.run(1);

        List<FakeActor> wave = items(stage);
        assertThat(wave).hasSize(30);
        assertThat(wave.subList(0, 4)).extracting(actor -> actor.type)
            .containsExactly("Item:DOUBLE_PLANT", "Item:GOLD_BLOCK", "Item:GOLD_BLOCK", "Item:DIAMOND");
        Vec body = sheep(stage).position;
        assertThat(wave).allSatisfy(item -> {
            Vec offset = item.position.subtract(body);
            double distance = Math.hypot(offset.x(), offset.z());
            assertThat(distance).isBetween(FestivalVictory.NEAREST - 1e-9, FestivalVictory.FARTHEST + 1e-9);
            assertThat(offset.z()).isGreaterThan(0);
            assertThat(Math.toDegrees(Math.abs(Math.atan2(offset.x(), offset.z())))).isLessThanOrEqualTo(FestivalVictory.WIDEST);
            assertThat(offset.y()).isBetween(FestivalVictory.RISE - 1e-9, FestivalVictory.RISE + FestivalVictory.RISE_SPREAD);
        });
        for (int tick = 2; tick <= 6; tick++) {
            timeline.run(tick);
        }
        assertThat(items(stage)).hasSize(60);
        for (int tick = 7; tick <= 1 + FestivalVictory.SHORTEST_LIFE - 1; tick++) {
            timeline.run(tick);
        }
        assertThat(wave).noneMatch(item -> item.removed);
        for (int tick = 1 + FestivalVictory.SHORTEST_LIFE; tick <= 1 + FestivalVictory.LONGEST_LIFE; tick++) {
            timeline.run(tick);
        }
        assertThat(wave).allMatch(item -> item.removed);
    }

    @Test
    void theTreasuresRiseWithThePitchLowerAndAreTossedUpAtATenthOfABlockSideways() {
        Vec level = FestivalVictory.spot(Vec.ZERO, 0, 0, 20, 0, 1);
        Vec down = FestivalVictory.spot(Vec.ZERO, 90, 30, 20, 0, 0);
        Vec toss = FestivalVictory.toss(new Random(3));

        assertThat(level.x()).isCloseTo(0, offset(1e-9));
        assertThat(level.y()).isCloseTo(FestivalVictory.RISE + 1, offset(1e-9));
        assertThat(level.z()).isCloseTo(20, offset(1e-9));
        assertThat(down.x()).isCloseTo(-20, offset(1e-9));
        assertThat(down.y()).isCloseTo(FestivalVictory.RISE - FestivalVictory.RISE_PER_PITCH / 2, offset(1e-9));
        assertThat(toss.y()).isEqualTo(0.2);
        assertThat(Math.abs(toss.x())).isLessThanOrEqualTo(0.1);
        assertThat(Math.abs(toss.z())).isLessThanOrEqualTo(0.1);
    }

    @Test
    void everyRiderGetsTheirOwnSheepAndTheTreasuresAreSharedBetweenThem() {
        RecordingStage stage = stage();
        stage.eyesOf.put(other, new Vec(60.5, FLOOR + Riders.EYE_HEIGHT, -40.5));
        stage.lookOf.put(other, new Vec(1, 0, 0));
        Timeline timeline = script(stage, scene(winner, other));
        timeline.run(0);
        timeline.run(1);

        assertThat(stage.mounts).extracting(mount -> mount.riders).containsExactly(List.of(winner), List.of(other));
        assertThat(stage.actors.stream().filter(actor -> actor.type.equals("Sheep"))).hasSize(2);
        assertThat(items(stage)).hasSize(30);
        assertThat(items(stage).subList(15, 30)).allSatisfy(item -> assertThat(Math.hypot(item.position.x() - 60.5, item.position.z() + 40.5))
            .isBetween(FestivalVictory.NEAREST - 1e-9, FestivalVictory.FARTHEST + 1e-9));
        assertThat(FestivalVictory.perRide(4)).isEqualTo(7);
        assertThat(FestivalVictory.perRide(40)).isEqualTo(1);
    }

    @Test
    void fireworksLaunchEveryTenTicksAndEverythingIsGoneAtTheEnd() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene(winner));
        for (int tick = 0; tick <= timeline.length(); tick++) {
            timeline.run(tick);
        }

        assertThat(stage.sounds.stream().filter("fireworks.launch"::equals).count()).isEqualTo(20);
        assertThat(items(stage)).hasSize(40 * 30);
        assertThat(stage.alive()).isZero();
    }

    @Test
    void thePreviewWithoutRidersStillHoversASheepAndShowersTreasure() {
        RecordingStage stage = stage();
        Timeline timeline = script(stage, scene());
        for (int tick = 0; tick <= 10; tick++) {
            timeline.run(tick);
        }

        assertThat(stage.mounts).hasSize(1);
        assertThat(stage.mounts.getFirst().riders).isEmpty();
        assertThat(items(stage)).hasSize(60);
    }

    private static FakeActor sheep(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.equals("Sheep")).findFirst().orElseThrow();
    }

    private static List<FakeActor> items(RecordingStage stage) {
        return stage.actors.stream().filter(actor -> actor.type.startsWith("Item:")).toList();
    }

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < FLOOR);
    }

    private static Timeline script(RecordingStage stage, Scene scene) {
        Timeline timeline = new Timeline();
        new FestivalVictory().script(scene, stage, timeline, new Random(7));
        return timeline;
    }

    private static Scene scene(UUID... riders) {
        return new Scene(new Vec(0.5, FLOOR, 0.5), new Vec(0, 0, 1), Optional.empty(), List.of(riders));
    }
}
