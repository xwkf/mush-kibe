package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import org.bukkit.Material;

final class EndPalette implements MapWave.Palette {

    static final double PATCHES_PER_COLUMN = 0.012;
    static final double SHELL = 1.5;
    static final int BIRCH = 2;
    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final BlockType END_STONE = new BlockType(Material.ENDER_STONE, 0);
    static final BlockType SANDSTONE = new BlockType(Material.SANDSTONE, 0);
    static final BlockType BIRCH_PLANKS = new BlockType(Material.WOOD, BIRCH);

    private final Patches patches;

    EndPalette(Patches patches) {
        this.patches = patches;
    }

    @Override
    public BlockType apply(int x, int y, int z, BlockType type) {
        return switch (BlockGroups.shape(type.material())) {
            case FULL -> stone(patches.depth(x, y, z));
            case PARTIAL, LOOSE -> AIR;
            case KEPT -> type;
        };
    }

    static BlockType stone(double depth) {
        if (depth >= SHELL) {
            return BIRCH_PLANKS;
        }
        return depth > 0 ? SANDSTONE : END_STONE;
    }
}
