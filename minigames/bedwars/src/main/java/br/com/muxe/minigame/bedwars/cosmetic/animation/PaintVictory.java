package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class PaintVictory implements SceneAnimation {

    static final String BUCKET = "8ee7b4a3202b5b59edac14977b318890a4c775f11d4bee05348b1a6cb9429f1b";
    static final int BUCKETS = 22;
    static final int FIRST_BUCKET = 2;
    static final int BUCKET_EVERY = 8;
    static final int FALL_TICKS = 22;
    static final int VOID_TICKS = 40;
    static final double DROP = 12;
    static final double BUCKET_BELOW_GROUND = 1.44;
    static final double WOOL_BELOW_GROUND = 1.38;
    static final double MIN_FORWARD = 1.5;
    static final double MAX_FORWARD = 8;
    static final double MAX_SIDE = 7;
    static final double MIN_DISTANCE = 2.5;
    static final int[] WOOL = {5, 4, 11, 14};
    static final double[][] WOOL_COLORS = {{0, 1, 0}, {1, 1, 0}, {0, 0, 1}, {1, 0, 0}};
    static final float[] POP_PITCH = {0.79f, 0.89f, 1f, 1.19f};
    static final int WALKER_EVERY = 2;
    static final int WALKER_LIFE = 17;
    static final int WALKER_LIFE_STEP = 5;
    static final double WALK_DISTANCE = 2.2;
    static final double STAR_HEIGHT = 1.6;
    static final double STAR_OUTER = 1.1;
    static final double STAR_INNER = 0.45;
    static final double STAR_PIXEL = 0.12;
    static final int[] ROCKET_DELAYS = {13, 20, 26, 33};
    static final int[] ROCKET_COLORS = {0x00FF00, 0xFFFF00, 0xFF0000, 0x0000FF};
    static final int[] ROCKET_WOOL = {5, 4, 14, 11};
    static final double PAINT_CHANCE = 0.8;
    static final double SPLAT_DISTANCE = 2;
    static final int[] FINALE_COLORS = {0xB400C7, 0x4DF06F, 0x60D62F, 0x65A81F};
    static final double FINALE_HEIGHT = 3.2;
    static final int SPARKLER_LIFE = 48;
    static final int LENGTH = 240;

    @Override
    public String id() {
        return "tinta";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec forward = flat(scene.facing());
        Vec right = new Vec(-forward.z(), 0, forward.x());
        for (int bucket = 0; bucket < BUCKETS; bucket++) {
            Vec spot = spot(origin, forward, right, random);
            int start = FIRST_BUCKET + bucket * BUCKET_EVERY;
            OptionalDouble ground = Ground.hit(stage::solid, spot.x(), origin.y() + DROP, spot.z(), (int) DROP + Ground.DEPTH);
            if (ground.isPresent()) {
                bucket(stage, timeline, random, origin, new Vec(spot.x(), ground.getAsDouble(), spot.z()), start, bucket);
            } else {
                lost(stage, timeline, random, new Vec(spot.x(), origin.y() - BUCKET_BELOW_GROUND, spot.z()), start);
            }
        }
        timeline.lasts(LENGTH);
    }

    static Vec flat(Vec facing) {
        Vec flat = new Vec(facing.x(), 0, facing.z()).normalize();
        return flat.length() == 0 ? new Vec(0, 0, 1) : flat;
    }

    static Vec spot(Vec origin, Vec forward, Vec right, RandomGenerator random) {
        Vec spot = origin;
        for (int attempt = 0; attempt < 6; attempt++) {
            spot = origin.add(forward.multiply(Kit.between(random, MIN_FORWARD, MAX_FORWARD))).add(right.multiply(Kit.between(random, -MAX_SIDE, MAX_SIDE)));
            if (new Vec(spot.x() - origin.x(), 0, spot.z() - origin.z()).length() >= MIN_DISTANCE) {
                return spot;
            }
        }
        return spot;
    }

    static double fallen(int tick) {
        return falling(Math.min(tick, FALL_TICKS));
    }

    static double falling(int tick) {
        double acceleration = 2 * DROP / (FALL_TICKS * (FALL_TICKS + 1.0));
        int clamped = Math.max(tick, 0);
        return acceleration * clamped * (clamped + 1) / 2;
    }

    private static void lost(Stage stage, Timeline timeline, RandomGenerator random, Vec below, int start) {
        Actor[] box = new Actor[1];
        timeline.at(start, () -> {
            box[0] = stage.armorStand(below.add(0, DROP, 0), 0, StandStyle.HIDDEN);
            box[0].equipSkull(Slot.HELMET, BUCKET);
        });
        timeline.repeat(start + 1, start + VOID_TICKS, 1, offset -> {
            int tick = offset + 1;
            Vec at = below.add(0, DROP - falling(tick), 0);
            box[0].teleport(at, 0, 0);
            if (tick % 2 == 0) {
                trail(stage, random, at);
            }
        });
        timeline.at(start + VOID_TICKS + 1, () -> box[0].remove());
    }

    private static void trail(Stage stage, RandomGenerator random, Vec at) {
        for (double[] color : WOOL_COLORS) {
            Kit.dust(stage, at.add(Kit.between(random, -0.25, 0.25), Kit.between(random, 1.65, 2.2), Kit.between(random, -0.25, 0.25)), color[0], color[1], color[2]);
        }
    }

    private static void bucket(Stage stage, Timeline timeline, RandomGenerator random, Vec origin, Vec ground, int start, int index) {
        Vec landed = ground.add(0, -BUCKET_BELOW_GROUND, 0);
        Actor[] box = new Actor[1];
        timeline.at(start, () -> {
            box[0] = stage.armorStand(landed.add(0, DROP, 0), 0, StandStyle.HIDDEN);
            box[0].equipSkull(Slot.HELMET, BUCKET);
        });
        timeline.repeat(start + 1, start + FALL_TICKS, 1, offset -> {
            int tick = offset + 1;
            Vec at = landed.add(0, DROP - fallen(tick), 0);
            box[0].teleport(at, 0, 0);
            if (tick % 2 == 0) {
                trail(stage, random, at);
            }
        });
        int landing = start + FALL_TICKS;
        timeline.at(landing, () -> {
            stage.sound("random.anvil_land", ground, 1, 1.59f);
            stage.sound("random.splash", ground, 1, 1.19f);
            stage.sound("mob.slime.small", ground, 1, 0.79f);
            for (int speck = 0; speck < 24; speck++) {
                double[] color = WOOL_COLORS[speck % WOOL_COLORS.length];
                Vec around = ground.add(Kit.horizontal(random.nextDouble() * 360, Kit.between(random, 0.3, 1.5))).add(0, 0.1, 0);
                Kit.dust(stage, around, color[0], color[1], color[2]);
            }
        });
        timeline.at(landing + 1, () -> box[0].remove());
        double angle = random.nextDouble() * 360;
        for (int walker = 0; walker < WOOL.length; walker++) {
            walker(stage, timeline, random, origin, ground, landing + walker * WALKER_EVERY, angle + walker * 90, (walker + index) % WOOL.length, walker);
        }
        for (int rocket = 0; rocket < ROCKET_DELAYS.length; rocket++) {
            int tick = landing + ROCKET_DELAYS[rocket];
            int shade = (rocket + index) % ROCKET_COLORS.length;
            Kit.flash(stage, timeline, tick, ground.add(0, 0.5, 0), Kit.burst(Kit.STAR, false, false, ROCKET_COLORS[shade]));
            timeline.at(tick, () -> paint(stage, random, ground, ROCKET_WOOL[shade]));
            sparkler(stage, timeline, random, ground.add(0, 0.5, 0), tick);
        }
        Kit.flash(stage, timeline, landing + ROCKET_DELAYS[ROCKET_DELAYS.length - 1], ground.add(0, FINALE_HEIGHT, 0), Kit.burst(Kit.STAR, false, false, FINALE_COLORS));
    }

    static void paint(Stage stage, RandomGenerator random, Vec ground, int wool) {
        int centerX = (int) Math.floor(ground.x());
        int centerZ = (int) Math.floor(ground.z());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0 || random.nextDouble() < PAINT_CHANCE) {
                    stain(stage, centerX + dx, ground.y(), centerZ + dz, wool);
                }
            }
        }
        Vec splat = ground.add(Kit.horizontal(random.nextDouble() * 360, SPLAT_DISTANCE));
        stain(stage, (int) Math.floor(splat.x()), ground.y(), (int) Math.floor(splat.z()), wool);
    }

    private static void stain(Stage stage, int x, double near, int z, int wool) {
        double top = Ground.surface(stage::solid, x + 0.5, near, z + 0.5);
        int y = (int) Math.floor(top) - 1;
        if (stage.solid(x, y, z)) {
            stage.block(x, y, z, Material.WOOL, wool);
        }
    }

    private static void walker(Stage stage, Timeline timeline, RandomGenerator random, Vec origin, Vec ground, int start, double degrees, int wool, int order) {
        int life = WALKER_LIFE + order * WALKER_LIFE_STEP;
        Vec from = ground.add(0, -WOOL_BELOW_GROUND, 0);
        Vec step = Kit.horizontal(degrees, WALK_DISTANCE);
        double hue = 300 + order * 18;
        Actor[] stand = new Actor[1];
        timeline.at(start, () -> {
            stand[0] = stage.armorStand(from, (float) degrees, new StandStyle(true, false, false, false, false));
            stand[0].equip(Slot.HELMET, Material.WOOL, WOOL[wool]);
            stage.sound("random.pop", ground, 1, POP_PITCH[order]);
            stage.sound("mob.chicken.plop", ground, 1, POP_PITCH[order] + 0.1f);
        });
        timeline.repeat(start + 1, start + life, 1, offset -> {
            int tick = offset + 1;
            double progress = 1 - Math.pow(1 - (double) tick / life, 2);
            Vec at = from.add(step.multiply(progress));
            stand[0].teleport(at, (float) degrees, 0);
            Vec top = at.add(0, WOOL_BELOW_GROUND, 0);
            if (tick == 1 || tick == 3) {
                star(stage, top.add(0, STAR_HEIGHT, 0), origin, hue);
            }
            if (tick % 2 == 0 && tick <= 10) {
                double[] color = WOOL_COLORS[wool];
                for (double up = 0; up < 4; up += 0.25) {
                    Kit.dust(stage, top.add(Kit.between(random, -0.08, 0.08), up, Kit.between(random, -0.08, 0.08)), color[0], color[1], color[2]);
                }
            }
            if (tick % 6 == 0) {
                stage.sound("dig.cloth", top, 0.6f, (float) Kit.between(random, 0.8, 1.2));
            }
        });
        timeline.at(start + life + 1, () -> stand[0].remove());
    }

    static void star(Stage stage, Vec center, Vec viewer, double hue) {
        Vec towards = new Vec(viewer.x() - center.x(), 0, viewer.z() - center.z()).normalize();
        Vec across = towards.length() == 0 ? new Vec(1, 0, 0) : new Vec(-towards.z(), 0, towards.x());
        List<double[]> pixels = starPixels(STAR_OUTER, STAR_INNER, STAR_PIXEL);
        for (double[] pixel : pixels) {
            double shade = (pixel[1] + STAR_OUTER) / (2 * STAR_OUTER);
            double[] color = Kit.hsv(hue + shade * 40, 0.75, 0.85);
            Kit.dust(stage, center.add(across.multiply(pixel[0])).add(0, pixel[1], 0), color[0], color[1], color[2]);
        }
    }

    static List<double[]> starPixels(double outer, double inner, double spacing) {
        double[][] corners = new double[10][];
        for (int corner = 0; corner < 10; corner++) {
            double radius = corner % 2 == 0 ? outer : inner;
            double radians = Math.toRadians(90 + corner * 36);
            corners[corner] = new double[] {Math.cos(radians) * radius, Math.sin(radians) * radius};
        }
        List<double[]> pixels = new ArrayList<>();
        int steps = (int) Math.ceil(outer / spacing);
        for (int column = -steps; column <= steps; column++) {
            for (int row = -steps; row <= steps; row++) {
                double x = column * spacing;
                double y = row * spacing;
                if (inside(corners, x, y)) {
                    pixels.add(new double[] {x, y});
                }
            }
        }
        return pixels;
    }

    static boolean inside(double[][] polygon, double x, double y) {
        boolean inside = false;
        for (int current = 0, previous = polygon.length - 1; current < polygon.length; previous = current++) {
            double[] a = polygon[current];
            double[] b = polygon[previous];
            if ((a[1] > y) != (b[1] > y) && x < (b[0] - a[0]) * (y - a[1]) / (b[1] - a[1]) + a[0]) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static void sparkler(Stage stage, Timeline timeline, RandomGenerator random, Vec from, int start) {
        Body body = Body.item(from, new Vec(Kit.between(random, -0.1, 0.1), 0.3, Kit.between(random, -0.1, 0.1)));
        Actor[] stick = new Actor[1];
        timeline.at(start, () -> stick[0] = stage.item(from, Material.STICK, 0));
        timeline.repeat(start + 1, start + SPARKLER_LIFE, 1, offset -> {
            if (!body.landed()) {
                body.step(stage);
                stick[0].teleport(body.position(), 0, 0);
            }
            if (offset % 2 == 0) {
                Kit.dust(stage, body.position().add(0, 0.3, 0), 1, Kit.between(random, 0.5, 0.9), 0.1);
            }
        });
        timeline.at(start + SPARKLER_LIFE + 1, () -> stick[0].remove());
    }
}
