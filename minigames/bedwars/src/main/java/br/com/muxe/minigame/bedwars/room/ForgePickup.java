package br.com.muxe.minigame.bedwars.room;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

public final class ForgePickup {

    public static final String FORGE_ITEM = "muxe-forge";
    public static final double GATHER_RADIUS = 0.5;
    public static final double SPLIT_HEIGHT = 2.0;
    public static final float SOUND_VOLUME = 1f;
    public static final float SOUND_PITCH = 1f;

    private ForgePickup() {
    }

    public static boolean splits(Material type) {
        return type == Material.IRON_INGOT || type == Material.GOLD_INGOT;
    }

    public static List<Item> gather(Item picked) {
        List<Item> items = new ArrayList<>();
        items.add(picked);
        Material type = picked.getItemStack().getType();
        for (Entity nearby : picked.getNearbyEntities(GATHER_RADIUS, GATHER_RADIUS, GATHER_RADIUS)) {
            if (nearby instanceof Item other && other.isValid() && other.getItemStack().getType() == type
                && other.getLocation().distanceSquared(picked.getLocation()) < GATHER_RADIUS * GATHER_RADIUS) {
                items.add(other);
            }
        }
        return items;
    }
}
