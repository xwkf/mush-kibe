package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import org.bukkit.Material;

final class InfernoPalette implements MapWave.Palette {

    static final double LAVA_PER_COLUMN = 0.0176;
    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final BlockType NETHERRACK = new BlockType(Material.NETHERRACK, 0);
    static final BlockType SOUL_SAND = new BlockType(Material.SOUL_SAND, 0);
    static final BlockType LAVA = new BlockType(Material.STATIONARY_LAVA, 0);

    private final Patches lava;

    InfernoPalette(Patches lava) {
        this.lava = lava;
    }

    @Override
    public BlockType apply(int x, int y, int z, BlockType type) {
        return switch (BlockGroups.shape(type.material())) {
            case FULL -> lava.depth(x, y, z) > 0 ? LAVA : NETHERRACK;
            case PARTIAL -> SOUL_SAND;
            case LOOSE -> AIR;
            case KEPT -> type;
        };
    }
}
