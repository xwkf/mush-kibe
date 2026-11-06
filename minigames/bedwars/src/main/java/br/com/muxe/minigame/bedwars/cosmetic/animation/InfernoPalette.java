package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.function.UnaryOperator;
import org.bukkit.Material;

final class InfernoPalette implements UnaryOperator<BlockType> {

    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final BlockType NETHERRACK = new BlockType(Material.NETHERRACK, 0);
    static final BlockType SOUL_SAND = new BlockType(Material.SOUL_SAND, 0);
    static final BlockType LAVA = new BlockType(Material.STATIONARY_LAVA, 0);
    static final BlockType SLAB = new BlockType(Material.DAYLIGHT_DETECTOR, 0);
    static final BlockType FENCE = new BlockType(Material.NETHER_FENCE, 0);

    @Override
    public BlockType apply(BlockType type) {
        Material material = type.material();
        if (BlockGroups.PLANTS.contains(material)) {
            return AIR;
        }
        if (BlockGroups.COLOURED.contains(material)) {
            return LAVA;
        }
        if (BlockGroups.LOOSE.contains(material)) {
            return SOUL_SAND;
        }
        if (BlockGroups.TERRAIN.contains(material) || BlockGroups.WOODEN.contains(material)) {
            return NETHERRACK;
        }
        if (BlockGroups.SLABS.contains(material)) {
            return SLAB;
        }
        if (BlockGroups.WOODEN_STAIRS.contains(material) || BlockGroups.STONE_STAIRS.contains(material)) {
            return new BlockType(Material.NETHER_BRICK_STAIRS, type.data());
        }
        if (BlockGroups.FENCES.contains(material)) {
            return FENCE;
        }
        return type;
    }
}
