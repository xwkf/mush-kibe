package br.com.muxe.minigame.bedwars.game;

import java.util.Collection;
import java.util.Set;
import org.bukkit.Material;

public final class ChestDeposits {

    private static final Set<Material> CHESTS = Set.of(Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST);
    private static final Set<Material> KEPT = Set.of(
        Material.WOOD_SWORD, Material.SHEARS,
        Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE,
        Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE
    );

    public enum Verdict {
        STORE,
        KEPT,
        LAST_SWORD,
        NOTHING
    }

    private ChestDeposits() {
    }

    public static boolean isChest(Material material) {
        return CHESTS.contains(material);
    }

    public static Verdict verdict(Material material, Collection<Material> inventory) {
        if (material == Material.AIR) {
            return Verdict.NOTHING;
        }
        if (isSword(material) && inventory.stream().filter(ChestDeposits::isSword).count() <= 1) {
            return Verdict.LAST_SWORD;
        }
        return KEPT.contains(material) ? Verdict.KEPT : Verdict.STORE;
    }

    public static boolean isSword(Material material) {
        return material.name().endsWith("_SWORD");
    }
}
