package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.FrostRules.Blobs;
import br.com.muxe.minigame.api.cosmetic.animation.FrostRules.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class FrozenVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int SCAN_BELOW = 27;
    static final int SCAN_ABOVE = 36;
    static final int MAX_HEIGHT = 255;
    static final double SNOWMAN_CHANCE = 1.0 / 100;
    static final int MAX_SNOWMEN = 4;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final int LAUNCH_LAST = 191;

    @Override
    public String id() {
        return "congelado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        int originY = (int) Math.floor(origin.y());
        List<Column> order = FrostRules.order(scene.area(), (int) Math.floor(origin.x()), (int) Math.floor(origin.z()), random);
        Wave wave = new Wave(stage, random, Math.max(0, originY - SCAN_BELOW), Math.min(MAX_HEIGHT, originY + SCAN_ABOVE));
        timeline.lasts(LENGTH);
        timeline.at(0, () -> stage.sound("mob.enderdragon.growl", feet(scene, stage), 3, 1));
        timeline.repeat(LAUNCH_FIRST, LAUNCH_LAST, LAUNCH_EVERY, offset -> stage.sound("fireworks.launch", feet(scene, stage), 3, 1));
        timeline.repeat(FrostRules.FIRST_TICK, FrostRules.FIRST_TICK + Math.max(1, FrostRules.batches(order.size())) - 1, 1,
            batch -> wave.freeze(FrostRules.batch(order, batch)));
        timeline.repeat(0, LENGTH - 1, 1, tick -> wave.snowmen.forEach(snowman -> snowman.step(stage, random)));
        timeline.at(LENGTH, () -> {
            wave.snowmen.forEach(SnowmanStroll::remove);
            wave.snowmen.clear();
        });
    }

    private static Vec feet(Scene scene, Stage stage) {
        return scene.riders().stream().findFirst().flatMap(stage::eyes).map(eyes -> eyes.add(0, -Riders.EYE_HEIGHT, 0)).orElse(scene.origin());
    }

    private static final class Wave {

        private final Stage stage;
        private final RandomGenerator random;
        private final Blobs blobs;
        private final int bottom;
        private final int top;
        private final List<SnowmanStroll> snowmen = new ArrayList<>();
        private int planned;

        Wave(Stage stage, RandomGenerator random, int bottom, int top) {
            this.stage = stage;
            this.random = random;
            this.blobs = new Blobs(random.nextLong());
            this.bottom = bottom;
            this.top = top;
        }

        BlockType frozenAt(int x, int y, int z) {
            BlockType type = stage.type(x, y, z);
            return FrostRules.frozen(type, packed(type, x, y, z)).orElse(type);
        }

        void freeze(List<Column> columns) {
            List<BlockChange> changes = new ArrayList<>();
            for (Column column : columns) {
                int surface = freezeColumn(column, changes);
                if (surface >= 0 && planned < MAX_SNOWMEN && random.nextDouble() < SNOWMAN_CHANCE) {
                    planned++;
                    plantSnowman(new Vec(column.x() + 0.5, surface, column.z() + 0.5));
                }
            }
            if (!changes.isEmpty()) {
                stage.blocks(changes);
            }
        }

        private int freezeColumn(Column column, List<BlockChange> changes) {
            int surface = -1;
            BlockType above = stage.type(column.x(), top + 1, column.z());
            for (int y = top; y >= bottom; y--) {
                BlockType type = stage.type(column.x(), y, column.z());
                Optional<BlockType> frozen = FrostRules.frozen(type, packed(type, column.x(), y, column.z()));
                if (frozen.isPresent()) {
                    changes.add(new BlockChange(column.x(), y, column.z(), frozen.get().material(), frozen.get().data()));
                }
                BlockType now = frozen.orElse(type);
                if (surface < 0 && FrostRules.ground(now) && above.material() == Material.AIR) {
                    surface = y + 1;
                }
                above = now;
            }
            return surface;
        }

        private boolean packed(BlockType type, int x, int y, int z) {
            return FrostRules.cube(type.material()) && blobs.packed(x, y, z);
        }

        private void plantSnowman(Vec spot) {
            float yaw = (float) (random.nextDouble() * 360);
            snowmen.add(new SnowmanStroll(stage.mob("SnowMan", spot, yaw), spot, yaw, this::frozenAt));
        }
    }
}
