package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class EndPaletteTest {

    private final EndPalette palette = new EndPalette();

    private BlockType convert(Material material, int data) {
        return palette.apply(new BlockType(material, data));
    }

    @Test
    void terrainTurnsIntoEndStone() {
        assertThat(convert(Material.STONE, 0)).isEqualTo(new BlockType(Material.ENDER_STONE, 0));
        assertThat(convert(Material.DIRT, 1)).isEqualTo(EndPalette.END_STONE);
        assertThat(convert(Material.SANDSTONE, 2)).isEqualTo(EndPalette.END_STONE);
    }

    @Test
    void woodTurnsIntoBirchPlanksAndColouredOrLooseBlocksIntoSandstone() {
        assertThat(convert(Material.LOG, 1)).isEqualTo(new BlockType(Material.WOOD, 2));
        assertThat(convert(Material.WOOD, 0)).isEqualTo(EndPalette.BIRCH_PLANKS);
        assertThat(convert(Material.STAINED_CLAY, 15)).isEqualTo(new BlockType(Material.SANDSTONE, 0));
        assertThat(convert(Material.SAND, 1)).isEqualTo(EndPalette.SANDSTONE);
    }

    @Test
    void slabsKeepTheirHalfAndStairsTheirFacing() {
        assertThat(convert(Material.STEP, 8)).isEqualTo(new BlockType(Material.STEP, 9));
        assertThat(convert(Material.WOOD_STEP, 0)).isEqualTo(new BlockType(Material.WOOD_STEP, 2));
        assertThat(convert(Material.SPRUCE_WOOD_STAIRS, 5)).isEqualTo(new BlockType(Material.BIRCH_WOOD_STAIRS, 5));
        assertThat(convert(Material.COBBLESTONE_STAIRS, 2)).isEqualTo(new BlockType(Material.SANDSTONE_STAIRS, 2));
        assertThat(convert(Material.FENCE, 0)).isEqualTo(EndPalette.FENCE);
    }

    @Test
    void plantsVanishAndEverythingElseStays() {
        assertThat(convert(Material.RED_ROSE, 0)).isEqualTo(EndPalette.AIR);
        assertThat(convert(Material.DOUBLE_PLANT, 2)).isEqualTo(EndPalette.AIR);
        assertThat(convert(Material.GLASS, 0)).isEqualTo(new BlockType(Material.GLASS, 0));
    }
}
