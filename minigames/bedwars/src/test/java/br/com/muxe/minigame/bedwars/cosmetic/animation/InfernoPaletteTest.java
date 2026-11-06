package br.com.muxe.minigame.bedwars.cosmetic.animation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class InfernoPaletteTest {

    private final InfernoPalette palette = new InfernoPalette();

    private BlockType convert(Material material, int data) {
        return palette.apply(new BlockType(material, data));
    }

    @Test
    void terrainAndWoodTurnIntoNetherrack() {
        assertThat(convert(Material.STONE, 3)).isEqualTo(InfernoPalette.NETHERRACK);
        assertThat(convert(Material.GRASS, 0)).isEqualTo(InfernoPalette.NETHERRACK);
        assertThat(convert(Material.QUARTZ_BLOCK, 0)).isEqualTo(InfernoPalette.NETHERRACK);
        assertThat(convert(Material.WOOD, 1)).isEqualTo(InfernoPalette.NETHERRACK);
    }

    @Test
    void colouredBlocksMeltIntoStillLavaAndLooseOnesIntoSoulSand() {
        assertThat(convert(Material.WOOL, 14)).isEqualTo(new BlockType(Material.STATIONARY_LAVA, 0));
        assertThat(convert(Material.STAINED_CLAY, 4)).isEqualTo(InfernoPalette.LAVA);
        assertThat(convert(Material.SAND, 0)).isEqualTo(InfernoPalette.SOUL_SAND);
        assertThat(convert(Material.CLAY, 0)).isEqualTo(InfernoPalette.SOUL_SAND);
    }

    @Test
    void shapedBlocksKeepTheirShapeInNetherMaterials() {
        assertThat(convert(Material.STEP, 8)).isEqualTo(new BlockType(Material.DAYLIGHT_DETECTOR, 0));
        assertThat(convert(Material.WOOD_STAIRS, 3)).isEqualTo(new BlockType(Material.NETHER_BRICK_STAIRS, 3));
        assertThat(convert(Material.FENCE, 0)).isEqualTo(new BlockType(Material.NETHER_FENCE, 0));
    }

    @Test
    void plantsAndSnowBurnAwayAndEverythingElseStays() {
        assertThat(convert(Material.LONG_GRASS, 1)).isEqualTo(InfernoPalette.AIR);
        assertThat(convert(Material.SNOW, 0)).isEqualTo(InfernoPalette.AIR);
        assertThat(convert(Material.LEAVES, 4)).isEqualTo(InfernoPalette.AIR);
        assertThat(convert(Material.AIR, 0)).isEqualTo(InfernoPalette.AIR);
        assertThat(convert(Material.GLASS, 0)).isEqualTo(new BlockType(Material.GLASS, 0));
        assertThat(convert(Material.BED_BLOCK, 2)).isEqualTo(new BlockType(Material.BED_BLOCK, 2));
    }
}
