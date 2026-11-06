package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.minigame.bedwars.cosmetic.animation.FrostRules.Blobs;
import br.com.muxe.minigame.bedwars.cosmetic.animation.FrostRules.Column;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class FrostRulesTest {

    private static Optional<BlockType> frozen(Material material, boolean packed) {
        return FrostRules.frozen(new BlockType(material, 0), packed);
    }

    @Test
    void fullBlocksTurnIntoSnowOrPackedIce() {
        assertThat(frozen(Material.WOOL, false)).contains(FrostRules.SNOW);
        assertThat(frozen(Material.OBSIDIAN, false)).contains(FrostRules.SNOW);
        assertThat(frozen(Material.STAINED_CLAY, true)).contains(FrostRules.PACKED_ICE);
        assertThat(frozen(Material.QUARTZ_BLOCK, true)).contains(FrostRules.PACKED_ICE);
    }

    @Test
    void stairsSlabsGlassAndWaterTurnIntoClearIce() {
        assertThat(frozen(Material.SMOOTH_STAIRS, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.STEP, true)).contains(FrostRules.ICE);
        assertThat(frozen(Material.STAINED_GLASS, false)).contains(FrostRules.ICE);
        assertThat(frozen(Material.STATIONARY_WATER, false)).contains(FrostRules.ICE);
    }

    @Test
    void plantsWitherAwayWhileAirBedsBarriersAndTorchesStay() {
        assertThat(frozen(Material.RED_ROSE, false)).contains(FrostRules.AIR);
        assertThat(frozen(Material.LONG_GRASS, false)).contains(FrostRules.AIR);
        assertThat(frozen(Material.AIR, true)).isEmpty();
        assertThat(frozen(Material.BED_BLOCK, false)).isEmpty();
        assertThat(frozen(Material.BARRIER, true)).isEmpty();
        assertThat(frozen(Material.TORCH, false)).isEmpty();
        assertThat(frozen(Material.SNOW_BLOCK, false)).isEmpty();
        assertThat(frozen(Material.PACKED_ICE, true)).isEmpty();
    }

    @Test
    void packedIceFormsBlobsOverAFifthOfTheBlocks() {
        Blobs blobs = new Blobs(42);
        int packed = 0;
        int same = 0;
        int pairs = 0;
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 30; y++) {
                for (int z = 0; z < 60; z++) {
                    boolean here = blobs.packed(x, y, z);
                    packed += here ? 1 : 0;
                    if (here) {
                        pairs++;
                        same += blobs.packed(x + 1, y, z) ? 1 : 0;
                    }
                }
            }
        }
        double share = packed / (60.0 * 30 * 60);

        assertThat(share).isBetween(0.12, 0.28);
        assertThat((double) same / pairs).isGreaterThan(0.7);
        assertThat(new Blobs(42).packed(13, 7, -21)).isEqualTo(blobs.packed(13, 7, -21));
    }

    @Test
    void theFrostSpreadsFromTheWinnerOutwardsOverTheWholeMap() {
        List<Column> order = FrostRules.order(new Area(-50, -50, 49, 49), 10, -5, new Random(1));

        assertThat(order).hasSize(100 * 100);
        assertThat(Math.hypot(order.getFirst().x() - 10, order.getFirst().z() + 5)).isLessThan(FrostRules.EDGE_JITTER);
        for (int index = 1; index < order.size(); index++) {
            assertThat(order.get(index).reach()).isGreaterThanOrEqualTo(order.get(index - 1).reach());
        }
        Column last = order.getLast();
        assertThat(Math.hypot(last.x() - 10, last.z() + 5)).isGreaterThan(70);
    }

    @Test
    void theFrontReachesOneHundredAndThirtyFiveBlocksInAboutNinetyFiveTicks() {
        List<Column> order = FrostRules.order(new Area(-70, -70, 85, 80), 30, -44, new Random(2));
        double spread = FrostRules.spread(order);

        assertThat(spread).isEqualTo(FrostRules.SPREAD);
        assertThat(FrostRules.tick(0, spread)).isEqualTo(1);
        assertThat(FrostRules.tick(35, spread)).isBetween(6, 8);
        assertThat(FrostRules.tick(135, spread)).isBetween(94, 97);
    }

    @Test
    void aHugeMapStillFreezesCompletelyByTickOneHundredAndFifty() {
        List<Column> order = FrostRules.order(new Area(-300, -300, 300, 300), 280, 280, new Random(3));
        double spread = FrostRules.spread(order);

        assertThat(spread).isGreaterThan(FrostRules.SPREAD);
        assertThat(FrostRules.tick(order.getLast().reach(), spread)).isBetween(FrostRules.LAST_TICK - 1, FrostRules.LAST_TICK);
    }
}
