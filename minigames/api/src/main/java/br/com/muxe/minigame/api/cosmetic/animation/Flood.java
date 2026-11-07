package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;

final class Flood {

    static final double TOE_SPEED = 4;
    static final double CREST_SPEED = 2;
    static final int FIRST_RISE = 1;
    static final int RISE_EVERY = 2;
    static final int ABOVE_HIGHEST = 2;
    static final int MAX_HEIGHT = 255;
    static final Set<Material> DROWNED = EnumSet.of(Material.AIR, Material.LONG_GRASS, Material.DOUBLE_PLANT, Material.RED_ROSE, Material.YELLOW_FLOWER);

    private final Stage stage;
    private final int minX;
    private final int minZ;
    private final int width;
    private final int depth;
    private final int top;
    private final int[] distance;
    private final int[] filled;
    private final List<Integer> rising = new ArrayList<>();

    Flood(Stage stage, Area area, Vec facing, double originY) {
        this.stage = stage;
        this.minX = (int) Math.floor(area.minX());
        this.minZ = (int) Math.floor(area.minZ());
        this.width = (int) Math.floor(area.maxX()) - minX + 1;
        this.depth = (int) Math.floor(area.maxZ()) - minZ + 1;
        this.distance = new int[width * depth];
        this.filled = new int[width * depth];
        int highest = (int) Math.floor(originY) - 1;
        for (int column = 0; column < distance.length; column++) {
            int x = minX + column % width;
            int z = minZ + column / width;
            distance[column] = rows(facing, x - minX, z - minZ, width, depth);
            highest = Math.max(highest, stage.highest(x, z));
            rising.add(column);
        }
        this.top = Math.min(MAX_HEIGHT, highest + ABOVE_HIGHEST);
    }

    static int rows(Vec facing, int alongX, int alongZ, int width, int depth) {
        if (Math.abs(facing.x()) > Math.abs(facing.z())) {
            return facing.x() > 0 ? width - 1 - alongX : alongX;
        }
        return facing.z() > 0 ? depth - 1 - alongZ : alongZ;
    }

    static int layers(int distance, int tick, int height) {
        if (tick < FIRST_RISE) {
            return 0;
        }
        double toe = TOE_SPEED * (tick - FIRST_RISE);
        double crest = CREST_SPEED * (tick - FIRST_RISE);
        if (distance <= crest) {
            return height;
        }
        if (distance > toe) {
            return 0;
        }
        return (int) Math.min(height, Math.floor((height - 1) * (toe - distance) / (toe - crest)) + 1);
    }

    static boolean rises(int tick) {
        return tick >= FIRST_RISE && (tick - FIRST_RISE) % RISE_EVERY == 0;
    }

    void rise(int tick) {
        if (!rises(tick)) {
            return;
        }
        int height = top + 1;
        List<BlockChange> changes = new ArrayList<>();
        Iterator<Integer> columns = rising.iterator();
        while (columns.hasNext()) {
            int column = columns.next();
            int target = layers(distance[column], tick, height);
            if (target <= filled[column]) {
                continue;
            }
            int x = minX + column % width;
            int z = minZ + column / width;
            for (int y = filled[column]; y < target; y++) {
                if (DROWNED.contains(stage.type(x, y, z).material())) {
                    changes.add(new BlockChange(x, y, z, Material.STATIONARY_WATER, 0));
                }
            }
            filled[column] = target;
            if (target == height) {
                columns.remove();
            }
        }
        if (!changes.isEmpty()) {
            stage.sections(changes);
        }
    }

    boolean flooded(int x, int y, int z) {
        int column = column(x, z);
        return column >= 0 && y >= 0 && y < filled[column];
    }

    boolean water(int x, int y, int z) {
        return flooded(x, y, z) && !stage.solid(x, y, z);
    }

    boolean done() {
        return rising.isEmpty();
    }

    int top() {
        return top;
    }

    private int column(int x, int z) {
        int dx = x - minX;
        int dz = z - minZ;
        if (dx < 0 || dz < 0 || dx >= width || dz >= depth) {
            return -1;
        }
        return dz * width + dx;
    }
}
