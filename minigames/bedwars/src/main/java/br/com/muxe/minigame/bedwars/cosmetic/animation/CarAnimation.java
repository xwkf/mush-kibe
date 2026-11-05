package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class CarAnimation implements BedAnimation {

    static final String WHEEL = "c3e5e07d0ffc31665be682b11e2c7d9bd9e3ba55006081b250449e02262fb91c";
    static final StandStyle WHEEL_STAND = new StandStyle(true, true, false, false, false);
    static final double[][] WHEELS = {{-0.35, -0.3}, {-0.35, 0.7}, {1.35, -0.3}, {1.35, 0.7}};
    static final double WHEEL_DROP = -0.53;
    static final double SMOKE_ABOVE_WHEEL = 0.75;
    static final double[] SMOKE_START = {-0.78, -0.28, 0.47, 1.22, 1.97};
    static final int SMOKE_LAG = 3;
    static final double SINK = 1;
    static final double RISE_STEP = 0.5;
    static final int RISE_TICKS = 2;
    static final int SPIN_TICKS = 20;
    static final double SPIN_DEGREES = 1440;
    static final double SPIN_EASE = 0.072;
    static final double WHEEL_ROLL = 90;
    static final int DRIVE = 8;
    static final double[] DRIVE_STEPS = {0.09375, 0.28125, 0.5625, 1.0, 1.65625, 2.53125, 3.6875, 5.1875, 5.15625, 3.6875, 2.53125, 1.65625};
    static final int END = DRIVE + DRIVE_STEPS.length - 1;

    @Override
    public String id() {
        return "carro";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Kit.BedDisplay display = Kit.bed(stage, bed);
        display.move(bed, new Vec(0, -SINK, 0));
        Actor lantern = HeadLantern.place(stage, bed, new Vec(0, -SINK, 0));
        List<Actor> wheels = new ArrayList<>();
        for (double[] wheel : WHEELS) {
            Actor stand = stage.armorStand(wheel(bed, wheel, 0, -SINK), bed.yaw(), WHEEL_STAND);
            stand.equipSkull(Slot.HELMET, WHEEL);
            stand.pose(Pose.STANDING.withHead(new Vec(0, 0, WHEEL_ROLL)));
            wheels.add(stand);
        }
        timeline.at(0, () -> stage.sound("mob.enderdragon.wings", bed.foot(), 1, SoundPitch.of(94)));
        timeline.repeat(0, END, 1, tick -> {
            Vec offset = offset(bed, tick);
            display.move(bed, offset);
            HeadLantern.move(lantern, bed, offset);
            for (int index = 0; index < wheels.size(); index++) {
                Actor wheel = wheels.get(index);
                wheel.teleport(wheel(bed, WHEELS[index], travel(tick), lift(tick) - SINK), bed.yaw(), 0);
                if (tick < SPIN_TICKS) {
                    wheel.pose(Pose.STANDING.withHead(new Vec(0, spin(tick + 1), WHEEL_ROLL)));
                }
            }
        });
        timeline.repeat(0, END - 1, 1, tick -> {
            for (double[] wheel : WHEELS) {
                stage.particle("SMOKE_LARGE", smoke(bed, wheel, tick), 1, Vec.ZERO, 0);
            }
        });
        timeline.at(END, () -> {
            display.remove();
            lantern.remove();
            wheels.forEach(Actor::remove);
        });
    }

    static double spin(int step) {
        if (step <= SPIN_TICKS / 2) {
            return SPIN_EASE * Math.pow(step, 4);
        }
        return SPIN_DEGREES - SPIN_EASE * Math.pow(SPIN_TICKS - step, 4);
    }

    static double lift(int tick) {
        return Math.min(tick + 1, RISE_TICKS) * RISE_STEP;
    }

    static double travel(int tick) {
        double distance = 0;
        for (int step = 0; step <= tick - DRIVE && step < DRIVE_STEPS.length; step++) {
            distance += DRIVE_STEPS[step];
        }
        return distance;
    }

    static Vec smoke(BedFrame bed, double[] wheel, int tick) {
        double up = tick < SMOKE_START.length ? SMOKE_START[tick] : WHEEL_DROP + SMOKE_ABOVE_WHEEL;
        return bed.at(wheel[0] - travel(tick - SMOKE_LAG), up, wheel[1]);
    }

    private static Vec offset(BedFrame bed, int tick) {
        return bed.forward().multiply(-travel(tick)).add(0, lift(tick) - SINK, 0);
    }

    private static Vec wheel(BedFrame bed, double[] wheel, double travel, double up) {
        return bed.at(wheel[0] - travel, WHEEL_DROP + up, wheel[1]);
    }
}
