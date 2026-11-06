package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Material;

final class Flood {

    static final double TOE_SPEED = 4;
    static final int TOE_DELAY = 1;
    static final double CREST_SPEED = 2.25;
    static final int CREST_DELAY = 10;
    static final int LAYERS = 64;
    static final int ABOVE_ORIGIN = 36;
    static final int MAX_HEIGHT = 255;

    private final Stage stage;
    private final int minX;
    private final int minZ;
    private final int width;
    private final int depth;
    private final int top;
    private final int bottom;
    private final double[] distance;
    private final int[] filled;
    private final List<Integer> rising = new ArrayList<>();

    Flood(Stage stage, Area area, Vec facing, double originY) {
        this.stage = stage;
        this.minX = (int) Math.floor(area.minX());
        this.minZ = (int) Math.floor(area.minZ());
        this.width = (int) Math.floor(area.maxX()) - minX + 1;
        this.depth = (int) Math.floor(area.maxZ()) - minZ + 1;
        this.top = Math.min(MAX_HEIGHT, (int) Math.floor(originY) + ABOVE_ORIGIN);
        this.bottom = Math.max(0, top - LAYERS + 1);
        this.distance = new double[width * depth];
        this.filled = new int[width * depth];
        double edge = leadingEdge(area, facing);
        for (int column = 0; column < distance.length; column++) {
            double x = minX + column % width + 0.5;
            double z = minZ + column / width + 0.5;
            distance[column] = edge - (x * facing.x() + z * facing.z());
            rising.add(column);
        }
    }

    static int layers(double distance, int tick) {
        double toe = TOE_SPEED * (tick - TOE_DELAY);
        double crest = CREST_SPEED * (tick - CREST_DELAY);
        if (distance <= crest) {
            return LAYERS;
        }
        if (distance >= toe) {
            return 0;
        }
        return (int) Math.min(LAYERS, Math.ceil(LAYERS * (toe - distance) / (toe - crest)));
    }

    static double leadingEdge(Area area, Vec facing) {
        double alongX = Math.max(area.minX() * facing.x(), area.maxX() * facing.x());
        double alongZ = Math.max(area.minZ() * facing.z(), area.maxZ() * facing.z());
        return alongX + alongZ;
    }

    void rise(int tick) {
        List<BlockChange> changes = new ArrayList<>();
        Iterator<Integer> columns = rising.iterator();
        while (columns.hasNext()) {
            int column = columns.next();
            int target = Math.min(layers(distance[column], tick), top - bottom + 1);
            if (target <= filled[column]) {
                continue;
            }
            int x = minX + column % width;
            int z = minZ + column / width;
            for (int layer = filled[column]; layer < target; layer++) {
                int y = bottom + layer;
                if (stage.type(x, y, z).material() == Material.AIR) {
                    changes.add(new BlockChange(x, y, z, Material.STATIONARY_WATER, 0));
                }
            }
            filled[column] = target;
            if (target == top - bottom + 1) {
                columns.remove();
            }
        }
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
    }

    boolean flooded(int x, int y, int z) {
        int column = column(x, z);
        return column >= 0 && y >= bottom && y < bottom + filled[column];
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
