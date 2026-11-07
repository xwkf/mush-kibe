package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Burst;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class FrightfulFinalKill implements SceneAnimation {

    static final String GHOST = "c11de96c3e7b52fa5e4c794b4be14a05fc67694ca5eff9c9eb8baac672d21c09";
    static final List<double[]> PASTEL = List.of(
        new double[] {0.78, 0.659, 1},
        new double[] {0.914, 0.867, 1},
        new double[] {0.71, 0.945, 1},
        new double[] {1, 0.839, 0.961},
        new double[] {1, 1, 1}
    );
    static final Burst POP = new Burst(Kit.BALL, List.of(0xFFFFFF, 0xC7A8FF, 0xB5F1FF), List.of(0xD0D0E0), true, true);
    static final int LITTLE_GHOSTS = 3;
    static final int SPAWN = 1;
    static final int LAST_STEP = 54;
    static final int BURST = SPAWN + LAST_STEP + 1;
    static final double BIG_START = -2.0625;
    static final double LITTLE_START = -1.40625;
    static final double RISE = 2;
    static final int RISE_STEPS = 10;
    static final double RISE_EASE = 2.5;
    static final int SPIRAL_START = 10;
    static final int SPIRAL_STEPS = 30;
    static final double SPIRAL_RADIUS = 1.38;
    static final double SPIRAL_CLIMB = 3;
    static final double BIG_TURN = 18;
    static final double HOVER_JITTER = 0.03;
    static final int ORBIT_START = 6;
    static final double LITTLE_TURN = 360.0 / 21;
    static final double WIDEST = 1.97;
    static final int WIDEN_STEPS = 20;
    static final int NARROW_STEPS = 16;
    static final double CIRCLE = 0.9;
    static final int CLIMB_START = 12;
    static final int CLIMB_END = 41;
    static final double LITTLE_TOP = 3.8;
    static final double LITTLE_SETTLE = 3.45;
    static final double BIG_HEAD = 1.75;
    static final double LITTLE_HEAD = 0.85;
    static final double TRAIL_DROP = 0.5;
    static final double TRAIL_SPREAD = 0.15;
    static final int WOBBLE_START = 3;
    static final int WOBBLE_END = 42;
    static final int WOBBLE_REST = 45;
    static final double WOBBLE = 12;
    static final double WOBBLE_PERIOD = 15.7;
    static final double WOBBLE_PHASE = 0.4;
    static final int[] CHIMES = {14, 18, 24, 31, 36};
    static final float[] CHIME_PITCHES = {1.175f, 1, 0.794f, 0.667f, 0.587f};
    static final float[] ORB_PITCHES = {1.635f, 1.81f, 1.619f, 1.905f, 1.968f};
    static final int SCREAM = 42;
    static final int TWINKLE = BURST + 5;
    static final Material[] CANDY = {Material.GLOWSTONE_DUST, Material.GLOWSTONE_DUST, Material.GLOWSTONE_DUST, Material.GLOWSTONE_DUST,
        Material.GLOWSTONE_DUST, Material.GLOWSTONE_DUST, Material.SUGAR, Material.SUGAR};
    static final double CANDY_SPEED = 0.15;
    static final double CANDY_MIN_LIFT = 0.12;
    static final double CANDY_MAX_LIFT = 0.28;
    static final int CLOUD_POINTS = 45;
    static final int CLOUD_TICKS = 30;
    static final double CLOUD_WIDTH = 1.35;
    static final double CLOUD_HEIGHT = 1.25;
    static final double CLOUD_SINK = 0.06;
    static final int END = 95;

    @Override
    public String id() {
        return "apavorante";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float towardHitter = origin.yawTowards(origin.subtract(scene.facing()));
        double spin = random.nextDouble() * 360;
        Vec[] big = new Vec[LAST_STEP + 1];
        Vec[][] little = new Vec[LITTLE_GHOSTS][LAST_STEP + 1];
        for (int step = 0; step <= LAST_STEP; step++) {
            big[step] = bigGhost(origin, spin, step).add(0, step > SPIRAL_START + SPIRAL_STEPS ? Kit.between(random, -HOVER_JITTER, HOVER_JITTER) : 0, 0);
            for (int ghost = 0; ghost < LITTLE_GHOSTS; ghost++) {
                little[ghost][step] = littleGhost(origin, spin, ghost, step);
            }
        }
        haunt(stage, origin, random);
        Actor[] ghosts = new Actor[LITTLE_GHOSTS + 1];
        timeline.at(SPAWN, () -> {
            ghosts[0] = stage.armorStand(big[0], towardHitter, new StandStyle(true, false, false, false, true));
            for (int ghost = 0; ghost < LITTLE_GHOSTS; ghost++) {
                ghosts[ghost + 1] = stage.armorStand(little[ghost][0], towardHitter, StandStyle.HIDDEN_SMALL);
            }
            for (Actor ghost : ghosts) {
                ghost.equipSkull(Slot.HELMET, GHOST);
            }
        });
        timeline.repeat(SPAWN + 1, BURST - 1, 1, offset -> {
            int step = offset + 1;
            ghosts[0].teleport(big[step], bigYaw(spin, step, towardHitter), 0);
            for (int ghost = 0; ghost < LITTLE_GHOSTS; ghost++) {
                ghosts[ghost + 1].teleport(little[ghost][step], littleYaw(spin, ghost, step, towardHitter), 0);
            }
        });
        timeline.repeat(WOBBLE_START, WOBBLE_END, 1, offset -> ghosts[0].pose(Pose.STANDING.withHead(new Vec(0, 0, wobble(offset)))));
        timeline.at(WOBBLE_REST, () -> ghosts[0].pose(Pose.STANDING));
        timeline.repeat(SPAWN, BURST - 1, 1, offset -> {
            int step = offset;
            Vec bigTrail = trail(big[step].add(0, BIG_HEAD, 0), random);
            pastel(stage, bigTrail, random);
            stage.particle("FIREWORKS_SPARK", bigTrail, 1, Vec.ZERO, 0);
            for (int ghost = 0; ghost < LITTLE_GHOSTS; ghost++) {
                pastel(stage, trail(little[ghost][step].add(0, LITTLE_HEAD, 0), random), random);
            }
        });
        timeline.at(SPAWN + ORBIT_START, () -> stage.sound("mob.chicken.plop", origin, 0.8f, 1.587f));
        timeline.at(CHIMES[0], () -> stage.sound("fireworks.launch", origin, 1, 0.587f));
        for (int chime = 0; chime < CHIMES.length; chime++) {
            Vec head = big[CHIMES[chime] - SPAWN].add(0, BIG_HEAD, 0);
            float pling = CHIME_PITCHES[chime];
            float orb = ORB_PITCHES[chime];
            timeline.at(CHIMES[chime], () -> {
                stage.sound("note.pling", head, 0.8f, pling);
                stage.sound("random.orb", head, 0.3f, orb);
            });
        }
        Vec screamAt = big[SCREAM - SPAWN].add(0, BIG_HEAD, 0);
        timeline.at(SCREAM, () -> {
            stage.sound("mob.ghast.scream", screamAt, 0.6f, 1.286f);
            stage.sound("mob.ghast.moan", screamAt, 0.8f, 0.794f);
            stage.particle("SPELL_WITCH", screamAt, 25, new Vec(0.4, 0.4, 0.4), 0.3);
        });
        Vec burst = big[LAST_STEP].add(0, BIG_HEAD, 0);
        Kit.flash(stage, timeline, BURST, burst, POP);
        List<Actor> candy = new ArrayList<>();
        timeline.at(BURST, () -> {
            for (Actor ghost : ghosts) {
                ghost.remove();
            }
            pop(stage, burst, random);
            double first = random.nextDouble() * 360;
            for (int index = 0; index < CANDY.length; index++) {
                Actor item = stage.item(burst, CANDY[index], 0);
                item.velocity(Kit.horizontal(first + index * 45, CANDY_SPEED).add(0, Kit.between(random, CANDY_MIN_LIFT, CANDY_MAX_LIFT), 0));
                candy.add(item);
            }
        });
        List<Mist> cloud = new ArrayList<>();
        for (int point = 0; point < CLOUD_POINTS; point++) {
            Vec at = burst.add(Kit.between(random, -CLOUD_WIDTH, CLOUD_WIDTH), Kit.between(random, -CLOUD_HEIGHT, CLOUD_HEIGHT), Kit.between(random, -CLOUD_WIDTH, CLOUD_WIDTH));
            cloud.add(new Mist(at, PASTEL.get(random.nextInt(PASTEL.size()))));
        }
        timeline.repeat(BURST, BURST + CLOUD_TICKS - 1, 1, offset -> {
            Vec sink = new Vec(0, -CLOUD_SINK * offset, 0);
            for (Mist mist : cloud) {
                Kit.dust(stage, mist.at().add(sink), mist.color()[0], mist.color()[1], mist.color()[2]);
            }
            stage.particle("FIREWORKS_SPARK", cloud.get(random.nextInt(cloud.size())).at().add(sink), 1, Vec.ZERO, 0);
        });
        timeline.at(TWINKLE, () -> stage.sound("fireworks.twinkle_far", burst, 1.5f, 1.286f));
        timeline.at(END, () -> candy.forEach(Actor::remove));
        timeline.lasts(END + 1);
    }

    static double rise(int step) {
        double progress = Math.min(step, RISE_STEPS) / (double) RISE_STEPS;
        return RISE * (1 - Math.pow(1 - progress, RISE_EASE));
    }

    static Vec bigGhost(Vec origin, double spin, int step) {
        Vec settled = origin.add(0, BIG_START + rise(step), 0);
        if (step <= SPIRAL_START) {
            return settled;
        }
        double progress = Math.min(1, (step - SPIRAL_START) / (double) SPIRAL_STEPS);
        double radius = SPIRAL_RADIUS * Math.sin(Math.PI * progress);
        double eased = progress * progress * (3 - 2 * progress);
        return settled.add(Kit.horizontal(bigAngle(spin, step), radius)).add(0, SPIRAL_CLIMB * eased, 0);
    }

    static double bigAngle(double spin, int step) {
        return spin + BIG_TURN * (step - SPIRAL_START);
    }

    static Vec littleGhost(Vec origin, double spin, int ghost, int step) {
        if (step <= ORBIT_START) {
            return origin.add(0, LITTLE_START + rise(step), 0);
        }
        return origin.add(Kit.horizontal(littleAngle(spin, ghost, step), orbit(step))).add(0, littleHeight(step), 0);
    }

    static double littleAngle(double spin, int ghost, int step) {
        return spin + ghost * 360.0 / LITTLE_GHOSTS + LITTLE_TURN * (step - ORBIT_START);
    }

    static double orbit(int step) {
        int widened = ORBIT_START + WIDEN_STEPS;
        if (step <= widened) {
            return WIDEST * Math.sin(Math.PI / 2 * (step - ORBIT_START) / WIDEN_STEPS);
        }
        double progress = Math.min(1, (step - widened) / (double) NARROW_STEPS);
        return CIRCLE + (WIDEST - CIRCLE) * (1 + Math.cos(Math.PI * progress)) / 2;
    }

    static double littleHeight(int step) {
        double start = LITTLE_START + rise(ORBIT_START);
        if (step <= CLIMB_START) {
            return start;
        }
        if (step <= CLIMB_END) {
            return start + (LITTLE_TOP - start) * (step - CLIMB_START) / (CLIMB_END - CLIMB_START);
        }
        return LITTLE_TOP + (LITTLE_SETTLE - LITTLE_TOP) * (step - CLIMB_END) / (LAST_STEP - CLIMB_END);
    }

    static double wobble(int offset) {
        return WOBBLE * Math.sin(WOBBLE_PHASE + 2 * Math.PI * offset / WOBBLE_PERIOD);
    }

    private static float bigYaw(double spin, int step, float towardHitter) {
        return step > SPIRAL_START && step < SPIRAL_START + SPIRAL_STEPS ? (float) bigAngle(spin, step) : towardHitter;
    }

    private static float littleYaw(double spin, int ghost, int step, float towardHitter) {
        return step > ORBIT_START ? (float) littleAngle(spin, ghost, step) : towardHitter;
    }

    private static Vec trail(Vec head, RandomGenerator random) {
        return head.add(Kit.between(random, -TRAIL_SPREAD, TRAIL_SPREAD), -TRAIL_DROP + Kit.between(random, -TRAIL_SPREAD, TRAIL_SPREAD),
            Kit.between(random, -TRAIL_SPREAD, TRAIL_SPREAD));
    }

    private static void pastel(Stage stage, Vec at, RandomGenerator random) {
        double[] color = PASTEL.get(random.nextInt(PASTEL.size()));
        Kit.dust(stage, at, color[0], color[1], color[2]);
    }

    private static void haunt(Stage stage, Vec origin, RandomGenerator random) {
        Vec spirit = origin.add(0, 1, 0);
        stage.sound("mob.ghast.moan", spirit, 0.6f, 1.587f);
        stage.sound("fireworks.twinkle", spirit, 1, 1.397f);
        stage.particle("SPELL_WITCH", spirit, 20, new Vec(0.3, 0.6, 0.3), 0.1);
        stage.particle("FIREWORKS_SPARK", spirit, 10, Vec.ZERO, 0.1);
        for (int puff = 0; puff < 25; puff++) {
            pastel(stage, origin.add(Kit.between(random, -0.5, 0.5), Kit.between(random, 0.25, 1.75), Kit.between(random, -0.5, 0.5)), random);
        }
    }

    private static void pop(Stage stage, Vec burst, RandomGenerator random) {
        stage.sound("fireworks.largeBlast", burst, 1.5f, 1.19f);
        stage.sound("fireworks.twinkle", burst, 1.5f, 1);
        for (int plop = 0; plop < 3; plop++) {
            Vec at = burst.add(Kit.between(random, -0.9, 0.9), Kit.between(random, -0.45, 0), Kit.between(random, -0.9, 0.9));
            stage.sound("mob.chicken.plop", at, 0.8f, (float) Kit.between(random, 1.49, 1.75));
            stage.particle("EXPLOSION_NORMAL", burst.add(Kit.horizontal(random.nextDouble() * 360, 0.9)).add(0, -0.2, 0), 6, Vec.ZERO, 0.05);
        }
        stage.particle("SPELL_WITCH", burst, 30, new Vec(0.5, 0.5, 0.5), 0.4);
    }

    private record Mist(Vec at, double[] color) {
    }
}
