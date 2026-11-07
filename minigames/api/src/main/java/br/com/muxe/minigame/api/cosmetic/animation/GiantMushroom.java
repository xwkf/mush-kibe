package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

final class GiantMushroom {

    static final int MIN_HEIGHT = 4;
    static final int MAX_HEIGHT = 6;
    static final int FIRST_STEP = 2;
    static final int STEP_EVERY = 3;
    static final int STEM = 10;
    static final int CAP = 14;
    static final int BROWN_RADIUS = 3;
    static final int RING_RADIUS = 2;
    static final int RING_LAYERS = 3;
    static final int TOP_RADIUS = 1;
    static final double FIRST_PITCH = 0.6;
    static final double PITCH_STEP = 0.12;
    static final float GROW_VOLUME = 0.8f;
    static final float DONE_PITCH = 1.6f;
    static final Vec SPARKLE_SPREAD = new Vec(0.4, 0.4, 0.4);

    private final List<List<BlockChange>> layers;

    GiantMushroom(int x, int y, int z, int height, boolean red) {
        this.layers = layers(x, y, z, height, red);
    }

    static List<List<BlockChange>> layers(int x, int y, int z, int height, boolean red) {
        List<List<BlockChange>> layers = new ArrayList<>();
        for (int up = 0; up < height; up++) {
            layers.add(List.of(new BlockChange(x, y + up, z, Material.HUGE_MUSHROOM_2, STEM)));
        }
        if (red) {
            for (int ring = 0; ring < RING_LAYERS; ring++) {
                layers.add(square(x, y + height - RING_LAYERS + ring, z, RING_RADIUS, true, Material.HUGE_MUSHROOM_2));
            }
            layers.add(square(x, y + height, z, TOP_RADIUS, false, Material.HUGE_MUSHROOM_2));
        } else {
            layers.add(square(x, y + height, z, BROWN_RADIUS, false, Material.HUGE_MUSHROOM_1));
        }
        return layers;
    }

    static int delay(int step) {
        return step == 0 ? 0 : STEP_EVERY * step - 1;
    }

    static float pitch(int step) {
        return (float) (FIRST_PITCH + PITCH_STEP * step);
    }

    int steps() {
        return layers.size();
    }

    void grow(Stage stage, Grove grove, int step) {
        List<BlockChange> placed = new ArrayList<>();
        for (BlockChange change : layers.get(step)) {
            if (grove.free(change.x(), change.y(), change.z())) {
                placed.add(change);
            }
        }
        if (placed.isEmpty()) {
            return;
        }
        grove.grow(placed);
        placed.forEach(change -> stage.particle("VILLAGER_HAPPY", center(change), 2, SPARKLE_SPREAD, 1));
        Vec last = center(placed.getLast());
        stage.sound("dig.grass", last, GROW_VOLUME, pitch(step));
        if (step == layers.size() - 1) {
            stage.sound("note.pling", last, GROW_VOLUME, DONE_PITCH);
            stage.particle("FIREWORKS_SPARK", last, 15, Vec.ZERO, 0.1);
        }
    }

    private static List<BlockChange> square(int x, int y, int z, int radius, boolean ringOnly, Material material) {
        List<BlockChange> blocks = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                boolean corner = Math.abs(dx) == radius && Math.abs(dz) == radius;
                boolean edge = Math.abs(dx) == radius || Math.abs(dz) == radius;
                if ((radius > TOP_RADIUS && corner) || (ringOnly && !edge)) {
                    continue;
                }
                blocks.add(new BlockChange(x + dx, y, z + dz, material, CAP));
            }
        }
        return blocks;
    }

    private static Vec center(BlockChange change) {
        return new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5);
    }
}
