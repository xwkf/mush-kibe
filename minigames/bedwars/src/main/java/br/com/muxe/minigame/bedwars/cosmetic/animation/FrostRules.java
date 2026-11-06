package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class FrostRules {

    static final double PACKED_SHARE = 0.2;
    static final int BLOB = 5;
    static final double EDGE_JITTER = 3;
    static final double SPREAD = 13.8;
    static final int FIRST_TICK = 1;
    static final int LAST_TICK = 150;
    static final BlockType SNOW = new BlockType(Material.SNOW_BLOCK, 0);
    static final BlockType PACKED_ICE = new BlockType(Material.PACKED_ICE, 0);
    static final BlockType ICE = new BlockType(Material.ICE, 0);
    static final BlockType AIR = new BlockType(Material.AIR, 0);
    static final Set<Material> KEPT = EnumSet.of(Material.AIR, Material.BARRIER, Material.BED_BLOCK);
    static final Set<Material> WATER = EnumSet.of(Material.WATER, Material.STATIONARY_WATER);
    static final Set<Material> PLANTS = EnumSet.of(
        Material.LONG_GRASS, Material.YELLOW_FLOWER, Material.RED_ROSE, Material.DOUBLE_PLANT, Material.DEAD_BUSH,
        Material.SAPLING, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM);

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;
    private static final double UNIT = 0x1.0p-53;

    private FrostRules() {
    }

    static Optional<BlockType> frozen(BlockType type, boolean packed) {
        Material material = type.material();
        BlockType frozen;
        if (KEPT.contains(material)) {
            return Optional.empty();
        } else if (WATER.contains(material)) {
            frozen = ICE;
        } else if (PLANTS.contains(material)) {
            frozen = AIR;
        } else if (material.isOccluding()) {
            frozen = packed ? PACKED_ICE : SNOW;
        } else if (material.isSolid()) {
            frozen = ICE;
        } else {
            return Optional.empty();
        }
        return frozen.equals(type) ? Optional.empty() : Optional.of(frozen);
    }

    static boolean ground(BlockType frozen) {
        return frozen.equals(SNOW) || frozen.equals(PACKED_ICE);
    }

    static List<Column> order(Area area, int originX, int originZ, RandomGenerator random) {
        int minX = (int) Math.floor(area.minX());
        int minZ = (int) Math.floor(area.minZ());
        int maxX = (int) Math.floor(area.maxX());
        int maxZ = (int) Math.floor(area.maxZ());
        List<Column> columns = new ArrayList<>((maxX - minX + 1) * (maxZ - minZ + 1));
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                columns.add(new Column(x, z, Math.hypot(x - originX, z - originZ) + random.nextDouble() * EDGE_JITTER));
            }
        }
        columns.sort(Comparator.comparingDouble(Column::reach));
        return columns;
    }

    static double spread(List<Column> order) {
        double farthest = order.isEmpty() ? 0 : order.getLast().reach();
        return Math.max(SPREAD, farthest / Math.sqrt(LAST_TICK - FIRST_TICK));
    }

    static int tick(double reach, double spread) {
        double steps = reach / spread;
        return FIRST_TICK + (int) Math.floor(steps * steps);
    }

    record Column(int x, int z, double reach) {
    }

    static final class Blobs {

        private final long seed;

        Blobs(long seed) {
            this.seed = seed;
        }

        boolean packed(int x, int y, int z) {
            int cellX = Math.floorDiv(x, BLOB);
            int cellY = Math.floorDiv(y, BLOB);
            int cellZ = Math.floorDiv(z, BLOB);
            double best = Double.MAX_VALUE;
            boolean packed = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int cx = cellX + dx;
                        int cy = cellY + dy;
                        int cz = cellZ + dz;
                        double px = cx * BLOB + unit(cx, cy, cz, 1) * BLOB - x;
                        double py = cy * BLOB + unit(cx, cy, cz, 2) * BLOB - y;
                        double pz = cz * BLOB + unit(cx, cy, cz, 3) * BLOB - z;
                        double distance = px * px + py * py + pz * pz;
                        if (distance < best) {
                            best = distance;
                            packed = unit(cx, cy, cz, 4) < PACKED_SHARE;
                        }
                    }
                }
            }
            return packed;
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
