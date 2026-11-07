package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.minigame.api.cosmetic.animation.FrostRules.Blobs;
import br.com.muxe.minigame.api.cosmetic.animation.FrostRules.Column;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class FrostRulesTest {

    private static final int SIDE = 64;

    private static Optional<BlockType> frozen(Material material, boolean packed) {
        return FrostRules.frozen(new BlockType(material, 0), packed);
    }

    private static double sameAt(Blobs blobs, int dx, int dy, int dz) {
        int same = 0;
        int pairs = 0;
        for (int x = 0; x < SIDE; x++) {
            for (int y = 0; y < SIDE; y++) {
                for (int z = 0; z < SIDE; z++) {
                    if (blobs.packed(x, y, z)) {
                        pairs++;
                        same += blobs.packed(x + dx, y + dy, z + dz) ? 1 : 0;
                    }
                }
            }
        }
        return (double) same / pairs;
    }

    @Test
    void fullBlocksTurnIntoSnowOrPackedIce() {
        assertThat(frozen(Material.WOOL, false)).contains(FrostRules.SNOW);
        assertThat(frozen(Material.OBSIDIAN, false)).contains(FrostRules.SNOW);
        assertThat(frozen(Material.STAINED_CLAY, true)).contains(FrostRules.PACKED_ICE);
        assertThat(frozen(Material.QUARTZ_BLOCK, true)).contains(FrostRules.PACKED_ICE);
        assertThat(frozen(Material.SEA_LANTERN, false)).contains(FrostRules.SNOW);
        assertThat(frozen(Material.DOUBLE_STEP, true)).contains(FrostRules.PACKED_ICE);
        assertThat(frozen(Material.GLOWSTONE, true)).contains(FrostRules.PACKED_ICE);
    }

    @Test
    void glassPanesStairsSlabsChestsAndWaterTurnIntoClearIce() {
        assertThat(frozen(Material.STAINED_GLASS, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.GLASS, true)).contains(FrostRules.ICE);
        assertThat(frozen(Material.STAINED_GLASS_PANE, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.THIN_GLASS, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.SMOOTH_STAIRS, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.QUARTZ_STAIRS, true)).contains(FrostRules.ICE);
        assertThat(frozen(Material.STEP, true)).contains(FrostRules.ICE);
        assertThat(frozen(Material.WOOD_STEP, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.CHEST, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.ENDER_CHEST, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.STATIONARY_WATER, false)).contains(FrostRules.ICE);
    }

    @Test
    void carpetsButtonsHeadsAndPlantsVanish() {
        assertThat(frozen(Material.CARPET, false)).contains(FrostRules.AIR);
        assertThat(frozen(Material.STONE_BUTTON, false)).contains(FrostRules.AIR);
        assertThat(frozen(Material.SKULL, false)).contains(FrostRules.AIR);
        assertThat(frozen(Material.RED_ROSE, false)).contains(FrostRules.AIR);
        assertThat(frozen(Material.LONG_GRASS, false)).contains(FrostRules.AIR);
    }

    @Test
    void airBarriersBedsLavaAndFrozenBlocksStay() {
        assertThat(frozen(Material.AIR, true)).isEmpty();
        assertThat(frozen(Material.BARRIER, true)).isEmpty();
        assertThat(frozen(Material.BED_BLOCK, false)).isEmpty();
        assertThat(frozen(Material.STATIONARY_LAVA, false)).isEmpty();
        assertThat(frozen(Material.SNOW_BLOCK, false)).isEmpty();
        assertThat(frozen(Material.PACKED_ICE, true)).isEmpty();
        assertThat(frozen(Material.ICE, false)).isEmpty();
    }

    @Test
    void packedIceFillsTheManhattanCellsOfAboutOneSeedInSevenAsInTheRecording() {
        Blobs blobs = new Blobs(42);
        int packed = 0;
        for (int x = 0; x < SIDE; x++) {
            for (int y = 0; y < SIDE; y++) {
                for (int z = 0; z < SIDE; z++) {
                    packed += blobs.packed(x, y, z) ? 1 : 0;
                }
            }
        }

        assertThat(packed / (double) (SIDE * SIDE * SIDE)).isBetween(0.1, 0.2);
        assertThat(sameAt(blobs, 1, 0, 0)).isBetween(0.76, 0.9);
        assertThat(sameAt(blobs, 0, 1, 0)).isBetween(0.76, 0.9);
        assertThat(sameAt(blobs, 0, 0, 4)).isBetween(0.33, 0.55);
        assertThat(sameAt(blobs, 10, 0, 0)).isLessThan(0.25);
        assertThat(new Blobs(42).packed(13, 7, -21)).isEqualTo(blobs.packed(13, 7, -21));
    }

    @Test
    void cellsHoldAboutOneSeedPerTwoHundredSeventyFiveBlocks() {
        Blobs blobs = new Blobs(7);
        int seeds = 0;
        int cells = 0;
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                for (int z = 0; z < 20; z++) {
                    seeds += blobs.seeds(x, y, z);
                    cells++;
                }
            }
        }
        double perCell = FrostRules.CELL * FrostRules.CELL * FrostRules.CELL / Math.pow(FrostRules.SEED_SPACING, 3);

        assertThat((double) seeds / cells).isBetween(perCell * 0.9, perCell * 1.1);
    }

    @Test
    void theFrostSpreadsInManhattanRingsFromTheWinnerWithShuffledTies() {
        List<Column> order = FrostRules.order(new Area(-50, -50, 49, 49), 10, -5, new Random(1));

        assertThat(order).hasSize(100 * 100);
        assertThat(order.getFirst()).isEqualTo(new Column(10, -5, 0));
        for (int index = 1; index < order.size(); index++) {
            assertThat(order.get(index).reach()).isGreaterThanOrEqualTo(order.get(index - 1).reach());
            assertThat(order.get(index).reach()).isEqualTo(FrostRules.distance(order.get(index).x(), order.get(index).z(), 10, -5));
        }
        Set<List<Column>> firstRings = new HashSet<>();
        for (int seed = 0; seed < 6; seed++) {
            firstRings.add(List.copyOf(FrostRules.order(new Area(-50, -50, 49, 49), 10, -5, new Random(seed)).subList(1, 5)));
        }
        assertThat(firstRings).hasSizeGreaterThan(1);
    }

    @Test
    void theRecordedBoxFreezesTwoHundredFiftyColumnsATickInNinetyEightTicks() {
        int recorded = 157 * 157;

        assertThat(FrostRules.perTick(recorded)).isEqualTo(252);
        assertThat(FrostRules.batches(recorded)).isEqualTo(98);
        assertThat(FrostRules.perTick(33 * 33)).isEqualTo(FrostRules.MIN_COLUMNS_PER_TICK);
        assertThat(FrostRules.batches(33 * 33)).isEqualTo(5);
        List<Column> order = FrostRules.order(new Area(0, 0, 32, 32), 16, 16, new Random(2));
        assertThat(FrostRules.batch(order, 0)).hasSize(FrostRules.MIN_COLUMNS_PER_TICK);
        assertThat(FrostRules.batch(order, 4)).hasSize(33 * 33 - 4 * FrostRules.MIN_COLUMNS_PER_TICK);
        assertThat(FrostRules.batch(order, 5)).isEmpty();
    }
}
