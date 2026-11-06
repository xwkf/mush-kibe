package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class InfernoPaletteTest {

    private final InfernoPalette palette = new InfernoPalette(new Patches(List.of(new Patches.Ball(100.5, 30.5, 100.5, 5))));

    private BlockType away(Material material, int data) {
        return palette.apply(0, 30, 0, new BlockType(material, data));
    }

    private BlockType insideLava(Material material) {
        return palette.apply(100, 30, 100, new BlockType(material, 0));
    }

    @Test
    void everyFullBlockTurnsIntoNetherrackWhateverItWas() {
        for (Material material : List.of(Material.STONE, Material.COAL_BLOCK, Material.OBSIDIAN, Material.MELON_BLOCK, Material.HUGE_MUSHROOM_1,
            Material.NETHER_BRICK, Material.STAINED_CLAY, Material.WOOD, Material.HARD_CLAY, Material.WOOL, Material.DIAMOND_BLOCK,
            Material.QUARTZ_BLOCK, Material.GOLD_BLOCK, Material.DOUBLE_STONE_SLAB2, Material.GRASS, Material.REDSTONE_BLOCK, Material.LEAVES,
            Material.PRISMARINE, Material.SEA_LANTERN, Material.REDSTONE_LAMP_ON, Material.SPONGE, Material.SNOW_BLOCK, Material.NETHERRACK)) {
            assertThat(away(material, 0)).as("%s", material).isEqualTo(InfernoPalette.NETHERRACK);
        }
    }

    @Test
    void fullBlocksInsideALavaPatchMelt() {
        assertThat(insideLava(Material.STONE)).isEqualTo(new BlockType(Material.STATIONARY_LAVA, 0));
        assertThat(insideLava(Material.WOOL)).isEqualTo(InfernoPalette.LAVA);
        assertThat(palette.apply(100, 36, 100, new BlockType(Material.STONE, 0))).isEqualTo(InfernoPalette.NETHERRACK);
    }

    @Test
    void solidBlocksThatAreNotFullCubesTurnIntoSoulSandEvenInLava() {
        for (Material material : List.of(Material.NETHER_FENCE, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.COBBLE_WALL,
            Material.BRICK_STAIRS, Material.WOOD_STEP, Material.STEP, Material.QUARTZ_STAIRS, Material.PISTON_BASE, Material.ANVIL,
            Material.CHEST, Material.ENDER_CHEST, Material.FENCE, Material.CAULDRON, Material.GLASS)) {
            assertThat(away(material, 3)).as("%s", material).isEqualTo(InfernoPalette.SOUL_SAND);
        }
        assertThat(insideLava(Material.STAINED_GLASS)).isEqualTo(InfernoPalette.SOUL_SAND);
    }

    @Test
    void liquidsAndLooseBlocksVanishWhileAirAndBarriersStay() {
        for (Material material : List.of(Material.STATIONARY_LAVA, Material.LAVA, Material.WATER, Material.LEVER, Material.SKULL, Material.CARPET,
            Material.RED_MUSHROOM, Material.LONG_GRASS)) {
            assertThat(away(material, 0)).as("%s", material).isEqualTo(InfernoPalette.AIR);
        }
        assertThat(away(Material.AIR, 0)).isEqualTo(InfernoPalette.AIR);
        assertThat(away(Material.BARRIER, 0)).isEqualTo(new BlockType(Material.BARRIER, 0));
    }
}
