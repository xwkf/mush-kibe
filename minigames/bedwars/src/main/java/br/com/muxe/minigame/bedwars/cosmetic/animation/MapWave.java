package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

final class MapWave {

    static final int HALF_SQUARE = 64;
    static final int SCAN_BELOW = 32;
    static final int SCAN_ABOVE = 36;
    static final int MAX_HEIGHT = 255;

    private final Stage stage;
    private final Speed speed;
    private final UnaryOperator<BlockType> palette;
    private final int originX;
    private final int originZ;
    private final int bottom;
    private final int top;
    private final List<int[]> columns;
    private final Set<Long> held = new HashSet<>();
    private int next;

    MapWave(Stage stage, Scene scene, Speed speed, UnaryOperator<BlockType> palette) {
        this.stage = stage;
        this.speed = speed;
        this.palette = palette;
        this.originX = (int) Math.floor(scene.origin().x());
        this.originZ = (int) Math.floor(scene.origin().z());
        int originY = (int) Math.floor(scene.origin().y());
        this.bottom = Math.max(0, originY - SCAN_BELOW);
        this.top = Math.min(MAX_HEIGHT, originY + SCAN_ABOVE);
        this.columns = order(box(scene.area(), originX, originZ), originX, originZ);
    }

    void step(int tick) {
        int radius = speed.radius(tick);
        List<BlockChange> changes = new ArrayList<>();
        while (next < columns.size() && distance(columns.get(next)[0], columns.get(next)[1], originX, originZ) <= radius) {
            convert(columns.get(next)[0], columns.get(next)[1], changes);
            next++;
        }
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
    }

    boolean done() {
        return next >= columns.size();
    }

    void hold(int x, int y, int z) {
        held.add(key(x, y, z));
    }

    private void convert(int x, int z, List<BlockChange> changes) {
        for (int y = bottom; y <= top; y++) {
            BlockType original = stage.type(x, y, z);
            BlockType converted = palette.apply(original);
            if (!converted.equals(original) && !held.contains(key(x, y, z))) {
                changes.add(new BlockChange(x, y, z, converted.material(), converted.data()));
            }
        }
    }

    static int distance(int x, int z, int centreX, int centreZ) {
        return Math.abs(x - centreX) + Math.abs(z - centreZ);
    }

    static int[] box(Area area, int originX, int originZ) {
        return new int[] {
            Math.min((int) Math.floor(area.minX()), originX - HALF_SQUARE),
            Math.min((int) Math.floor(area.minZ()), originZ - HALF_SQUARE),
            Math.max((int) Math.floor(area.maxX()), originX + HALF_SQUARE),
            Math.max((int) Math.floor(area.maxZ()), originZ + HALF_SQUARE)
        };
    }

    static List<int[]> order(int[] box, int originX, int originZ) {
        List<int[]> columns = new ArrayList<>((box[2] - box[0] + 1) * (box[3] - box[1] + 1));
        for (int x = box[0]; x <= box[2]; x++) {
            for (int z = box[1]; z <= box[3]; z++) {
                columns.add(new int[] {x, z});
            }
        }
        columns.sort(Comparator.comparingInt(column -> distance(column[0], column[1], originX, originZ)));
        return columns;
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
    }

    record Speed(double start, double perTick) {

        int radius(int tick) {
            return (int) Math.floor(start + perTick * tick);
        }
    }
}
