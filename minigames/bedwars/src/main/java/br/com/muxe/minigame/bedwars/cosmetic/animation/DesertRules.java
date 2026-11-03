package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
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
    static final int WAVE_TICKS = 162;
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

    static int[] box(Area area, int originX, int originZ) {
        return new int[] {
            Math.min((int) Math.floor(area.minX()), originX - AREA_BEFORE),
            Math.min((int) Math.floor(area.minZ()), originZ - AREA_BEFORE),
            Math.max((int) Math.floor(area.maxX()), originX - AREA_BEFORE + AREA - 1),
            Math.max((int) Math.floor(area.maxZ()), originZ - AREA_BEFORE + AREA - 1)
        };
    }

    static int[] square(int centreX, int centreZ) {
        return new int[] {centreX - AREA_BEFORE, centreZ - AREA_BEFORE, centreX - AREA_BEFORE + AREA - 1, centreZ - AREA_BEFORE + AREA - 1};
    }

    static List<int[]> order(int centreX, int centreZ, RandomGenerator random) {
        return order(square(centreX, centreZ), centreX, centreZ, random);
    }

    static List<int[]> order(int[] box, int centreX, int centreZ, RandomGenerator random) {
        List<int[]> columns = new ArrayList<>((box[2] - box[0] + 1) * (box[3] - box[1] + 1));
        for (int x = box[0]; x <= box[2]; x++) {
            for (int z = box[1]; z <= box[3]; z++) {
                columns.add(new int[] {x, z});
            }
        }
        Collections.shuffle(columns, random);
        columns.sort(Comparator.comparingInt(column -> distance(column[0], column[1], centreX, centreZ)));
        return columns;
    }

    static int perTick(int columns) {
        return Math.max(COLUMNS_PER_TICK, (columns + WAVE_TICKS - 1) / WAVE_TICKS);
    }

    static int batches(int columns) {
        int perTick = perTick(columns);
        return (columns + perTick - 1) / perTick;
    }

    static List<int[]> batch(List<int[]> order, int index) {
        int perTick = perTick(order.size());
        int from = index * perTick;
        return order.subList(Math.min(from, order.size()), Math.min(from + perTick, order.size()));
    }

    static final class Patches {

        private final int[] seedX;
        private final int[] seedZ;
        private final boolean[] clay;

        Patches(int minX, int minZ, RandomGenerator random) {
            this(new int[] {minX, minZ, minX + AREA - 1, minZ + AREA - 1}, random);
        }

        Patches(int[] box, RandomGenerator random) {
            int width = box[2] - box[0] + 1;
            int depth = box[3] - box[1] + 1;
            int seeds = (int) Math.max(SEEDS, Math.round(SEEDS * (double) width * depth / (AREA * AREA)));
            seedX = new int[seeds];
            seedZ = new int[seeds];
            clay = new boolean[seeds];
            for (int seed = 0; seed < seeds; seed++) {
                seedX[seed] = box[0] + random.nextInt(width);
                seedZ[seed] = box[1] + random.nextInt(depth);
                clay[seed] = random.nextDouble() < CLAY_SHARE;
            }
        }

        boolean clay(int x, int z) {
            int nearest = 0;
            int best = Integer.MAX_VALUE;
            for (int seed = 0; seed < seedX.length; seed++) {
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
