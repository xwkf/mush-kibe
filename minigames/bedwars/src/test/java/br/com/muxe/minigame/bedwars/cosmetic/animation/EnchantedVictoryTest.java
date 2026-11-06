package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.RecordingStage.FakeActor;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class EnchantedVictoryTest {

    private static final Predicate<int[]> FLAT = block -> block[1] < 27;
    private static final Vec ANCHOR = new Vec(0.5, 27, -6.5);

    private static Scene scene() {
        return Scene.at(new Vec(0.5, 27, 0.5), new Vec(0, 0, -1));
    }

    private static Timeline play(RecordingStage stage, int until) {
        Timeline timeline = new Timeline();
        new EnchantedVictory().script(scene(), stage, timeline, new Random(11));
        for (int tick = 0; tick <= until; tick++) {
            timeline.run(tick);
        }
        return timeline;
    }

    private static double reach(BlockChange change) {
        return Math.hypot(change.x() + 0.5 - ANCHOR.x(), change.z() + 0.5 - ANCHOR.z());
    }

    @Test
    void itIsTheEncantadoEffectAndLastsTwoHundredTicks() {
        Timeline timeline = new Timeline();
        new EnchantedVictory().script(scene(), new RecordingStage(FLAT), timeline, new Random(1));

        assertThat(new EnchantedVictory().id()).isEqualTo("encantado");
        assertThat(timeline.length()).isEqualTo(200);
    }

    @Test
    void everythingGrowsAroundTheSpotSevenBlocksAheadOfTheWinner() {
        assertThat(EnchantedVictory.anchor(scene())).isEqualTo(ANCHOR);
    }

    @Test
    void aSmallMushroomPopsOutOfTheFloorEveryTwoTicksWithDirtDust() {
        RecordingStage stage = new RecordingStage(FLAT);

        play(stage, 1);

        assertThat(stage.bulk).hasSize(1);
        BlockChange small = stage.bulk.getFirst();
        assertThat(small.material()).isIn(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM);
        assertThat(small.y()).isEqualTo(27);
        assertThat(reach(small)).isLessThanOrEqualTo(EnchantedVictory.MUSHROOM_RADIUS + 1);
        assertThat(stage.particleData.get("BLOCK_DUST")).containsExactly(Kit.blockId(Material.DIRT, 0));
        assertThat(stage.sounds).containsExactly("random.pop");
        assertThat(stage.pitches.getFirst()).isBetween(1.2f, 1.6f);
    }

    @Test
    void sixtySmallMushroomsAreTriedAndAllStayNearTheSpot() {
        RecordingStage stage = new RecordingStage(FLAT);

        play(stage, 199);

        List<BlockChange> smalls = stage.bulk.stream().filter(change -> change.material() == Material.RED_MUSHROOM || change.material() == Material.BROWN_MUSHROOM).toList();
        assertThat(smalls.size()).isBetween(50, 60);
        assertThat(smalls).allMatch(change -> reach(change) <= EnchantedVictory.MUSHROOM_RADIUS + 1);
        assertThat(smalls.stream().map(BlockChange::material).distinct()).hasSize(2);
    }

    @Test
    void aBrownGiantHasItsStemAndAFlatCapWithoutCorners() {
        List<List<BlockChange>> layers = GiantMushroom.layers(0, 27, 0, 5, false);

        assertThat(layers).hasSize(6);
        for (int up = 0; up < 5; up++) {
            assertThat(layers.get(up)).containsExactly(new BlockChange(0, 27 + up, 0, Material.HUGE_MUSHROOM_2, 10));
        }
        assertThat(layers.get(5)).hasSize(45).allMatch(block -> block.y() == 32 && block.material() == Material.HUGE_MUSHROOM_1 && block.data() == 14)
            .noneMatch(block -> Math.abs(block.x()) == 3 && Math.abs(block.z()) == 3);
        assertThat(layers.get(5).getLast()).isEqualTo(new BlockChange(3, 32, 2, Material.HUGE_MUSHROOM_1, 14));
    }

    @Test
    void aRedGiantHasThreeRingsAroundTheTopOfItsStemAndASquareOnTop() {
        List<List<BlockChange>> layers = GiantMushroom.layers(0, 27, 0, 5, true);

        assertThat(layers).hasSize(9);
        for (int ring = 0; ring < 3; ring++) {
            int y = 29 + ring;
            assertThat(layers.get(5 + ring)).hasSize(12).allMatch(block -> block.y() == y && block.material() == Material.HUGE_MUSHROOM_2 && block.data() == 14)
                .allMatch(block -> Math.max(Math.abs(block.x()), Math.abs(block.z())) == 2);
        }
        assertThat(layers.get(8)).hasSize(9).allMatch(block -> block.y() == 32 && Math.abs(block.x()) <= 1 && Math.abs(block.z()) <= 1);
    }

    @Test
    void aGiantGrowsOneLayerTwoTicksAfterTheFirstThenEveryThreeTicksWithARisingSound() {
        assertThat(List.of(0, 1, 2, 3, 4, 5).stream().map(GiantMushroom::delay)).containsExactly(0, 2, 5, 8, 11, 14);
        assertThat(GiantMushroom.pitch(0)).isCloseTo(0.6f, within(1e-6f));
        assertThat(GiantMushroom.pitch(5)).isCloseTo(1.2f, within(1e-6f));
    }

    @Test
    void tenGiantsGrowBlockByBlockAndRingTheBellWhenTheirCapIsDone() {
        RecordingStage stage = new RecordingStage(FLAT);

        play(stage, 199);

        long stems = stage.bulk.stream().filter(change -> change.data() == 10).map(change -> change.x() + "," + change.z()).distinct().count();
        assertThat(stems).isBetween(8L, 10L);
        assertThat(stage.bulk.stream().filter(change -> change.data() == 10 || change.data() == 14)).allMatch(change -> reach(change) <= EnchantedVictory.MUSHROOM_RADIUS + 4);
        long bells = stage.sounds.stream().filter("note.pling"::equals).count();
        assertThat(bells).isBetween(8L, 10L);
        assertThat(stage.particles.get("FIREWORKS_SPARK")).isEqualTo((int) bells);
        assertThat(stage.sounds.stream().filter("dig.grass"::equals).count()).isGreaterThan(50);
        assertThat(stage.particles.get("VILLAGER_HAPPY")).isGreaterThan(300);
    }

    @Test
    void aGiantNeverReplacesWhatIsAlreadyThere() {
        RecordingStage stage = new RecordingStage(FLAT);
        Grove grove = new Grove(stage, ANCHOR);
        grove.grow(List.of(new BlockChange(0, 29, 0, Material.RED_MUSHROOM, 0)));
        GiantMushroom mushroom = new GiantMushroom(0, 27, 0, 4, false);

        for (int step = 0; step < mushroom.steps(); step++) {
            mushroom.grow(stage, grove, step);
        }

        assertThat(grove.at(0, 29, 0)).isEqualTo(Material.RED_MUSHROOM);
        assertThat(grove.at(0, 30, 0)).isEqualTo(Material.HUGE_MUSHROOM_2);
        assertThat(grove.at(0, 31, 0)).isEqualTo(Material.HUGE_MUSHROOM_1);
        assertThat(grove.solid(0, 31, 0)).isTrue();
        assertThat(grove.solid(0, 29, 0)).isFalse();
    }

    @Test
    void fifteenGnomesArriveInAPuffEveryThreeTicksNearTheSpot() {
        RecordingStage stage = new RecordingStage(FLAT);
        Timeline timeline = new Timeline();
        new EnchantedVictory().script(scene(), stage, timeline, new Random(11));

        for (int tick = 0; tick <= 199; tick++) {
            timeline.run(tick);
            long arrived = stage.actors.stream().filter(actor -> actor.type.equals("Player")).count();
            assertThat(arrived).as("tick %d", tick).isEqualTo(tick < 6 ? 0 : Math.min(15, (tick - 6) / 3 + 1));
            assertThat(stage.particles.getOrDefault("CLOUD", 0)).as("tick %d", tick).isEqualTo(tick < 5 ? 0 : Math.min(15, (tick - 5) / 3 + 1));
        }

        List<FakeActor> gnomes = stage.actors.stream().filter(actor -> actor.type.equals("Player")).toList();
        assertThat(stage.skins).hasSize(15).allMatch(Gnome.SKIN::equals);
        assertThat(stage.sounds.stream().filter("mob.chicken.plop"::equals).count()).isEqualTo(15);
        assertThat(gnomes).allMatch(gnome -> gnome.position.y() >= 27);
    }

    @Test
    void aGnomeWalksStraightUntilItMeetsAWallOrAnEdgeAndThenTurns() {
        RecordingStage stage = new RecordingStage(block -> block[1] < 27 && block[0] < 5 || block[1] == 27 && block[0] == -5);
        Gnome gnome = Gnome.spawn(stage, new Vec(0.5, 27, 0.5), -90);
        Random random = new Random(4);

        for (int tick = 0; tick < 200; tick++) {
            gnome.walk(stage::solid, random);
            assertThat(Gnome.blocked(stage::solid, gnome.position())).isFalse();
        }

        assertThat(Gnome.blocked(stage::solid, new Vec(5.2, 27, 0.5))).isTrue();
        assertThat(Gnome.blocked(stage::solid, new Vec(-4.5, 27, 0.5))).isTrue();
        assertThat(gnome.position().y()).isEqualTo(27);
        assertThat(stage.actors.getFirst().position).isEqualTo(gnome.position());
    }

    @Test
    void aGnomeWalksAQuarterBlockATick() {
        RecordingStage stage = new RecordingStage(FLAT);
        Gnome gnome = Gnome.spawn(stage, new Vec(0.5, 27, 0.5), 0);

        for (int tick = 0; tick < 4; tick++) {
            gnome.walk(stage::solid, new Random(1));
        }

        assertThat(gnome.position().z()).isCloseTo(0.5 + 4 * 0.225, within(1e-9));
        assertThat(gnome.yaw()).isZero();
    }

    @Test
    void theGnomesLeaveAtTheEndButTheForestStays() {
        RecordingStage stage = new RecordingStage(FLAT);

        Timeline timeline = play(stage, 200);

        assertThat(timeline.length()).isEqualTo(200);
        assertThat(stage.alive()).isZero();
        assertThat(stage.bulk).isNotEmpty();
    }

    @Test
    void nothingGrowsOverTheVoid() {
        RecordingStage stage = new RecordingStage(block -> false);

        play(stage, 200);

        assertThat(stage.bulk).isEmpty();
        assertThat(stage.actors).isEmpty();
        assertThat(stage.sounds).isEmpty();
    }
}
