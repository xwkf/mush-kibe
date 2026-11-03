package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.minigame.bedwars.cosmetic.animation.DesertRules.Patches;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class DesertRulesTest {

    private static final BlockType STONE = new BlockType(Material.STONE, 0);
    private static final BlockType AIR = new BlockType(Material.AIR, 0);

    private static BlockType type(Material material, int data) {
        return new BlockType(material, data);
    }

    private static BlockType[] column(BlockType... types) {
        return types;
    }

    @Test
    void sandstoneStrataFollowTheAbsoluteHeightEveryFourBlocks() {
        for (int y = -8; y <= 40; y++) {
            BlockType stratum = DesertRules.stratum(y, false);

            assertThat(stratum.material()).isEqualTo(Material.SANDSTONE);
            assertThat(stratum.data()).as("y %d", y).isEqualTo(Math.floorMod(y, 4) == 0 ? 2 : 0);
        }
    }

    @Test
    void clayStrataRepeatEveryEightBlocks() {
        List<BlockType> expected = List.of(
            type(Material.HARD_CLAY, 0), type(Material.STAINED_CLAY, 1), type(Material.STAINED_CLAY, 1),
            type(Material.STAINED_CLAY, 4), type(Material.HARD_CLAY, 0), type(Material.STAINED_CLAY, 0),
            type(Material.STAINED_CLAY, 14), type(Material.RED_SANDSTONE, 0));

        for (int y = -9; y < 40; y++) {
            assertThat(DesertRules.stratum(y, true)).as("y %d", y).isEqualTo(expected.get(Math.floorMod(y, 8)));
        }
    }

    @Test
    void aSolidBlockWithOpenAirAboveAndSolidBelowBecomesSandOrRedSand() {
        BlockType[] original = column(STONE, STONE, STONE, AIR);

        BlockType[] plain = DesertRules.convert(original, 0, false);
        BlockType[] red = DesertRules.convert(original, 0, true);

        assertThat(plain).containsExactly(type(Material.SANDSTONE, 2), type(Material.SANDSTONE, 0), type(Material.SAND, 0), AIR);
        assertThat(red).containsExactly(type(Material.HARD_CLAY, 0), type(Material.STAINED_CLAY, 1), type(Material.SAND, 1), AIR);
    }

    @Test
    void aLoneFloatingBlockKeepsItsStratum() {
        BlockType[] converted = DesertRules.convert(column(AIR, STONE, AIR), 5, false);

        assertThat(converted[1]).isEqualTo(DesertRules.stratum(6, false));
    }

    @Test
    void aBlockUnderGlassOrLeavesKeepsItsStratum() {
        BlockType[] underGlass = DesertRules.convert(column(STONE, STONE, type(Material.GLASS, 0)), 0, false);
        BlockType[] underLeaves = DesertRules.convert(column(STONE, STONE, type(Material.LEAVES, 0)), 0, false);

        assertThat(underGlass[1]).isEqualTo(DesertRules.stratum(1, false));
        assertThat(underGlass[2]).isEqualTo(type(Material.GLASS, 0));
        assertThat(underLeaves[1]).isEqualTo(DesertRules.stratum(1, false));
        assertThat(underLeaves[2]).isEqualTo(AIR);
    }

    @Test
    void aBlockUnderAPlantBecomesSandBecausePlantsAreNotSolid() {
        BlockType[] converted = DesertRules.convert(column(STONE, STONE, type(Material.RED_ROSE, 0)), 0, false);

        assertThat(converted[1]).isEqualTo(type(Material.SAND, 0));
        assertThat(converted[2]).isEqualTo(AIR);
    }

    @Test
    void slabsBecomeSandstoneSlabsKeepingOnlyTheUpperHalfBit() {
        assertThat(DesertRules.convert(column(type(Material.STEP, 7)), 0, false)[0]).isEqualTo(type(Material.STEP, 1));
        assertThat(DesertRules.convert(column(type(Material.WOOD_STEP, 9)), 0, false)[0]).isEqualTo(type(Material.STEP, 9));
        assertThat(DesertRules.convert(column(type(Material.WOOD_STEP, 4)), 0, true)[0]).isEqualTo(type(Material.STEP, 1));
    }

    @Test
    void stairsBecomeSandstoneStairsKeepingTheirData() {
        for (Material stairs : DesertRules.STAIRS) {
            for (int data = 0; data < 8; data++) {
                assertThat(DesertRules.convert(column(type(stairs, data)), 0, false)[0]).isEqualTo(type(Material.SANDSTONE_STAIRS, data));
            }
        }
    }

    @Test
    void leavesVinesFlowersAndTallGrassVanish() {
        for (Material plant : DesertRules.PLANTS) {
            BlockType[] converted = DesertRules.convert(column(AIR, type(plant, 0), AIR), 0, false);

            assertThat(converted[1]).as(plant.name()).isEqualTo(AIR);
        }
    }

    @Test
    void shrubsAndFernsOnANewSandTopBecomeADeadBush() {
        assertThat(DesertRules.convert(column(STONE, STONE, type(Material.LONG_GRASS, 1)), 0, false)[2]).isEqualTo(type(Material.DEAD_BUSH, 0));
        assertThat(DesertRules.convert(column(STONE, STONE, type(Material.LONG_GRASS, 2)), 0, false)[2]).isEqualTo(type(Material.DEAD_BUSH, 0));
        assertThat(DesertRules.convert(column(STONE, STONE, type(Material.DOUBLE_PLANT, 0)), 0, true)[2]).isEqualTo(type(Material.DEAD_BUSH, 0));
    }

    @Test
    void otherPlantsAndPlantsWithoutASandTopBelowDoNotBecomeADeadBush() {
        assertThat(DesertRules.convert(column(STONE, STONE, type(Material.LONG_GRASS, 0)), 0, false)[2]).isEqualTo(AIR);
        assertThat(DesertRules.convert(column(STONE, STONE, type(Material.DOUBLE_PLANT, 8)), 0, false)[2]).isEqualTo(AIR);
        assertThat(DesertRules.convert(column(STONE, STONE, type(Material.YELLOW_FLOWER, 0)), 0, false)[2]).isEqualTo(AIR);
        assertThat(DesertRules.convert(column(AIR, STONE, type(Material.LONG_GRASS, 1)), 0, false)[2]).isEqualTo(AIR);
    }

    @Test
    void logsGlassFencesAndCarpetsStayAsTheyWere() {
        for (BlockType kept : List.of(type(Material.LOG, 3), type(Material.GLASS, 0), type(Material.FENCE, 0), type(Material.CARPET, 4), type(Material.BARRIER, 0), type(Material.TORCH, 5))) {
            assertThat(DesertRules.convert(column(STONE, kept), 0, false)[1]).isEqualTo(kept);
        }
    }

    @Test
    void theCactusSitsOnTheHighestSandWithAirAboveWhenTheChanceHits() {
        BlockType sand = type(Material.SAND, 0);
        RandomGenerator lucky = () -> 0L;
        RandomGenerator unlucky = () -> -1L;

        assertThat(DesertRules.cactusSpot(column(sand, AIR, sand, AIR), lucky)).isEqualTo(2);
        assertThat(DesertRules.cactusSpot(column(sand, AIR, sand, AIR), unlucky)).isEqualTo(-1);
        assertThat(DesertRules.cactusSpot(column(sand, type(Material.SANDSTONE, 0)), lucky)).isEqualTo(-1);
    }

    @Test
    void columnsAreOrderedByManhattanDistanceFromTheOriginBlock() {
        List<int[]> order = DesertRules.order(100, -50, new Random(1));

        assertThat(order).hasSize(144 * 144);
        assertThat(order.getFirst()).containsExactly(100, -50);
        int previous = 0;
        Set<String> seen = new HashSet<>();
        for (int[] column : order) {
            int distance = DesertRules.distance(column[0], column[1], 100, -50);
            assertThat(distance).isGreaterThanOrEqualTo(previous);
            previous = distance;
            seen.add(column[0] + "," + column[1]);
            assertThat(column[0]).isBetween(28, 171);
            assertThat(column[1]).isBetween(-122, 21);
        }
        assertThat(seen).hasSize(144 * 144);
    }

    @Test
    void theWaveIsSplitIntoOneHundredSixtyTwoBatchesOfOneHundredTwentyEightColumns() {
        List<int[]> order = DesertRules.order(0, 0, new Random(2));

        assertThat(DesertRules.batches(order.size())).isEqualTo(162);
        assertThat(DesertRules.batch(order, 0)).hasSize(128);
        assertThat(DesertRules.batch(order, 161)).hasSize(128);
        assertThat(DesertRules.batch(order, 162)).isEmpty();
    }

    @Test
    void aSeedColumnTakesItsOwnStyleAndNeighboursUsuallyAgree() {
        Patches patches = new Patches(0, 0, new Random(11));

        assertThat(patches.clay(patches.seedX(0), patches.seedZ(0))).isEqualTo(patches.seedClay(0));
        int clay = 0;
        int samples = 0;
        int agree = 0;
        for (int x = 0; x < 143; x += 3) {
            for (int z = 0; z < 143; z += 3) {
                samples++;
                clay += patches.clay(x, z) ? 1 : 0;
                agree += patches.clay(x, z) == patches.clay(x + 1, z) ? 1 : 0;
            }
        }
        assertThat(clay / (double) samples).isBetween(0.02, 0.8);
        assertThat(agree / (double) samples).isGreaterThan(0.8);
    }
}
