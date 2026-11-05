package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class BalloonsAnimation implements BedAnimation {

    static final int BALLOONS = 25;
    static final Vec ANCHOR = new Vec(0.75, 0.25, -0.5);
    static final double MIN_X = -2.66;
    static final double MAX_X = 2.47;
    static final double MIN_Z = -2.03;
    static final double MAX_Z = 2.59;
    static final double MIN_Y = 1.41;
    static final double MAX_Y = 5.88;
    static final double SWAY = 0.25;
    static final double SWAY_SPEED = 0.1;
    static final double LIFT_START = 0.1;
    static final double LIFT_GAIN = 0.05;
    static final double LIFT_MAX = 1;
    static final int END = 55;

    @Override
    public String id() {
        return "baloes";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Kit.BedDisplay display = Kit.bed(stage, bed);
        Actor lantern = HeadLantern.place(stage, bed, Vec.ZERO);
        Vec anchorAt = bed.middle().add(ANCHOR);
        Actor anchor = stage.mob("Chicken", anchorAt, 0);
        anchor.watch(0, Hex.INVISIBLE);
        List<Balloon> balloons = new ArrayList<>();
        for (int index = 0; index < BALLOONS; index++) {
            Vec base = new Vec(Kit.between(random, MIN_X, MAX_X), Kit.between(random, MIN_Y, MAX_Y), Kit.between(random, MIN_Z, MAX_Z));
            balloons.add(new Balloon(stage, anchor, anchorAt, base, random.nextDouble() * 2 * Math.PI, BalloonsVictory.skin(index)));
        }
        timeline.at(0, () -> stage.sound("fireworks.launch", bed.foot(), 1, 1));
        timeline.repeat(0, END, 1, tick -> {
            Vec rise = new Vec(0, rise(tick + 1), 0);
            display.move(bed, rise);
            HeadLantern.move(lantern, bed, rise);
            anchor.teleport(anchorAt.add(rise), 0, 0);
            for (Balloon balloon : balloons) {
                balloon.drift(anchorAt.add(rise), tick + 1);
            }
        });
        timeline.at(END, () -> {
            display.remove();
            lantern.remove();
            anchor.remove();
            balloons.forEach(Balloon::remove);
        });
    }

    static double lift(int step) {
        return Math.min(LIFT_MAX, LIFT_START + LIFT_GAIN * step);
    }

    static double rise(int steps) {
        double total = 0;
        for (int step = 1; step <= steps; step++) {
            total += lift(step);
        }
        return total;
    }

    static Vec sway(int tick, double phase) {
        double angle = SWAY_SPEED * tick + phase;
        return new Vec(SWAY * Math.cos(2 * angle), SWAY * Math.sin(angle), SWAY * Math.sin(2 * angle));
    }

    private static final class Balloon {

        private final Actor stand;
        private final Actor chicken;
        private final Vec base;
        private final double phase;

        Balloon(Stage stage, Actor anchor, Vec anchorAt, Vec base, double phase, String skin) {
            this.base = base;
            this.phase = phase;
            Vec knot = anchorAt.add(base).add(sway(0, phase));
            this.stand = stage.armorStand(knot.subtract(BalloonsVictory.CHICKEN_OFFSET), 0, BalloonsVictory.BALLOON_STAND);
            stand.equipSkull(Slot.HELMET, skin);
            this.chicken = stage.mob("Chicken", knot, 0);
            chicken.watch(0, Hex.INVISIBLE);
            chicken.leash(anchor);
        }

        void drift(Vec anchorAt, int tick) {
            Vec knot = anchorAt.add(base).add(sway(tick, phase));
            chicken.teleport(knot, 0, 0);
            stand.teleport(knot.subtract(BalloonsVictory.CHICKEN_OFFSET), 0, 0);
        }

        void remove() {
            stand.remove();
            chicken.remove();
        }
    }
}
