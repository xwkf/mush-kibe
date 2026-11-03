package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class DesertRules {

    static final int AREA = 144;
    static final int AREA_BEFORE = 72;
    static final int COLUMNS_PER_TICK = 128;
    static final int SEEDS = 120;
    static final double CLAY_SHARE = 0.3;
    static final double CACTUS_CHANCE = 0.005;
    static final int SMOOTH_EVERY = 4;
    static final int SMOOTH = 2;
    static final int CLAY_PERIOD = 8;
    static final int RED_SAND = 1;
    static final int SANDSTONE_SLAB = 1;
    static final int UPPER_HALF = 8;
    static final int TALL_GRASS_FERN = 2;
    static final int TALL_GRASS_SHRUB = 1;
    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final BlockType DEAD_BUSH = new BlockType(Material.DEAD_BUSH, 0);
    static final List<BlockType> CLAY_STRATA = List.of(
        new BlockType(Material.HARD_CLAY, 0),
        new BlockType(Material.STAINED_CLAY, 1),
        new BlockType(Material.STAINED_CLAY, 1),
        new BlockType(Material.STAINED_CLAY, 4),
        new BlockType(Material.HARD_CLAY, 0),
        new BlockType(Material.STAINED_CLAY, 0),
        new BlockType(Material.STAINED_CLAY, 14),
        new BlockType(Material.RED_SANDSTONE, 0));
    static final Set<Material> CONVERTIBLE = EnumSet.of(
        Material.STONE, Material.DIRT, Material.WOOD, Material.LAPIS_BLOCK, Material.WOOL, Material.GOLD_BLOCK,
        Material.IRON_BLOCK, Material.DIAMOND_BLOCK, Material.JUKEBOX, Material.PUMPKIN, Material.GLOWSTONE,
        Material.JACK_O_LANTERN, Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF, Material.WOOD_DOUBLE_STEP,
        Material.DOUBLE_STEP, Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK, Material.STAINED_CLAY,
        Material.SEA_LANTERN, Material.HAY_BLOCK, Material.RED_SANDSTONE, Material.GRASS, Material.COBBLESTONE,
        Material.BEDROCK, Material.SAND, Material.GRAVEL, Material.GOLD_ORE, Material.IRON_ORE, Material.COAL_ORE,
        Material.LAPIS_ORE, Material.SANDSTONE, Material.BRICK, Material.TNT, Material.BOOKSHELF,
        Material.MOSSY_COBBLESTONE, Material.OBSIDIAN, Material.DIAMOND_ORE, Material.REDSTONE_ORE,
        Material.GLOWING_REDSTONE_ORE, Material.CLAY, Material.NETHERRACK, Material.SOUL_SAND, Material.SMOOTH_BRICK,
        Material.NETHER_BRICK, Material.ENDER_STONE, Material.QUARTZ_BLOCK, Material.HARD_CLAY, Material.COAL_BLOCK,
        Material.PACKED_ICE, Material.MYCEL, Material.SNOW_BLOCK, Material.SPONGE, Material.EMERALD_ORE,
        Material.QUARTZ_ORE);
    static final Set<Material> SLABS = EnumSet.of(Material.STEP, Material.WOOD_STEP);
    static final Set<Material> STAIRS = EnumSet.of(
        Material.WOOD_STAIRS, Material.COBBLESTONE_STAIRS, Material.BRICK_STAIRS, Material.SMOOTH_STAIRS,
        Material.NETHER_BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.SPRUCE_WOOD_STAIRS,
        Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS, Material.QUARTZ_STAIRS, Material.ACACIA_STAIRS,
        Material.DARK_OAK_STAIRS, Material.RED_SANDSTONE_STAIRS);
    static final Set<Material> PLANTS = EnumSet.of(
        Material.LEAVES, Material.LEAVES_2, Material.VINE, Material.YELLOW_FLOWER, Material.RED_ROSE,
        Material.DOUBLE_PLANT, Material.LONG_GRASS);

    private DesertRules() {
    }

    static BlockType stratum(int y, boolean clay) {
        if (clay) {
            return CLAY_STRATA.get(Math.floorMod(y, CLAY_PERIOD));
        }
        return new BlockType(Material.SANDSTONE, Math.floorMod(y, SMOOTH_EVERY) == 0 ? SMOOTH : 0);
    }

    static BlockType sand(boolean clay) {
        return new BlockType(Material.SAND, clay ? RED_SAND : 0);
    }

    static BlockType slab(int data) {
        return new BlockType(Material.STEP, SANDSTONE_SLAB | (data & UPPER_HALF));
    }

    static boolean open(BlockType type) {
        return type.material() == Material.AIR || !type.material().isSolid();
    }

    static boolean sandTop(BlockType above, BlockType below) {
        return open(above) && !open(below);
    }

    static boolean growsDeadBush(BlockType type) {
        return type.material() == Material.LONG_GRASS && (type.data() == TALL_GRASS_SHRUB || type.data() == TALL_GRASS_FERN)
            || type.material() == Material.DOUBLE_PLANT && type.data() == 0;
    }

    static BlockType[] convert(BlockType[] original, int bottomY, boolean clay) {
        BlockType[] converted = new BlockType[original.length];
        for (int index = 0; index < original.length; index++) {
            BlockType type = original[index];
            BlockType above = index + 1 < original.length ? original[index + 1] : AIR;
            BlockType below = index > 0 ? original[index - 1] : AIR;
            converted[index] = convertCell(type, above, below, index > 0 ? converted[index - 1] : AIR, bottomY + index, clay);
        }
        return converted;
    }

    private static BlockType convertCell(BlockType type, BlockType above, BlockType below, BlockType newBelow, int y, boolean clay) {
        Material material = type.material();
        if (CONVERTIBLE.contains(material)) {
            return sandTop(above, below) ? sand(clay) : stratum(y, clay);
        }
        if (SLABS.contains(material)) {
            return slab(type.data());
        }
        if (STAIRS.contains(material)) {
            return new BlockType(Material.SANDSTONE_STAIRS, type.data());
        }
        if (PLANTS.contains(material)) {
            return newBelow.material() == Material.SAND && growsDeadBush(type) ? DEAD_BUSH : AIR;
        }
        return type;
    }

    static int cactusSpot(BlockType[] converted, RandomGenerator random) {
        int spot = -1;
        for (int index = 0; index < converted.length; index++) {
            boolean airAbove = index + 1 == converted.length || converted[index + 1].material() == Material.AIR;
            if (converted[index].material() == Material.SAND && airAbove) {
                spot = index;
            }
        }
        if (spot >= 0 && random.nextDouble() < CACTUS_CHANCE) {
            return spot;
        }
        return -1;
    }

    static int distance(int x, int z, int centreX, int centreZ) {
        return Math.abs(x - centreX) + Math.abs(z - centreZ);
    }

    static List<int[]> order(int centreX, int centreZ, RandomGenerator random) {
        List<int[]> columns = new ArrayList<>(AREA * AREA);
        for (int x = centreX - AREA_BEFORE; x < centreX - AREA_BEFORE + AREA; x++) {
            for (int z = centreZ - AREA_BEFORE; z < centreZ - AREA_BEFORE + AREA; z++) {
                columns.add(new int[] {x, z});
            }
        }
        Collections.shuffle(columns, random);
        columns.sort(Comparator.comparingInt(column -> distance(column[0], column[1], centreX, centreZ)));
        return columns;
    }

    static int batches(int columns) {
        return (columns + COLUMNS_PER_TICK - 1) / COLUMNS_PER_TICK;
    }

    static List<int[]> batch(List<int[]> order, int index) {
        int from = index * COLUMNS_PER_TICK;
        return order.subList(Math.min(from, order.size()), Math.min(from + COLUMNS_PER_TICK, order.size()));
    }

    static final class Patches {

        private final int[] seedX = new int[SEEDS];
        private final int[] seedZ = new int[SEEDS];
        private final boolean[] clay = new boolean[SEEDS];

        Patches(int minX, int minZ, RandomGenerator random) {
            for (int seed = 0; seed < SEEDS; seed++) {
                seedX[seed] = minX + random.nextInt(AREA);
                seedZ[seed] = minZ + random.nextInt(AREA);
                clay[seed] = random.nextDouble() < CLAY_SHARE;
            }
        }

        boolean clay(int x, int z) {
            int nearest = 0;
            int best = Integer.MAX_VALUE;
            for (int seed = 0; seed < SEEDS; seed++) {
                int distance = distance(x, z, seedX[seed], seedZ[seed]);
                if (distance < best) {
                    best = distance;
                    nearest = seed;
                }
            }
            return clay[nearest];
        }

        int seedX(int seed) {
            return seedX[seed];
        }

        int seedZ(int seed) {
            return seedZ[seed];
        }

        boolean seedClay(int seed) {
            return clay[seed];
        }
    }
}
