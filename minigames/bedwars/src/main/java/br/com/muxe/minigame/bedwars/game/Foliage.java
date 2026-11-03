package br.com.muxe.minigame.bedwars.game;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;

public final class Foliage {

    private static final Set<Material> BREAKABLE = EnumSet.of(Material.LONG_GRASS, Material.DOUBLE_PLANT, Material.DEAD_BUSH);

    private Foliage() {
    }

    public static boolean isFoliage(Material material) {
        return BREAKABLE.contains(material);
    }

    public static boolean isTall(Material material) {
        return material == Material.DOUBLE_PLANT;
    }
}
