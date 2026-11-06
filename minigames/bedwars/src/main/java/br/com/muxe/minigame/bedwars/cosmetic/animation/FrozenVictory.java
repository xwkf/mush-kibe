package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.bedwars.cosmetic.animation.FrostRules.Blobs;
import br.com.muxe.minigame.bedwars.cosmetic.animation.FrostRules.Column;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class FrozenVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int SCAN_BELOW = 26;
    static final int SCAN_ABOVE = 30;
    static final int MAX_HEIGHT = 255;
    static final double SNOWMAN_CHANCE = 1.0 / 300;
    static final int MAX_SNOWMEN = 12;
    static final int SNOWMAN_DELAY = 20;
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
        Wave wave = new Wave(stage, timeline, random, order, Math.max(0, originY - SCAN_BELOW), Math.min(MAX_HEIGHT, originY + SCAN_ABOVE));
        timeline.lasts(LENGTH);
        timeline.at(0, () -> stage.sound("mob.enderdragon.growl", feet(scene, stage), 3, 1));
        timeline.repeat(LAUNCH_FIRST, LAUNCH_LAST, LAUNCH_EVERY, offset -> stage.sound("fireworks.launch", feet(scene, stage), 3, 1));
        timeline.repeat(FrostRules.FIRST_TICK, wave.lastTick(), 1, offset -> wave.advance(FrostRules.FIRST_TICK + offset));
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
        private final Timeline timeline;
        private final RandomGenerator random;
        private final List<Column> order;
        private final double spread;
        private final Blobs blobs;
        private final int bottom;
        private final int top;
        private final List<SnowmanStroll> snowmen = new ArrayList<>();
        private int next;
        private int planned;

        Wave(Stage stage, Timeline timeline, RandomGenerator random, List<Column> order, int bottom, int top) {
            this.stage = stage;
            this.timeline = timeline;
            this.random = random;
            this.order = order;
            this.spread = FrostRules.spread(order);
            this.blobs = new Blobs(random.nextLong());
            this.bottom = bottom;
            this.top = top;
        }

        int lastTick() {
            return order.isEmpty() ? FrostRules.FIRST_TICK : FrostRules.tick(order.getLast().reach(), spread);
        }

        void advance(int tick) {
            int from = next;
            while (next < order.size() && FrostRules.tick(order.get(next).reach(), spread) <= tick) {
                next++;
            }
            freeze(order.subList(from, next), tick);
        }

        private void freeze(List<Column> columns, int tick) {
            List<BlockChange> changes = new ArrayList<>();
            for (Column column : columns) {
                int surface = freezeColumn(column, changes);
                if (surface >= 0 && planned < MAX_SNOWMEN && random.nextDouble() < SNOWMAN_CHANCE) {
                    planned++;
                    plantSnowman(new Vec(column.x() + 0.5, surface, column.z() + 0.5), tick + random.nextInt(SNOWMAN_DELAY + 1));
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
                boolean packed = type.material().isOccluding() && blobs.packed(column.x(), y, column.z());
                Optional<BlockType> frozen = FrostRules.frozen(type, packed);
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

        private void plantSnowman(Vec spot, int when) {
            if (when >= LENGTH) {
                return;
            }
            timeline.at(when, () -> {
                float yaw = (float) (random.nextDouble() * 360);
                snowmen.add(new SnowmanStroll(stage.mob("SnowMan", spot, yaw), spot, yaw));
            });
        }
    }
}
