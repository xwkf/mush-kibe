package br.com.muxe.minigame.api.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class EndPaletteTest {

    private final EndPalette palette = new EndPalette(new Patches(List.of(new Patches.Ball(100.5, 30.5, 100.5, 5))));

    private BlockType away(Material material, int data) {
        return palette.apply(0, 30, 0, new BlockType(material, data));
    }

    @Test
    void everyFullBlockTurnsIntoEndStoneWhateverItWas() {
        for (Material material : List.of(Material.STAINED_CLAY, Material.DIRT, Material.STONE, Material.WOOL, Material.WOOD, Material.GRASS,
            Material.LOG_2, Material.LOG, Material.DIAMOND_BLOCK, Material.LEAVES, Material.WOOD_DOUBLE_STEP, Material.EMERALD_BLOCK,
            Material.GLOWSTONE, Material.BOOKSHELF, Material.GRAVEL, Material.JACK_O_LANTERN, Material.WORKBENCH, Material.SANDSTONE)) {
            assertThat(away(material, 1)).as("%s", material).isEqualTo(new BlockType(Material.ENDER_STONE, 0));
        }
    }

    @Test
    void insideAPatchTheCoreIsBirchPlanksAndTheLastBlockAndAHalfIsSandstone() {
        assertThat(palette.apply(100, 30, 100, new BlockType(Material.STONE, 0))).isEqualTo(new BlockType(Material.WOOD, 2));
        assertThat(palette.apply(100, 33, 100, new BlockType(Material.DIRT, 0))).isEqualTo(EndPalette.BIRCH_PLANKS);
        assertThat(palette.apply(100, 34, 100, new BlockType(Material.DIRT, 0))).isEqualTo(new BlockType(Material.SANDSTONE, 0));
        assertThat(palette.apply(100, 35, 100, new BlockType(Material.DIRT, 0))).isEqualTo(EndPalette.END_STONE);
        assertThat(EndPalette.stone(EndPalette.SHELL)).isEqualTo(EndPalette.BIRCH_PLANKS);
        assertThat(EndPalette.stone(0.01)).isEqualTo(EndPalette.SANDSTONE);
        assertThat(EndPalette.stone(0)).isEqualTo(EndPalette.END_STONE);
    }

    @Test
    void everythingThatIsNotAFullCubeVanishes() {
        for (Material material : List.of(Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.WOOD_STEP, Material.WEB, Material.DARK_OAK_STAIRS,
            Material.LONG_GRASS, Material.SPRUCE_FENCE, Material.DOUBLE_PLANT, Material.RED_SANDSTONE_STAIRS, Material.STONE_SLAB2, Material.CHEST,
            Material.FENCE_GATE, Material.TRAP_DOOR, Material.COBBLE_WALL)) {
            assertThat(away(material, 2)).as("%s", material).isEqualTo(EndPalette.AIR);
        }
        assertThat(away(Material.BARRIER, 0)).isEqualTo(new BlockType(Material.BARRIER, 0));
    }
}
