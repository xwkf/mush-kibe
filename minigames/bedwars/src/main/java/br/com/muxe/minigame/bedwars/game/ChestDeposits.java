package br.com.muxe.minigame.bedwars.game;

import java.util.Set;
import org.bukkit.Material;

public final class ChestDeposits {

    private static final Set<Material> CHESTS = Set.of(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST);
    private static final Set<Material> KEPT = Set.of(
        Material.WOOD_SWORD, Material.SHEARS,
        Material.WOOD_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE,
        Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE
    );

    private ChestDeposits() {
    }

    public static boolean isChest(Material material) {
        return CHESTS.contains(material);
    }

    public static boolean canStore(Material material) {
        return material != Material.AIR && !KEPT.contains(material);
    }
}
