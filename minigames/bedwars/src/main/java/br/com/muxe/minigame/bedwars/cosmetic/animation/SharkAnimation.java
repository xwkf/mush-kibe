package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
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

public final class SharkAnimation implements BedAnimation {

    static final String SHARK = "3f425470831592c950ba94aba010fb796fda24303bfda21d92cf614eb4cf7814";
    static final String PALE_SHARK = "8d35f988579a46efb3bdfb92dde1324e5660f75e781d0b998cfe597f392f8588";
    static final double[] SIDES = {1.2, 0.4, -0.4, -1.2};
    static final double[] TURNS = {30, 15, -15, -30};
    static final String[] SKINS = {SHARK, SHARK, SHARK, PALE_SHARK};
    static final double SUNK = -0.94;
    static final double FIN = -0.69;
    static final double LEAP = 0.25;
    static final double LEAP_RISE = 1.19;
    static final int SWIM = 28;
    static final double FIRST_STROKE = 0.04;
    static final double STROKE_GROWTH = 0.0115;
    static final double SWIM_PITCH = 10;
    static final double WIGGLE = 5;
    static final double WIGGLE_PERIOD = 6;
    static final double WATER_LINE = 1.0;
    static final int[] GONE = {58, 56, 56, 58};
    static final double SPLASH_CHANCE = 0.4;

    @Override
    public String id() {
        return "tubarao";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Vec away = away(bed, breaker);
        Vec side = new Vec(-away.z(), 0, away.x());
        float yaw = Vec.ZERO.yawTowards(away);
        Kit.flash(stage, timeline, 0, middle, Kit.burst(Kit.BALL, false, true, 0x363897));
        List<Shark> sharks = new ArrayList<>();
        for (int index = 0; index < SIDES.length; index++) {
            Vec start = middle.add(side.multiply(SIDES[index])).add(0, SUNK, 0);
            Actor body = stage.armorStand(start, yaw, StandStyle.HIDDEN_SMALL);
            body.equipSkull(Slot.HELMET, SKINS[index]);
            double turn = Math.toRadians(TURNS[index]);
            Vec heading = away.multiply(Math.cos(turn)).add(side.multiply(Math.sin(turn)));
            Shark shark = new Shark(index, body, heading, Body.living(start, away.multiply(LEAP).add(0, LEAP_RISE, 0)));
            sharks.add(shark);
        }
        timeline.repeat(0, SWIM - 1, 1, tick -> sharks.forEach(shark -> shark.leap(stage, yaw)));
        timeline.repeat(SWIM, GONE[0], 1, offset -> sharks.forEach(shark -> shark.swim(stage, yaw, offset)));
        for (Shark shark : sharks) {
            timeline.at(GONE[shark.index()], () -> {
                Vec at = shark.water();
                shark.body().remove();
                stage.particle("CLOUD", at, 15, new Vec(0.01, 0.01, 0.01), 0.01);
                stage.sound("mob.bat.hurt", middle, 1, (float) Kit.between(random, 1.0, 1.75));
            });
        }
        timeline.at(SWIM, () -> stage.sound("random.splash", middle, 1, 1.81f));
        timeline.repeat(SWIM, GONE[0], 1, offset -> {
            stage.sound("fire.ignite", middle, 1, 1.49f);
            stage.sound("fire.ignite", middle, 1, 1.49f);
            if (offset > 0 && offset < GONE[1] - SWIM && random.nextDouble() < SPLASH_CHANCE) {
                stage.sound("random.splash", middle, 1, (float) Kit.between(random, 1.17, 1.92));
            }
        });
    }

    static Vec away(BedFrame bed, Vec breaker) {
        Vec toward = new Vec(breaker.x() - bed.middle().x(), 0, breaker.z() - bed.middle().z()).normalize();
        return toward.length() == 0 ? bed.forward().multiply(-1) : toward;
    }

    static double stroke(int offset) {
        return FIRST_STROKE + STROKE_GROWTH * offset;
    }

    static float pitch(Vec velocity) {
        return (float) -Math.toDegrees(Math.atan2(velocity.y(), Math.hypot(velocity.x(), velocity.z())));
    }

    private static final class Shark {

        private final int index;
        private final Actor body;
        private final Vec heading;
        private final Body leap;
        private Vec position;
        private boolean swimming;

        Shark(int index, Actor body, Vec heading, Body leap) {
            this.index = index;
            this.body = body;
            this.heading = heading;
            this.leap = leap;
            this.position = leap.position();
        }

        int index() {
            return index;
        }

        Actor body() {
            return body;
        }

        Vec water() {
            return position.add(0, swimming ? WATER_LINE : 0, 0);
        }

        void leap(Stage stage, float yaw) {
            if (!swimming) {
                Vec velocity = leap.velocity();
                swimming = leap.step(stage);
                position = swimming ? leap.position().add(0, FIN, 0) : leap.position();
                body.teleport(position, yaw, 0);
                body.pose(Pose.STANDING.withHead(new Vec(swimming ? SWIM_PITCH : pitch(velocity), TURNS[index], 0)));
            }
            splash(stage);
        }

        void swim(Stage stage, float yaw, int offset) {
            if (body.removed()) {
                return;
            }
            if (!swimming) {
                position = Ground.surface(stage::solid, position).add(0, FIN, 0);
                swimming = true;
            }
            Vec moved = position.add(heading.multiply(stroke(offset)));
            double ground = Ground.surface(stage::solid, moved.x(), moved.y() - FIN, moved.z());
            position = new Vec(moved.x(), ground + FIN, moved.z());
            body.teleport(position, yaw, 0);
            double roll = WIGGLE * Math.sin(2 * Math.PI * (offset + index * WIGGLE_PERIOD / 4) / WIGGLE_PERIOD);
            body.pose(Pose.STANDING.withHead(new Vec(SWIM_PITCH, TURNS[index], roll)));
            splash(stage);
        }

        private void splash(Stage stage) {
            Vec at = water();
            stage.particle("WATER_SPLASH", at, 10, Vec.ZERO, 1);
            stage.particle("CRIT_MAGIC", at, 0, Vec.ZERO, 1);
        }
    }
}
