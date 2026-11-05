package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;

public final class MagicalGirlAnimation implements BedAnimation {

    static final String STAR = "a64c1490c1254807b7e3fe7b3a5a3b3f15cc699ab3872cc02968e9712570ef99";
    static final double[] PINK = {1, 0.71, 0.77};
    static final double[][] RAINBOW = {{1, 0, 0}, {1, 0.5, 0}, {1, 1, 0}, {0, 1, 0}, {0, 0, 1}, {0.29, 0, 0.51}, {0.58, 0, 0.83}};
    static final int[] CHIMES = {0, 9, 20};
    static final int HEART_UNTIL = 31;
    static final int HEART_EVERY = 2;
    static final int HEART_STEPS = 60;
    static final int HEART_GROWTH = 4;
    static final double HEART_SCALE = 0.1;
    static final double HEART_CENTER = 2.48;
    static final double HEART_JITTER = 0.04;
    static final double SPARKLE_CHANCE = 0.08;
    static final int LAUNCH = 31;
    static final double ROCKET_LIFT = 2.5;
    static final int BURST = 46;
    static final int[] STARS = {48, 50, 51, 53, 56, 57, 58, 61};
    static final float[] STAR_PITCH = {2.0f, 1.89f, 1.79f, 1.70f, 1.59f, 1.49f, 1.40f, 1.29f};

    @Override
    public String id() {
        return "garota-magica";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        for (int chime : CHIMES) {
            timeline.at(chime, () -> stage.sound("random.orb", middle, 1, 1));
        }
        Vec across = across(bed, breaker);
        timeline.repeat(0, HEART_UNTIL, HEART_EVERY, tick -> heart(middle, across, stage, tick / HEART_EVERY, random));
        rocket(middle, stage, timeline);
        for (int index = 0; index < STARS.length; index++) {
            float pitch = STAR_PITCH[index];
            timeline.at(STARS[index], () -> stage.sound("note.pling", middle, 0.6f, pitch));
            new FallingStar(bed, stage, random).script(timeline, STARS[index]);
        }
    }

    static int heartPoints(int frame) {
        return Math.min(HEART_STEPS, HEART_GROWTH * (frame + 1));
    }

    static Vec across(BedFrame bed, Vec breaker) {
        Vec toBreaker = new Vec(breaker.x() - bed.middle().x(), 0, breaker.z() - bed.middle().z()).normalize();
        return toBreaker.length() == 0 ? bed.right() : new Vec(-toBreaker.z(), 0, toBreaker.x());
    }

    static Vec heartPoint(Vec middle, Vec across, double radians) {
        double sine = Math.sin(radians);
        double side = 16 * sine * sine * sine;
        double up = 13 * Math.cos(radians) - 5 * Math.cos(2 * radians) - 2 * Math.cos(3 * radians) - Math.cos(4 * radians);
        return middle.add(across.multiply(side * HEART_SCALE)).add(0, HEART_CENTER + up * HEART_SCALE, 0);
    }

    private static void heart(Vec middle, Vec across, Stage stage, int frame, RandomGenerator random) {
        int reach = heartPoints(frame);
        for (int step = -reach; step <= reach; step++) {
            if (step == HEART_STEPS) {
                continue;
            }
            Vec point = heartPoint(middle, across, Math.PI + step * Math.PI / HEART_STEPS).add(jitter(random), jitter(random), jitter(random));
            Kit.dust(stage, point, PINK[0], PINK[1], PINK[2]);
            if (random.nextDouble() < SPARKLE_CHANCE) {
                stage.particle("FIREWORKS_SPARK", point, 0, Vec.ZERO, 0);
            }
        }
    }

    private static double jitter(RandomGenerator random) {
        return Kit.between(random, -HEART_JITTER, HEART_JITTER);
    }

    private static void rocket(Vec middle, Stage stage, Timeline timeline) {
        Vec start = middle.add(0, ROCKET_LIFT, 0);
        Actor[] rocket = new Actor[1];
        timeline.at(LAUNCH, () -> {
            rocket[0] = stage.firework(start, List.of(Kit.burst(Kit.LARGE_BALL, true, true, 0xFFB4C5)));
            rocket[0].velocity(new Vec(0, 0.05, 0));
            stage.sound("fireworks.launch", middle, 1, 1);
            stage.sound("fireworks.launch", start, 3, 1);
        });
        timeline.at(BURST, () -> rocket[0].status(17));
        timeline.at(BURST + 2, () -> rocket[0].remove());
    }

    static final class FallingStar {

        static final double MIN_RADIUS = 4.5;
        static final double MAX_RADIUS = 6.8;
        static final double MIN_HEIGHT = 5.8;
        static final double MAX_HEIGHT = 7.5;
        static final double LANDING_SPREAD = 1.5;
        static final double REST = -1.22;
        static final int FLIGHT = 13;
        static final double FIRST_DROP = 0.0372;
        static final double DROP_GROWTH = 0.00306;
        static final double[] SETTLE = {-0.22, -0.09, 0, 0.03, 0};
        static final int FADE = 4;
        static final double SPIN = -18.28;
        static final double HEAD = 1.17;
        static final double TRAIL_START = 0.15;
        static final double TRAIL_LENGTH = 0.45;
        static final double TRAIL_RISE = 0.39;
        static final double FIZZ = 1.6;

        private final Stage stage;
        private final Vec start;
        private final Vec landing;
        private Actor star;

        FallingStar(BedFrame bed, Stage stage, RandomGenerator random) {
            this.stage = stage;
            Vec middle = bed.middle();
            this.start = middle.add(Kit.horizontal(random.nextDouble() * 360, Kit.between(random, MIN_RADIUS, MAX_RADIUS)))
                .add(0, Kit.between(random, MIN_HEIGHT, MAX_HEIGHT), 0);
            this.landing = middle.add(Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * LANDING_SPREAD)).add(0, REST, 0);
        }

        void script(Timeline timeline, int spawn) {
            timeline.at(spawn, () -> {
                star = stage.armorStand(start, 0, StandStyle.HIDDEN);
                star.equipSkull(Slot.HELMET, STAR);
            });
            int landed = spawn + FLIGHT;
            timeline.repeat(spawn, landed + FADE - 1, 1, step -> {
                Vec at = position(step);
                star.teleport(at, (float) (SPIN * step), 0);
                trail(at);
                if (step == FLIGHT) {
                    stage.sound("mob.chicken.plop", at.add(0, HEAD, 0), 0.6f, 2);
                }
            });
            timeline.at(landed + FADE, () -> {
                stage.sound("random.fizz", landing.add(0, FIZZ, 0), 0.3f, 2);
                star.remove();
            });
        }

        Vec position(int step) {
            if (step >= FLIGHT) {
                double settle = SETTLE[Math.min(step - FLIGHT, SETTLE.length - 1)];
                return landing.add(0, settle, 0);
            }
            double across = step / (double) FLIGHT;
            double down = FIRST_DROP * step + DROP_GROWTH * step * step;
            Vec flat = start.add(new Vec(landing.x() - start.x(), 0, landing.z() - start.z()).multiply(across));
            return new Vec(flat.x(), start.y() + (landing.y() + SETTLE[0] - start.y()) * down, flat.z());
        }

        Vec start() {
            return start;
        }

        Vec landing() {
            return landing;
        }

        private void trail(Vec at) {
            Vec heading = new Vec(landing.x() - start.x(), 0, landing.z() - start.z()).normalize();
            Vec tail = at.add(0, HEAD, 0).add(heading.multiply(TRAIL_START));
            Vec span = heading.multiply(TRAIL_LENGTH).add(0, TRAIL_RISE, 0);
            for (int band = 0; band < RAINBOW.length; band++) {
                double[] color = RAINBOW[band];
                Vec point = tail.add(span.multiply(1 - band / (double) (RAINBOW.length - 1)));
                Kit.dust(stage, point, color[0], color[1], color[2]);
            }
        }
    }
}
