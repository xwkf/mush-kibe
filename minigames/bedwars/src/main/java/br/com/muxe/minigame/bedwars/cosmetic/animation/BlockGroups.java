package br.com.muxe.minigame.bedwars.cosmetic.animation;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;

final class BlockGroups {

    static final int UPPER_HALF = 8;
    static final Set<Material> TERRAIN = EnumSet.of(
        Material.STONE, Material.GRASS, Material.DIRT, Material.COBBLESTONE, Material.BEDROCK, Material.GOLD_ORE, Material.IRON_ORE,
        Material.COAL_ORE, Material.LAPIS_ORE, Material.LAPIS_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.DIAMOND_BLOCK,
        Material.DIAMOND_ORE, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE, Material.EMERALD_ORE, Material.EMERALD_BLOCK,
        Material.REDSTONE_BLOCK, Material.COAL_BLOCK, Material.QUARTZ_ORE, Material.QUARTZ_BLOCK, Material.DOUBLE_STEP,
        Material.DOUBLE_STONE_SLAB2, Material.BRICK, Material.MOSSY_COBBLESTONE, Material.OBSIDIAN, Material.SMOOTH_BRICK,
        Material.NETHERRACK, Material.NETHER_BRICK, Material.ENDER_STONE, Material.MYCEL, Material.HUGE_MUSHROOM_1,
        Material.HUGE_MUSHROOM_2, Material.PACKED_ICE, Material.PRISMARINE, Material.SEA_LANTERN, Material.GLOWSTONE,
        Material.JACK_O_LANTERN, Material.PUMPKIN, Material.MELON_BLOCK, Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF,
        Material.JUKEBOX, Material.NOTE_BLOCK, Material.TNT, Material.SANDSTONE, Material.RED_SANDSTONE);
    static final Set<Material> WOODEN = EnumSet.of(Material.WOOD, Material.LOG, Material.LOG_2, Material.WOOD_DOUBLE_STEP, Material.BOOKSHELF);
    static final Set<Material> COLOURED = EnumSet.of(Material.WOOL, Material.STAINED_CLAY, Material.HARD_CLAY);
    static final Set<Material> LOOSE = EnumSet.of(Material.SAND, Material.GRAVEL, Material.CLAY, Material.SNOW_BLOCK, Material.SOUL_SAND,
        Material.SPONGE, Material.HAY_BLOCK);
    static final Set<Material> SLABS = EnumSet.of(Material.STEP, Material.WOOD_STEP, Material.STONE_SLAB2);
    static final Set<Material> WOODEN_STAIRS = EnumSet.of(Material.WOOD_STAIRS, Material.SPRUCE_WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS,
        Material.JUNGLE_WOOD_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS);
    static final Set<Material> STONE_STAIRS = EnumSet.of(Material.COBBLESTONE_STAIRS, Material.BRICK_STAIRS, Material.SMOOTH_STAIRS,
        Material.NETHER_BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.QUARTZ_STAIRS, Material.RED_SANDSTONE_STAIRS);
    static final Set<Material> FENCES = EnumSet.of(Material.FENCE, Material.SPRUCE_FENCE, Material.BIRCH_FENCE, Material.JUNGLE_FENCE,
        Material.DARK_OAK_FENCE, Material.ACACIA_FENCE, Material.NETHER_FENCE, Material.COBBLE_WALL);
    static final Set<Material> PLANTS = EnumSet.of(Material.LEAVES, Material.LEAVES_2, Material.LONG_GRASS, Material.DEAD_BUSH,
        Material.YELLOW_FLOWER, Material.RED_ROSE, Material.DOUBLE_PLANT, Material.VINE, Material.SAPLING, Material.SNOW,
        Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.WATER_LILY);

    private BlockGroups() {
    }
}
