package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class DuskAnimation implements BedAnimation {

    static final String OWL = "d6cd9fc319e14176dba72cf9e83030e538f5499235e0cf2531959eaa2e31dbcb";
    static final StandStyle OWL_STAND = new StandStyle(true, false, false, false, false);
    static final double[][] APPROACH = {
        {1.188, 4.562, -10.0}, {0.969, 2.906, -6.969}, {0.812, 1.719, -4.844}, {0.719, 0.906, -3.344}, {0.656, 0.312, -2.281},
        {0.594, -0.094, -1.5}, {0.562, -0.375, -0.969}, {0.531, -0.594, -0.594}, {0.5, -0.719, -0.344}, {0.5, -0.844, -0.156},
        {0.5, -0.906, 0.0}
    };
    static final double[][] CARRY = {
        {-0.062, 0.25, 0.594}, {-0.094, 0.562, 1.188}, {-0.156, 0.844, 1.812}, {-0.188, 1.156, 2.469}, {-0.25, 1.469, 3.188},
        {-0.281, 1.844, 3.938}, {-0.344, 2.219, 4.75}, {-0.406, 2.656, 5.719}, {-0.5, 3.188, 6.812}, {-0.594, 3.812, 8.156},
        {-0.719, 4.562, 9.781}, {-0.844, 5.562, 11.906}, {-1.062, 6.844, 14.625}, {-1.312, 8.531, 18.281}, {-1.656, 10.844, 23.188},
        {-2.125, 14.0, 29.906}
    };
    static final Vec GRIP = new Vec(0.5, -0.906, 0);
    static final double OWL_HEAD = 1.75;
    static final int CARRY_FROM = APPROACH.length - 1;
    static final int LEAVE = CARRY_FROM + CARRY.length - 1;
    static final int[] WINGS = {0, 2, 5, 8, 11, 15, 16, 20};
    static final int GRAB = 8;
    static final int SCREECH = 19;
    static final double[] MOON = {0.933, 0.867, 0.51};
    static final double MOON_HEIGHT = 3;
    static final double MOON_RADIUS = 1.3;
    static final double SHADOW_SHIFT = 0.6;
    static final double SHADOW_RADIUS = 1.1;
    static final int MOON_DOTS = 21;
    static final int MOON_SPARKS = 3;
    static final int MOON_FADE = 30;
    static final int SET = 37;
    static final double[][] FEATHERS = {{1, 1, 1}, {0.91, 0.863, 0.784}, {0.545, 0.353, 0.169}, {0.361, 0.227, 0.118}};
    static final int FEATHER_FROM = 2;
    static final int FEATHERS_PER_TICK = 4;
    static final double FEATHER_SPREAD = 0.35;

    @Override
    public String id() {
        return "anoitecer";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Nightfall.schedule(stage, timeline);
        Kit.BedDisplay display = Kit.bed(stage, bed);
        Actor lantern = HeadLantern.place(stage, bed, Vec.ZERO);
        Vec[] owlAt = {at(bed, APPROACH[0])};
        float heading = owlAt[0].yawTowards(at(bed, APPROACH[CARRY_FROM]));
        Actor owl = stage.armorStand(owlAt[0], heading, OWL_STAND);
        owl.equipSkull(Slot.HELMET, OWL);
        Vec moon = moonCenter(bed);
        timeline.at(0, () -> {
            stage.sound("ambient.weather.thunder", breaker, 1, 1);
            stage.sound("fireworks.twinkle", bed.middle(), 1, SoundPitch.of(75));
            stage.sound("mob.bat.idle", bed.middle(), 1, SoundPitch.of(37));
        });
        timeline.repeat(0, LEAVE, 1, tick -> {
            owlAt[0] = owl(bed, tick);
            owl.teleport(owlAt[0], heading, 0);
            if (tick >= CARRY_FROM) {
                Vec offset = carried(bed, tick);
                display.move(bed, offset);
                HeadLantern.move(lantern, bed, offset);
            }
            if (tick >= FEATHER_FROM) {
                feathers(stage, owlAt[0].add(0, OWL_HEAD, 0), random);
            }
        });
        for (int wing : WINGS) {
            timeline.at(wing, () -> stage.sound("mob.enderdragon.wings", owlAt[0].add(0, OWL_HEAD, 0), 0.4f, SoundPitch.of(113)));
        }
        timeline.at(GRAB, () -> {
            Vec burst = moon.add(bed.right().multiply(-SHADOW_SHIFT)).add(0, -0.2, 0);
            stage.particle("EXPLOSION_NORMAL", burst, 10, new Vec(0.3, 0.3, 0.3), 0.05);
            stage.sound("mob.bat.takeoff", burst, 1, SoundPitch.of(50));
            stage.sound("mob.bat.idle", burst, 1, SoundPitch.of(37));
        });
        timeline.at(SCREECH, () -> {
            stage.sound("mob.bat.idle", owlAt[0].add(0, OWL_HEAD, 0), 3, SoundPitch.of(31));
            stage.sound("mob.enderdragon.wings", owlAt[0].add(0, OWL_HEAD, 0), 3, SoundPitch.of(75));
        });
        timeline.at(LEAVE, () -> {
            Vec head = owlAt[0].add(0, OWL_HEAD, 0);
            owl.remove();
            display.remove();
            lantern.remove();
            stage.particle("CLOUD", head, 25, new Vec(0.6, 0.6, 0.6), 0.05);
            stage.sound("mob.chicken.plop", head, 3, SoundPitch.of(50));
            stage.sound("random.fizz", head, 1.5f, SoundPitch.of(94));
        });
        timeline.repeat(0, SET - 1, 1, tick -> {
            for (int dot = 0; dot < MOON_DOTS; dot++) {
                Vec point = crescent(bed, moon, random);
                Kit.dust(stage, point, MOON[0], MOON[1], MOON[2]);
                if (dot < MOON_SPARKS) {
                    stage.particle("FIREWORKS_SPARK", point, 0, Vec.ZERO, 0);
                }
            }
        });
        timeline.at(SET, () -> {
            for (int dot = 0; dot < MOON_FADE; dot++) {
                Vec scatter = new Vec(Kit.between(random, -1, 1), Kit.between(random, -1, 1), Kit.between(random, -1, 1)).multiply(MOON_RADIUS);
                Kit.dust(stage, moon.add(scatter), MOON[0], MOON[1], MOON[2]);
            }
            stage.particle("FIREWORKS_SPARK", moon, 30, new Vec(0.4, 0.4, 0.4), 0.15);
            stage.sound("fireworks.twinkle", moon, 1, SoundPitch.of(94));
            stage.sound("random.orb", moon, 0.6f, SoundPitch.of(126));
        });
    }

    static Vec owl(BedFrame bed, int tick) {
        if (tick < CARRY_FROM) {
            return at(bed, APPROACH[tick + 1]);
        }
        return carried(bed, tick).add(bed.at(GRIP.x(), GRIP.y(), GRIP.z()));
    }

    static Vec carried(BedFrame bed, int tick) {
        double[] step = CARRY[Math.min(tick - CARRY_FROM, CARRY.length - 1)];
        return at(bed, step).subtract(bed.foot());
    }

    static Vec moonCenter(BedFrame bed) {
        return bed.middle().add(0, MOON_HEIGHT, 0);
    }

    static boolean lit(double right, double up) {
        boolean inside = right * right + up * up <= MOON_RADIUS * MOON_RADIUS;
        double shifted = right - SHADOW_SHIFT;
        boolean shadowed = shifted * shifted + up * up < SHADOW_RADIUS * SHADOW_RADIUS;
        return inside && !shadowed;
    }

    static Vec crescent(BedFrame bed, Vec moon, RandomGenerator random) {
        double right;
        double up;
        do {
            right = Kit.between(random, -MOON_RADIUS, MOON_RADIUS);
            up = Kit.between(random, -MOON_RADIUS, MOON_RADIUS);
        } while (!lit(right, up));
        return moon.add(bed.right().multiply(right)).add(0, up, 0);
    }

    private static Vec at(BedFrame bed, double[] offset) {
        return bed.at(offset[0], offset[1], offset[2]);
    }

    private static void feathers(Stage stage, Vec head, RandomGenerator random) {
        for (int feather = 0; feather < FEATHERS_PER_TICK; feather++) {
            double[] color = FEATHERS[random.nextInt(FEATHERS.length)];
            Vec spread = new Vec(Kit.between(random, -1, 1), Kit.between(random, -1, 1), Kit.between(random, -1, 1)).multiply(FEATHER_SPREAD);
            Kit.dust(stage, head.add(spread), color[0], color[1], color[2]);
        }
    }
}
