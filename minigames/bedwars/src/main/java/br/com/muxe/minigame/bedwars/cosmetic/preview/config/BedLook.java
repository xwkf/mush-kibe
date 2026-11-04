package br.com.muxe.minigame.bedwars.cosmetic.preview.config;

import java.util.HashSet;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class BedLook {

    public static final int REACH = 6;

    private BedLook() {
    }

    @SuppressWarnings("deprecation")
    public static Optional<BedBlocks> at(Player player) {
        Block block = player.getTargetBlock((HashSet<Byte>) null, REACH);
        if (block == null || block.getType() != Material.BED_BLOCK) {
            return Optional.empty();
        }
        return Optional.of(BedBlocks.of(block.getX(), block.getY(), block.getZ(), block.getData()));
    }
}
