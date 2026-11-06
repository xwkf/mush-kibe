package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.function.UnaryOperator;
import org.bukkit.Material;

final class EndPalette implements UnaryOperator<BlockType> {

    static final int BIRCH = 2;
    static final int SANDSTONE_SLAB = 1;
    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final BlockType END_STONE = new BlockType(Material.ENDER_STONE, 0);
    static final BlockType SANDSTONE = new BlockType(Material.SANDSTONE, 0);
    static final BlockType BIRCH_PLANKS = new BlockType(Material.WOOD, BIRCH);
    static final BlockType FENCE = new BlockType(Material.BIRCH_FENCE, 0);

    @Override
    public BlockType apply(BlockType type) {
        Material material = type.material();
        int upper = type.data() & BlockGroups.UPPER_HALF;
        if (BlockGroups.PLANTS.contains(material)) {
            return AIR;
        }
        if (BlockGroups.WOODEN.contains(material)) {
            return BIRCH_PLANKS;
        }
        if (BlockGroups.COLOURED.contains(material) || BlockGroups.LOOSE.contains(material)) {
            return SANDSTONE;
        }
        if (BlockGroups.TERRAIN.contains(material)) {
            return END_STONE;
        }
        if (material == Material.WOOD_STEP) {
            return new BlockType(Material.WOOD_STEP, BIRCH | upper);
        }
        if (BlockGroups.SLABS.contains(material)) {
            return new BlockType(Material.STEP, SANDSTONE_SLAB | upper);
        }
        if (BlockGroups.WOODEN_STAIRS.contains(material)) {
            return new BlockType(Material.BIRCH_WOOD_STAIRS, type.data());
        }
        if (BlockGroups.STONE_STAIRS.contains(material)) {
            return new BlockType(Material.SANDSTONE_STAIRS, type.data());
        }
        if (BlockGroups.FENCES.contains(material)) {
            return FENCE;
        }
        return type;
    }
}
