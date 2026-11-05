package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class PumpkinHarvestFinalKill implements SceneAnimation {

    static final double[] PIXEL = {0.05, 0.078, 0.105, 0.11, 0.115, 0.119, 0.121, 0.122, 0.123, 0.125, 0.126, 0.128, 0.129, 0.1305, 0.132, 0.1335, 0.135, 0.1365};
    static final int BURST = PIXEL.length;
    static final double CENTER = 2.2;
    static final double SOUND_HEIGHT = 2.12;
    static final double LAUNCH_HEIGHT = 2.1875;
    static final int SHAKE_FROM = 8;
    static final double SHAKE = 0.03;
    static final int[] BEATS = {8, 10, 13, 15};
    static final float[] PLING = {0.79f, 1.1f, 1.4f, 1.7f};
    static final int FADE_FROM = 21;
    static final int FADE_UNTIL = 29;
    static final int SEEDS = 12;
    static final double SEED_SPEED = 0.2;
    static final double SEED_MIN_LIFT = 0.055;
    static final double SEED_MAX_LIFT = 0.19;
    static final int[] THROWS = {20, 21, 24, 25, 28, 30, 31, 33, 36, 37};
    static final Material[] HARVEST = {Material.JACK_O_LANTERN, Material.PUMPKIN, Material.PUMPKIN_PIE};
    static final float FIRST_PLOP = 0.89f;
    static final float PLOP_STEP = 0.1f;
    static final double THROW_MAX_SPEED = 0.12;
    static final double THROW_MIN_LIFT = 0.45;
    static final double THROW_MAX_LIFT = 0.6;
    static final int END = 79;

    @Override
    public String id() {
        return "colheita-de-aboboras";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec center = origin.add(0, CENTER, 0);
        Vec voice = origin.add(0, SOUND_HEIGHT, 0);
        Vec right = scene.right();
        int pumpkin = Kit.blockId(Material.PUMPKIN, 0);
        stage.particle("BLOCK_DUST", origin.add(0, 0.2, 0), 15, new Vec(0.3, 0.1, 0.3), 0.1, pumpkin);
        stage.sound("dig.wood", origin, 1, 0.79f);
        stage.sound("dig.grass", origin, 1, 0.7f);
        stage.sound("random.pop", voice, 1, 0.59f);
        stage.sound("mob.ghast.moan", voice, 0.5f, 1.49f);
        timeline.repeat(0, BURST - 1, 1, tick -> {
            Vec at = tick < SHAKE_FROM ? center : center.add(right.multiply(Kit.between(random, -SHAKE, SHAKE)));
            draw(stage, at, right, PIXEL[tick]);
            if (tick >= SHAKE_FROM) {
                Vec flame = center.add(right.multiply(Kit.between(random, -0.55, 0.55))).add(0, Kit.between(random, -0.7, 0.4), 0);
                stage.particle("FLAME", flame, 1, Vec.ZERO, 0.01);
            }
        });
        for (int beat = 0; beat < BEATS.length; beat++) {
            float pitch = PLING[beat];
            timeline.at(BEATS[beat], () -> stage.sound("note.pling", voice, 0.6f, pitch));
        }
        timeline.at(BURST, () -> {
            stage.particle("EXPLOSION_LARGE", center, 1, Vec.ZERO, 1);
            stage.particle("FLAME", center, 20, Vec.ZERO, 0.15);
            stage.particle("BLOCK_DUST", center, 30, new Vec(0.5, 0.5, 0.5), 0.1, pumpkin);
            stage.sound("random.explode", voice, 0.6f, 1.49f);
            stage.sound("dig.wood", voice, 1, 0.59f);
            stage.sound("mob.chicken.plop", voice, 1, 0.59f);
        });
        scatter(stage, timeline, center, right, scene.facing(), random);
        List<Actor> items = new ArrayList<>();
        Vec launch = origin.add(0, LAUNCH_HEIGHT, 0);
        double first = random.nextDouble() * 360;
        for (int seed = 0; seed < SEEDS; seed++) {
            Vec velocity = Kit.horizontal(first + seed * 360.0 / SEEDS, SEED_SPEED).add(0, Kit.between(random, SEED_MIN_LIFT, SEED_MAX_LIFT), 0);
            timeline.at(BURST, () -> {
                Actor item = stage.item(launch, Material.PUMPKIN_SEEDS, 0);
                item.velocity(velocity);
                items.add(item);
            });
        }
        for (int index = 0; index < THROWS.length; index++) {
            harvest(stage, timeline, launch, center, index, random, items);
        }
        timeline.at(END, () -> items.forEach(Actor::remove));
    }

    static Vec pixel(Vec center, Vec right, int column, int row, double size) {
        double columns = (PumpkinHarvestAnimation.LANTERN.getFirst().length() - 1) / 2.0;
        double rows = (PumpkinHarvestAnimation.LANTERN.size() - 1) / 2.0;
        return center.add(right.multiply((column - columns) * size)).add(0, (rows - row) * size, 0);
    }

    static float plop(int index) {
        return FIRST_PLOP + index * PLOP_STEP;
    }

    private static void draw(Stage stage, Vec center, Vec right, double size) {
        List<String> rows = PumpkinHarvestAnimation.LANTERN;
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < rows.get(row).length(); column++) {
                double[] color = PumpkinHarvestAnimation.PALETTE.get(rows.get(row).charAt(column));
                if (color != null) {
                    Kit.dust(stage, pixel(center, right, column, row, size), color[0], color[1], color[2]);
                }
            }
        }
    }

    private static void scatter(Stage stage, Timeline timeline, Vec center, Vec right, Vec forward, RandomGenerator random) {
        List<Pixel> pixels = new ArrayList<>();
        List<String> rows = PumpkinHarvestAnimation.LANTERN;
        for (int row = 0; row < rows.size(); row++) {
            for (int column = 0; column < rows.get(row).length(); column++) {
                double[] color = PumpkinHarvestAnimation.PALETTE.get(rows.get(row).charAt(column));
                if (color != null) {
                    Vec start = pixel(center, right, column, row, PIXEL[BURST - 1]);
                    Vec velocity = start.subtract(center).multiply(0.5).add(forward.multiply(Kit.between(random, -0.35, 0.35)));
                    pixels.add(new Pixel(start, velocity, color, FADE_FROM + random.nextInt(FADE_UNTIL - FADE_FROM + 1)));
                }
            }
        }
        timeline.repeat(BURST, FADE_UNTIL, 1, offset -> {
            for (Pixel pixel : pixels) {
                if (BURST + offset < pixel.dies) {
                    pixel.position = pixel.position.add(pixel.velocity);
                    pixel.velocity = pixel.velocity.multiply(0.88).add(0, -0.015, 0);
                    Kit.dust(stage, pixel.position, pixel.color[0], pixel.color[1], pixel.color[2]);
                }
            }
        });
    }

    private static void harvest(Stage stage, Timeline timeline, Vec launch, Vec center, int index, RandomGenerator random, List<Actor> items) {
        int tick = THROWS[index];
        Vec velocity = Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * THROW_MAX_SPEED).add(0, Kit.between(random, THROW_MIN_LIFT, THROW_MAX_LIFT), 0);
        Material material = HARVEST[random.nextInt(HARVEST.length)];
        Body body = Body.item(launch, velocity);
        timeline.at(tick, () -> {
            Actor item = stage.item(launch, material, 0);
            item.velocity(velocity);
            items.add(item);
            stage.sound("mob.chicken.plop", launch, 0.8f, plop(index));
            stage.particle("FLAME", center, 1, Vec.ZERO, 0.01);
        });
        timeline.repeat(tick + 1, END - 1, 1, offset -> {
            if (!body.landed() && body.step(stage)) {
                Vec at = body.position();
                stage.particle("BLOCK_DUST", at.add(0, 0.1, 0), 6, new Vec(0.2, 0.05, 0.2), 0.1, Kit.blockId(Material.PUMPKIN, 0));
                stage.sound("dig.wood", at, 0.7f, (float) Kit.between(random, 0.81, 1.08));
                stage.sound("random.pop", at, 0.4f, (float) Kit.between(random, 1.6, 1.98));
            }
        });
    }

    private static final class Pixel {

        private final double[] color;
        private final int dies;
        private Vec position;
        private Vec velocity;

        Pixel(Vec position, Vec velocity, double[] color, int dies) {
            this.position = position;
            this.velocity = velocity;
            this.color = color;
            this.dies = dies;
        }
    }
}
