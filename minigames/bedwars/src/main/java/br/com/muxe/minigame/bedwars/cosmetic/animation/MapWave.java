package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

final class MapWave {

    static final int HALF_SQUARE = 64;
    static final int COLUMNS_PER_TICK = 256;
    static final int BOTTOM = 0;
    static final int TOP = 127;

    private final Stage stage;
    private final Palette palette;
    private final int originX;
    private final int originZ;
    private final List<int[]> columns;
    private final Set<Long> held = new HashSet<>();
    private int next;
    private int front = -1;

    MapWave(Stage stage, Scene scene, Palette palette, RandomGenerator random) {
        this.stage = stage;
        this.palette = palette;
        this.originX = (int) Math.floor(scene.origin().x());
        this.originZ = (int) Math.floor(scene.origin().z());
        this.columns = order(box(scene.area(), originX, originZ), originX, originZ, random);
    }

    void step() {
        List<BlockChange> changes = new ArrayList<>();
        int last = Math.min(columns.size(), next + COLUMNS_PER_TICK);
        for (; next < last; next++) {
            int[] column = columns.get(next);
            convert(column[0], column[1], changes);
            front = distance(column[0], column[1], originX, originZ);
        }
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
    }

    boolean done() {
        return next >= columns.size();
    }

    int front() {
        return front;
    }

    int originX() {
        return originX;
    }

    int originZ() {
        return originZ;
    }

    void hold(int x, int y, int z) {
        held.add(key(x, y, z));
    }

    private void convert(int x, int z, List<BlockChange> changes) {
        for (int y = BOTTOM; y <= TOP; y++) {
            BlockType original = stage.type(x, y, z);
            BlockType converted = palette.apply(x, y, z, original);
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

    static List<int[]> order(int[] box, int originX, int originZ, RandomGenerator random) {
        List<int[]> columns = new ArrayList<>((box[2] - box[0] + 1) * (box[3] - box[1] + 1));
        for (int x = box[0]; x <= box[2]; x++) {
            for (int z = box[1]; z <= box[3]; z++) {
                columns.add(new int[] {x, z});
            }
        }
        Collections.shuffle(columns, random);
        columns.sort(Comparator.comparingInt(column -> distance(column[0], column[1], originX, originZ)));
        return columns;
    }

    static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
    }

    @FunctionalInterface
    interface Palette {

        BlockType apply(int x, int y, int z, BlockType type);
    }
}
