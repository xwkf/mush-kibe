package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.PaintAnimation.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class PaintFinalKill implements SceneAnimation {

    static final List<Paint> ORDER = List.of(PaintAnimation.PAINTS.get(1), PaintAnimation.PAINTS.get(2), PaintAnimation.PAINTS.get(0), PaintAnimation.PAINTS.get(3));
    static final int LAUNCH_EVERY = 2;
    static final double LAUNCH_SIDEWAYS = 0.27;
    static final int SPLASH_RINGS = 6;
    static final int STAR_FROM = 1;
    static final int FLIGHT_LIMIT = 50;
    static final int SPLAT_LEAD = 3;
    static final double SPARK_HEIGHT = 0.5;
    static final int FINALE = 33;
    static final int END = 57;

    @Override
    public String id() {
        return "tinta";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.sound("random.splash", origin, 1, 1.19f);
        stage.sound("mob.slime.small", origin, 1, 0.79f);
        for (Paint paint : ORDER) {
            stage.particle("BLOCK_DUST", origin.add(0, 0.1, 0), 15, new Vec(0.6, 0.05, 0.6), 0.1, Kit.blockId(Material.WOOL, paint.wool()));
        }
        timeline.repeat(0, SPLASH_RINGS - 1, 1, tick -> ring(stage, origin.add(0, 0.1, 0), (tick + 1) / 3.0, random));
        double base = random.nextDouble() * 360;
        List<Actor> sticks = new ArrayList<>();
        for (int index = 0; index < ORDER.size(); index++) {
            launch(stage, timeline, random, origin, index, base + index * 90, sticks);
        }
        int frames = PaintAnimation.STAR_FRAMES;
        timeline.repeat(STAR_FROM, STAR_FROM + (frames - 1) * 2, 2, offset -> drawStar(stage, origin, scene.right(), offset / 2));
        Vec center = PaintAnimation.starCenter(origin, frames - 1);
        int[] colors = new int[4];
        for (int index = 0; index < colors.length; index++) {
            colors[index] = Kit.rgb(PaintAnimation.starColor(frames - 1, index / 3.0));
        }
        timeline.at(FINALE, () -> stage.sound("fireworks.twinkle", center, 1.5f, 1));
        Kit.flash(stage, timeline, FINALE, center, Kit.burst(Kit.STAR, false, false, colors));
        timeline.at(END, () -> sticks.forEach(Actor::remove));
    }

    static List<Vec> flight(Stage stage, Vec origin, Vec velocity) {
        Body body = Body.living(origin, velocity);
        List<Vec> path = new ArrayList<>();
        while (path.size() < FLIGHT_LIMIT && !body.landed()) {
            body.step(stage);
            path.add(body.position());
        }
        return List.copyOf(path);
    }

    static Vec launchVelocity(int index, double degrees) {
        return Kit.horizontal(degrees, LAUNCH_SIDEWAYS).add(0, PaintAnimation.LAUNCH_SPEED[index], 0);
    }

    private static void launch(Stage stage, Timeline timeline, RandomGenerator random, Vec origin, int index, double degrees, List<Actor> sticks) {
        Paint paint = ORDER.get(index);
        int start = index * LAUNCH_EVERY;
        List<Vec> path = flight(stage, origin, launchVelocity(index, degrees));
        int landing = start + path.size();
        Actor[] blob = new Actor[1];
        float[] yaw = {0};
        timeline.at(start, () -> {
            blob[0] = stage.armorStand(origin.add(0, -PaintAnimation.BLOB_HEAD, 0), 0, new StandStyle(true, false, false, false, false));
            blob[0].equip(Slot.HELMET, Material.WOOL, paint.wool());
            stage.sound("random.pop", origin, 1, 0.79f + 0.2f * index);
            stage.sound("mob.chicken.plop", origin, 1, 0.89f + 0.15f * index);
        });
        Vec squirt = origin.add(Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * 0.3));
        timeline.repeat(start + 2, start + 4, 1, tick -> {
            Vec at = squirt.add(0, 2.5 + 1.66 * tick, 0);
            for (int dot = 0; dot < 10; dot++) {
                Kit.dust(stage, at.add(Kit.between(random, -0.1, 0.1), Kit.between(random, -0.1, 0.1), Kit.between(random, -0.1, 0.1)), paint.red(), paint.green(), paint.blue());
            }
        });
        timeline.at(start + 5, () -> line(stage, squirt.add(0, 5.8, 0), path.get(Math.min(3, path.size() - 1)), 0.05, 0, paint, random));
        timeline.repeat(start + 1, landing, 1, step -> {
            Vec before = step == 0 ? origin : path.get(step - 1);
            Vec after = path.get(step);
            if (step < PaintAnimation.SPIN.length) {
                yaw[0] -= (float) PaintAnimation.SPIN[step];
            }
            blob[0].teleport(after.add(0, -PaintAnimation.BLOB_HEAD, 0), yaw[0], 0);
            if (step >= 5) {
                line(stage, before, after, 0.05, 0.12, paint, random);
            }
        });
        int splat = Math.max(start + 1, landing - SPLAT_LEAD);
        Vec spark = origin.add(0, SPARK_HEIGHT, 0);
        Kit.flash(stage, timeline, splat, spark, Kit.burst(Kit.STAR, false, false, paint.color()));
        timeline.at(splat, () -> {
            stage.sound("mob.chicken.plop", spark, 1, (float) Kit.between(random, 1.2, 1.6));
            Actor stick = stage.item(spark, Material.STICK, 0);
            stick.velocity(new Vec(Kit.between(random, -0.1, 0.1), 0.3, Kit.between(random, -0.1, 0.1)));
            sticks.add(stick);
        });
        timeline.at(landing + 1, () -> blob[0].remove());
    }

    private static void ring(Stage stage, Vec center, double radius, RandomGenerator random) {
        int points = (int) Math.round(2 * Math.PI * radius / 0.2);
        for (int index = 0; index < points; index++) {
            Paint paint = ORDER.get(random.nextInt(ORDER.size()));
            Kit.dust(stage, center.add(Kit.horizontal(360.0 * index / points, radius)), paint.red(), paint.green(), paint.blue());
        }
        for (int index = 0; index < 10; index++) {
            Paint paint = ORDER.get(random.nextInt(ORDER.size()));
            Kit.dust(stage, center.add(Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * radius)), paint.red(), paint.green(), paint.blue());
        }
    }

    private static void drawStar(Stage stage, Vec origin, Vec right, int frame) {
        double spacing = PaintAnimation.STAR_SCALE[Math.min(frame, PaintAnimation.STAR_SCALE.length - 1)];
        Vec center = PaintAnimation.starCenter(origin, frame);
        List<String> star = PaintAnimation.STAR;
        int rows = star.size();
        for (int row = 0; row < rows; row++) {
            double[] color = PaintAnimation.starColor(frame, 1 - row / (rows - 1.0));
            String line = star.get(row);
            for (int column = 0; column < line.length(); column++) {
                if (line.charAt(column) == '#') {
                    Vec at = center.add(right.multiply((column - (line.length() - 1) / 2.0) * spacing)).add(0, ((rows - 1) / 2.0 - row) * spacing, 0);
                    Kit.dust(stage, at, color[0], color[1], color[2]);
                }
            }
        }
    }

    private static void line(Stage stage, Vec from, Vec to, double spacing, double jitter, Paint paint, RandomGenerator random) {
        double length = from.distance(to);
        int dots = Math.max(1, (int) Math.ceil(length / spacing));
        Vec step = to.subtract(from).multiply(1.0 / dots);
        for (int dot = 0; dot <= dots; dot++) {
            Vec at = from.add(step.multiply(dot));
            if (jitter > 0) {
                at = at.add(Kit.between(random, -jitter, jitter), Kit.between(random, -jitter, jitter), Kit.between(random, -jitter, jitter));
            }
            Kit.dust(stage, at, paint.red(), paint.green(), paint.blue());
        }
    }
}
