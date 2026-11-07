package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class MapWaveTest {

    private static final Vec ORIGIN = new Vec(5.5, 27, -3.5);
    private static final int ORIGIN_X = 5;
    private static final int ORIGIN_Z = -4;

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < 27);
    }

    private static MapWave wave(RecordingStage stage) {
        return new MapWave(stage, new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), List.of()),
            (x, y, z, type) -> type.material() == Material.STONE ? new BlockType(Material.NETHERRACK, 0) : type, new Random(1));
    }

    private static Set<String> columns(List<BlockChange> changes) {
        return changes.stream().map(change -> change.x() + "," + change.z()).collect(Collectors.toSet());
    }

    @Test
    void theBoxCoversTheMapAndAtLeastASquareAroundTheWinner() {
        assertThat(MapWave.box(new Area(-100, -20, 30, 90), 0, 0)).containsExactly(-100, -64, 64, 90);
    }

    @Test
    void columnsAreOrderedByManhattanDistanceFromTheWinner() {
        List<int[]> columns = MapWave.order(new int[] {-3, -3, 3, 3}, 0, 0, new Random(2));

        assertThat(columns).hasSize(49);
        for (int index = 1; index < columns.size(); index++) {
            assertThat(MapWave.distance(columns.get(index)[0], columns.get(index)[1], 0, 0))
                .isGreaterThanOrEqualTo(MapWave.distance(columns.get(index - 1)[0], columns.get(index - 1)[1], 0, 0));
        }
    }

    @Test
    void eachStepConvertsTheNextTwoHundredFiftySixColumnsWholeFromTheBottomToHeightOneHundredTwentySeven() {
        RecordingStage stage = stage();
        MapWave wave = wave(stage);

        wave.step();
        List<BlockChange> first = List.copyOf(stage.bulk);
        int reached = wave.front();
        wave.step();
        List<BlockChange> second = stage.bulk.subList(first.size(), stage.bulk.size());

        assertThat(columns(first)).hasSize(MapWave.COLUMNS_PER_TICK);
        assertThat(columns(second)).hasSize(MapWave.COLUMNS_PER_TICK).doesNotContainAnyElementsOf(columns(first));
        assertThat(first).allMatch(change -> MapWave.distance(change.x(), change.z(), ORIGIN_X, ORIGIN_Z) <= reached);
        assertThat(second).allMatch(change -> MapWave.distance(change.x(), change.z(), ORIGIN_X, ORIGIN_Z) >= reached);
        assertThat(first).allMatch(change -> change.material() == Material.NETHERRACK && change.y() >= MapWave.BOTTOM && change.y() < 27);
        assertThat(first.stream().filter(change -> change.x() == ORIGIN_X && change.z() == ORIGIN_Z)).hasSize(27);
    }

    @Test
    void thePaletteSeesWhereEachBlockIs() {
        RecordingStage stage = stage();
        MapWave wave = new MapWave(stage, new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), List.of()),
            (x, y, z, type) -> y == 10 ? new BlockType(Material.GLASS, 0) : type, new Random(1));

        wave.step();

        assertThat(stage.bulk).isNotEmpty().allMatch(change -> change.y() == 10 && change.material() == Material.GLASS);
    }

    @Test
    void heldBlocksAreLeftAlone() {
        RecordingStage stage = stage();
        MapWave wave = wave(stage);

        wave.hold(ORIGIN_X, 26, ORIGIN_Z);
        wave.step();

        assertThat(stage.bulk).noneMatch(change -> change.x() == ORIGIN_X && change.y() == 26 && change.z() == ORIGIN_Z);
        assertThat(stage.bulk).anyMatch(change -> change.x() == ORIGIN_X && change.y() == 25 && change.z() == ORIGIN_Z);
    }

    @Test
    void theWaveIsDoneOnceEveryColumnOfTheBoxIsConverted() {
        RecordingStage stage = stage();
        MapWave wave = wave(stage);
        int[] box = MapWave.box(Area.around(ORIGIN, Scene.REACH), ORIGIN_X, ORIGIN_Z);
        int columns = (box[2] - box[0] + 1) * (box[3] - box[1] + 1);
        int ticks = (columns + MapWave.COLUMNS_PER_TICK - 1) / MapWave.COLUMNS_PER_TICK;

        for (int tick = 1; tick < ticks; tick++) {
            wave.step();
        }
        assertThat(wave.done()).isFalse();
        wave.step();

        assertThat(wave.done()).isTrue();
        assertThat(wave.front()).isEqualTo(2 * MapWave.HALF_SQUARE);
    }
}
