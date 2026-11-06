package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class MapWaveTest {

    private static final Vec ORIGIN = new Vec(5.5, 27, -3.5);
    private static final int ORIGIN_X = 5;
    private static final int ORIGIN_Z = -4;

    private static RecordingStage stage() {
        return new RecordingStage(block -> block[1] < 27);
    }

    private static MapWave wave(RecordingStage stage, MapWave.Speed speed) {
        return new MapWave(stage, new Scene(ORIGIN, new Vec(0, 0, 1), Optional.empty(), List.of()), speed,
            type -> type.material() == Material.STONE ? new BlockType(Material.NETHERRACK, 0) : type);
    }

    @Test
    void theBoxCoversTheMapAndAtLeastASquareAroundTheWinner() {
        assertThat(MapWave.box(new Area(-100, -20, 30, 90), 0, 0)).containsExactly(-100, -64, 64, 90);
    }

    @Test
    void columnsAreOrderedByManhattanDistanceFromTheWinner() {
        List<int[]> columns = MapWave.order(new int[] {-3, -3, 3, 3}, 0, 0);

        assertThat(columns).hasSize(49);
        for (int index = 1; index < columns.size(); index++) {
            assertThat(MapWave.distance(columns.get(index)[0], columns.get(index)[1], 0, 0))
                .isGreaterThanOrEqualTo(MapWave.distance(columns.get(index - 1)[0], columns.get(index - 1)[1], 0, 0));
        }
    }

    @Test
    void theFrontGrowsLinearlyFromItsStartingRadius() {
        MapWave.Speed speed = new MapWave.Speed(20, 1.5);

        assertThat(speed.radius(1)).isEqualTo(21);
        assertThat(speed.radius(2)).isEqualTo(23);
        assertThat(speed.radius(100)).isEqualTo(170);
    }

    @Test
    void eachStepConvertsEveryColumnTheFrontReachedOnlyOnce() {
        RecordingStage stage = stage();
        MapWave wave = wave(stage, new MapWave.Speed(2, 1));

        wave.step(1);
        List<BlockChange> first = List.copyOf(stage.bulk);
        wave.step(2);
        List<BlockChange> second = stage.bulk.subList(first.size(), stage.bulk.size());

        assertThat(first).allMatch(change -> MapWave.distance(change.x(), change.z(), ORIGIN_X, ORIGIN_Z) <= 3);
        assertThat(first.stream().map(change -> change.x() + "," + change.z()).distinct()).hasSize(25);
        assertThat(second).allMatch(change -> MapWave.distance(change.x(), change.z(), ORIGIN_X, ORIGIN_Z) == 4);
        assertThat(first).allMatch(change -> change.material() == Material.NETHERRACK && change.y() < 27 && change.y() >= 27 - MapWave.SCAN_BELOW);
    }

    @Test
    void heldBlocksAreLeftAlone() {
        RecordingStage stage = stage();
        MapWave wave = wave(stage, new MapWave.Speed(0, 1));

        wave.hold(ORIGIN_X, 26, ORIGIN_Z);
        wave.step(0);

        assertThat(stage.bulk).noneMatch(change -> change.x() == ORIGIN_X && change.y() == 26 && change.z() == ORIGIN_Z);
        assertThat(stage.bulk).anyMatch(change -> change.x() == ORIGIN_X && change.y() == 25 && change.z() == ORIGIN_Z);
    }

    @Test
    void theWaveIsDoneOnceEveryColumnIsConverted() {
        RecordingStage stage = stage();
        MapWave wave = wave(stage, new MapWave.Speed(1000, 0));

        assertThat(wave.done()).isFalse();
        wave.step(1);

        assertThat(wave.done()).isTrue();
    }
}
