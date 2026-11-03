package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Burst;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class PaparazziVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int FIRST_BURST = 1;
    static final int BURST_EVERY = 42;
    static final int FACES_PER_BURST = 3;
    static final int FACE_EVERY = 6;
    static final int REFRESH_EVERY = 2;
    static final int REFRESHES = 20;
    static final int FACE_LIFE = (REFRESHES - 1) * REFRESH_EVERY;
    static final double EYE_HEIGHT = 1.62;
    static final double MIN_DISTANCE = 3.1;
    static final double MAX_DISTANCE = 5.0;
    static final double MAX_BEARING = 45;
    static final double MIN_HEIGHT = 1.27;
    static final double MAX_HEIGHT = 2.22;
    static final double FACE_RADIUS = 0.7;
    static final double BLUE_FROM = 0.24;
    static final int FILL_POINTS = 22;
    static final int SPARKS_PER_REFRESH = 4;
    static final double SPARK_RADIUS = 0.9;
    static final int SPAWN_SPARKS = 12;
    static final double SPAWN_SPARK_SPEED = 0.15;
    static final double MIN_POP_PITCH = 1.5;
    static final double MAX_POP_PITCH = 2.0;
    static final int FLASH_EVERY = 3;
    static final double FLASH_MIN_DISTANCE = 4;
    static final double FLASH_MAX_DISTANCE = 9;
    static final double FLASH_MAX_BEARING = 75;
    static final double FLASH_MIN_HEIGHT = 0.5;
    static final double FLASH_MAX_HEIGHT = 3.5;
    static final double MIN_CLICK_PITCH = 1.6;
    static final double MAX_CLICK_PITCH = 2.0;
    static final double[] YELLOW = {1.0, 0.8, 0.2};
    static final double[] BLUE = {127 / 255.0, 184 / 255.0, 232 / 255.0};
    static final double[] WHITE = {1, 1, 1};
    static final double[] BROWN = {59 / 255.0, 36 / 255.0, 16 / 255.0};
    static final double[] ORANGE = {244 / 255.0, 169 / 255.0, 0};
    static final List<FacePoint> FACE = face();

    @Override
    public String id() {
        return "contagem-de-flash";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        for (int tick = FIRST_BURST; tick <= LENGTH; tick += BURST_EVERY) {
            for (int member = 0; member < FACES_PER_BURST; member++) {
                int start = tick + member * FACE_EVERY;
                if (start <= LENGTH) {
                    face(scene, stage, timeline, random, start);
                }
            }
        }
        for (int tick = FIRST_BURST; tick + 2 <= LENGTH; tick += FLASH_EVERY) {
            flash(scene, stage, timeline, random, tick);
        }
        timeline.lasts(LENGTH);
    }

    static boolean startsFace(int tick) {
        int sinceFirst = tick - FIRST_BURST;
        if (sinceFirst < 0) {
            return false;
        }
        int inBurst = sinceFirst % BURST_EVERY;
        return inBurst % FACE_EVERY == 0 && inBurst / FACE_EVERY < FACES_PER_BURST;
    }

    static List<FacePoint> face() {
        List<FacePoint> points = new ArrayList<>();
        for (int index = 0; index < 26; index++) {
            double[] color = index >= 2 && index <= 11 ? BLUE : YELLOW;
            points.add(ellipse(0, 0, FACE_RADIUS, FACE_RADIUS, index * 360.0 / 26, color));
        }
        for (int side = -1; side <= 1; side += 2) {
            for (int index = 0; index < 8; index++) {
                points.add(ellipse(side * 0.245, 0.14, 0.098, 0.126, index * 45, WHITE));
            }
            points.add(new FacePoint(side * 0.245, 0.119, BROWN));
        }
        for (int index = 0; index < 12; index++) {
            points.add(ellipse(0, -0.28, 0.14, 0.21, index * 30, BROWN));
        }
        points.add(new FacePoint(0, -0.28, BROWN));
        for (int side = -1; side <= 1; side += 2) {
            for (int index = 0; index < 10; index++) {
                points.add(ellipse(side * 0.735, -0.245, 0.154, 0.245, index * 36, ORANGE));
            }
        }
        return List.copyOf(points);
    }

    static FacePoint fill(RandomGenerator random) {
        double[] spot = inDisc(random, FACE_RADIUS);
        return new FacePoint(spot[0], spot[1], spot[1] >= BLUE_FROM ? BLUE : YELLOW);
    }

    static double[] inDisc(RandomGenerator random, double radius) {
        double distance = radius * Math.sqrt(random.nextDouble());
        double angle = random.nextDouble() * 2 * Math.PI;
        return new double[] {Math.cos(angle) * distance, Math.sin(angle) * distance};
    }

    static Placement place(Vec feet, Vec forward, RandomGenerator random) {
        Vec direction = rotate(forward, Kit.between(random, -MAX_BEARING, MAX_BEARING));
        Vec anchor = feet.add(direction.multiply(Kit.between(random, MIN_DISTANCE, MAX_DISTANCE))).add(0, Kit.between(random, MIN_HEIGHT, MAX_HEIGHT), 0);
        return new Placement(anchor, rightToward(anchor, feet));
    }

    static Vec rightToward(Vec anchor, Vec viewer) {
        Vec toward = new Vec(viewer.x() - anchor.x(), 0, viewer.z() - anchor.z()).normalize();
        return toward.length() == 0 ? new Vec(1, 0, 0) : new Vec(toward.z(), 0, -toward.x());
    }

    static Vec world(Placement placement, double u, double v) {
        return placement.anchor().add(placement.right().multiply(u)).add(0, v, 0);
    }

    static Vec flashSpot(Vec feet, Vec forward, RandomGenerator random) {
        Vec direction = rotate(forward, Kit.between(random, -FLASH_MAX_BEARING, FLASH_MAX_BEARING));
        return feet.add(direction.multiply(Kit.between(random, FLASH_MIN_DISTANCE, FLASH_MAX_DISTANCE))).add(0, Kit.between(random, FLASH_MIN_HEIGHT, FLASH_MAX_HEIGHT), 0);
    }

    static Vec rotate(Vec forward, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec(forward.x() * cos - forward.z() * sin, 0, forward.x() * sin + forward.z() * cos);
    }

    static Vec flat(Vec direction) {
        Vec flat = new Vec(direction.x(), 0, direction.z()).normalize();
        return flat.length() == 0 ? new Vec(0, 0, 1) : flat;
    }

    private static FacePoint ellipse(double centerU, double centerV, double radiusU, double radiusV, double degrees, double[] color) {
        double radians = Math.toRadians(degrees);
        return new FacePoint(centerU + Math.cos(radians) * radiusU, centerV + Math.sin(radians) * radiusV, color);
    }

    private static void face(Scene scene, Stage stage, Timeline timeline, RandomGenerator random, int start) {
        Placement[] placement = new Placement[1];
        timeline.repeat(start, Math.min(start + FACE_LIFE, LENGTH), REFRESH_EVERY, offset -> {
            if (offset == 0) {
                placement[0] = place(feet(scene, stage), forward(scene, stage), random);
                Vec anchor = placement[0].anchor();
                stage.sound("mob.chicken.plop", anchor, 0.6f, (float) Kit.between(random, MIN_POP_PITCH, MAX_POP_PITCH));
                stage.particle("FIREWORKS_SPARK", anchor, SPAWN_SPARKS, Vec.ZERO, SPAWN_SPARK_SPEED);
            }
            draw(stage, placement[0], random);
        });
    }

    private static void draw(Stage stage, Placement placement, RandomGenerator random) {
        for (FacePoint point : FACE) {
            dust(stage, placement, point);
        }
        for (int index = 0; index < FILL_POINTS; index++) {
            dust(stage, placement, fill(random));
        }
        for (int index = 0; index < SPARKS_PER_REFRESH; index++) {
            double[] spot = inDisc(random, SPARK_RADIUS);
            stage.particle("FIREWORKS_SPARK", world(placement, spot[0], spot[1]), 1, Vec.ZERO, 0);
        }
    }

    private static void dust(Stage stage, Placement placement, FacePoint point) {
        Kit.dust(stage, world(placement, point.u(), point.v()), point.color()[0], point.color()[1], point.color()[2]);
    }

    private static void flash(Scene scene, Stage stage, Timeline timeline, RandomGenerator random, int tick) {
        Actor[] rocket = new Actor[1];
        timeline.at(tick, () -> {
            Vec spot = flashSpot(feet(scene, stage), forward(scene, stage), random);
            Burst burst = Kit.burst(Kit.BALL, true, false, Kit.WHITE);
            rocket[0] = stage.firework(spot, List.of(burst));
            rocket[0].velocity(new Vec(0, 0.05, 0));
            stage.sound("random.click", spot, 0.6f, (float) Kit.between(random, MIN_CLICK_PITCH, MAX_CLICK_PITCH));
            stage.sound("fireworks.launch", spot, 3, 1);
        });
        timeline.at(tick + 1, () -> rocket[0].status(17));
        timeline.at(tick + 2, () -> rocket[0].remove());
    }

    private static Vec feet(Scene scene, Stage stage) {
        return winner(scene).flatMap(stage::eyes).map(eyes -> eyes.add(0, -EYE_HEIGHT, 0)).orElse(scene.origin());
    }

    private static Vec forward(Scene scene, Stage stage) {
        return winner(scene).flatMap(stage::look).map(PaparazziVictory::flat).orElse(scene.facing());
    }

    private static Optional<UUID> winner(Scene scene) {
        return scene.riders().stream().findFirst();
    }

    record FacePoint(double u, double v, double[] color) {
    }

    record Placement(Vec anchor, Vec right) {
    }
}
