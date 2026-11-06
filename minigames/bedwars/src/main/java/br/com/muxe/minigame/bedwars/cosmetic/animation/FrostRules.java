package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class FrostRules {

    static final double PACKED_SHARE = 0.15;
    static final double SEED_SPACING = 6.5;
    static final int CELL = 8;
    static final int MAX_SEEDS = 12;
    static final int FIRST_TICK = 0;
    static final int WAVE_TICKS = 98;
    static final int MIN_COLUMNS_PER_TICK = 250;
    static final BlockType SNOW = new BlockType(Material.SNOW_BLOCK, 0);
    static final BlockType PACKED_ICE = new BlockType(Material.PACKED_ICE, 0);
    static final BlockType ICE = new BlockType(Material.ICE, 0);
    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final Set<Material> KEPT = EnumSet.of(Material.AIR, Material.BARRIER, Material.BED_BLOCK, Material.LAVA, Material.STATIONARY_LAVA);
    static final Set<Material> WATER = EnumSet.of(Material.WATER, Material.STATIONARY_WATER);
    static final Set<Material> CUBES = EnumSet.of(Material.SEA_LANTERN, Material.GLOWSTONE, Material.REDSTONE_BLOCK, Material.TNT);

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;
    private static final double UNIT = 0x1.0p-53;
    private static final double SEEDS_PER_CELL = CELL * CELL * CELL / (SEED_SPACING * SEED_SPACING * SEED_SPACING);

    private FrostRules() {
    }

    static Optional<BlockType> frozen(BlockType type, boolean packed) {
        Material material = type.material();
        BlockType frozen;
        if (KEPT.contains(material)) {
            return Optional.empty();
        } else if (WATER.contains(material)) {
            frozen = ICE;
        } else if (cube(material)) {
            frozen = packed ? PACKED_ICE : SNOW;
        } else if (material.isSolid()) {
            frozen = ICE;
        } else {
            frozen = AIR;
        }
        return frozen.equals(type) ? Optional.empty() : Optional.of(frozen);
    }

    static boolean cube(Material material) {
        return material.isOccluding() || CUBES.contains(material);
    }

    static boolean ground(BlockType frozen) {
        return frozen.equals(SNOW) || frozen.equals(PACKED_ICE);
    }

    static int distance(int x, int z, int originX, int originZ) {
        return Math.abs(x - originX) + Math.abs(z - originZ);
    }

    static List<Column> order(Area area, int originX, int originZ, RandomGenerator random) {
        int minX = (int) Math.floor(area.minX());
        int minZ = (int) Math.floor(area.minZ());
        int maxX = (int) Math.floor(area.maxX());
        int maxZ = (int) Math.floor(area.maxZ());
        List<Column> columns = new ArrayList<>((maxX - minX + 1) * (maxZ - minZ + 1));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                columns.add(new Column(x, z, distance(x, z, originX, originZ)));
            }
        }
        Collections.shuffle(columns, random);
        columns.sort(Comparator.comparingInt(Column::reach));
        return columns;
    }

    static int perTick(int columns) {
        return Math.max(MIN_COLUMNS_PER_TICK, (columns + WAVE_TICKS - 1) / WAVE_TICKS);
    }

    static int batches(int columns) {
        int perTick = perTick(columns);
        return (columns + perTick - 1) / perTick;
    }

    static List<Column> batch(List<Column> order, int index) {
        int perTick = perTick(order.size());
        int from = Math.min(index * perTick, order.size());
        return order.subList(from, Math.min(from + perTick, order.size()));
    }

    record Column(int x, int z, int reach) {
    }

    static final class Blobs {

        private final long seed;

        Blobs(long seed) {
            this.seed = seed;
        }

        boolean packed(int x, int y, int z) {
            int cellX = Math.floorDiv(x, CELL);
            int cellY = Math.floorDiv(y, CELL);
            int cellZ = Math.floorDiv(z, CELL);
            double best = Double.MAX_VALUE;
            boolean packed = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int cx = cellX + dx;
                        int cy = cellY + dy;
                        int cz = cellZ + dz;
                        int seeds = seeds(cx, cy, cz);
                        for (int index = 0; index < seeds; index++) {
                            int salt = index * 4;
                            double distance = Math.abs(cx * CELL + unit(cx, cy, cz, salt + 1) * CELL - x)
                                + Math.abs(cy * CELL + unit(cx, cy, cz, salt + 2) * CELL - y)
                                + Math.abs(cz * CELL + unit(cx, cy, cz, salt + 3) * CELL - z);
                            if (distance < best) {
                                best = distance;
                                packed = unit(cx, cy, cz, salt + 4) < PACKED_SHARE;
                            }
                        }
                    }
                }
            }
            return packed;
        }

        int seeds(int x, int y, int z) {
            double roll = unit(x, y, z, 0);
            double chance = Math.exp(-SEEDS_PER_CELL);
            double total = chance;
            int seeds = 0;
            while (roll > total && seeds < MAX_SEEDS) {
                seeds++;
                chance *= SEEDS_PER_CELL / seeds;
                total += chance;
            }
            return seeds;
        }

        private double unit(int x, int y, int z, int salt) {
            long mixed = seed ^ (x * 0x632BE59BD9B4E019L) ^ (y * 0x85157AF5L) ^ (z * 0x8CB92BA72F3D8DD7L) ^ (salt * GOLDEN);
            mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
            mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
            mixed = mixed ^ (mixed >>> 31);
            return (mixed >>> 11) * UNIT;
        }
    }
}
