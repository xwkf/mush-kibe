package br.com.muxe.minigame.bedwars.cosmetic.animation;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;

final class BlockGroups {

    static final Set<Material> FULL = EnumSet.of(
        Material.STONE, Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.WOOD, Material.BEDROCK, Material.SAND, Material.GRAVEL,
        Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE, Material.LOG, Material.LOG_2, Material.LEAVES, Material.LEAVES_2,
        Material.SPONGE, Material.LAPIS_ORE, Material.LAPIS_BLOCK, Material.DISPENSER, Material.DROPPER, Material.SANDSTONE,
        Material.RED_SANDSTONE, Material.NOTE_BLOCK, Material.WOOL, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.DOUBLE_STEP,
        Material.DOUBLE_STONE_SLAB2, Material.WOOD_DOUBLE_STEP, Material.BRICK, Material.TNT, Material.BOOKSHELF, Material.MOSSY_COBBLESTONE,
        Material.OBSIDIAN, Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.WORKBENCH, Material.FURNACE, Material.BURNING_FURNACE,
        Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE, Material.SNOW_BLOCK, Material.CLAY, Material.JUKEBOX, Material.PUMPKIN,
        Material.JACK_O_LANTERN, Material.NETHERRACK, Material.SOUL_SAND, Material.GLOWSTONE, Material.MONSTER_EGGS, Material.SMOOTH_BRICK,
        Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.MELON_BLOCK, Material.MYCEL, Material.NETHER_BRICK, Material.ENDER_STONE,
        Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.EMERALD_ORE, Material.EMERALD_BLOCK, Material.COMMAND,
        Material.QUARTZ_ORE, Material.QUARTZ_BLOCK, Material.REDSTONE_BLOCK, Material.STAINED_CLAY, Material.HARD_CLAY, Material.HAY_BLOCK,
        Material.COAL_BLOCK, Material.PACKED_ICE, Material.PRISMARINE, Material.SEA_LANTERN, Material.MOB_SPAWNER);
    static final Set<Material> KEPT = EnumSet.of(Material.AIR, Material.BARRIER);

    private BlockGroups() {
    }

    static Shape shape(Material material) {
        if (KEPT.contains(material)) {
            return Shape.KEPT;
        }
        if (FULL.contains(material)) {
            return Shape.FULL;
        }
        return material.isSolid() ? Shape.PARTIAL : Shape.LOOSE;
    }

    enum Shape {
        FULL,
        PARTIAL,
        LOOSE,
        KEPT
    }
}
