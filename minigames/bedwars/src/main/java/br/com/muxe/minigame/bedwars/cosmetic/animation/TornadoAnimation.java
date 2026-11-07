package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class TornadoAnimation implements BedAnimation {

    static final int[] GUSTS = {0, 7, 15, 22, 31, 40, 46, 53};
    static final float[] GUST_PITCH = {0.57f, 0.52f, 0.48f, 0.51f, 0.48f, 0.41f, 0.56f, 0.40f};
    static final int LAYERS = 26;
    static final double LAYER_HEIGHT = 0.35;
    static final double BASE_RADIUS = 0.3;
    static final double RADIUS_GROWTH = 0.098;
    static final double SPIN = 20;
    static final double TWIST = 8;
    static final double FUNNEL_START = -180;
    static final double[] GRAY = {0.48, 0.48, 0.48};
    static final double SHRINK = 1.4;
    static final double ORBIT = 0.5;
    static final double ORBIT_START = 180;
    static final double FIRST_TURN = 3.5;
    static final double TURN_GROWTH = 0.17;
    static final double LAUNCH_TURN = 34;
    static final int LIFT_FROM = 8;
    static final int HOVER = 40;
    static final double HOVER_HEIGHT = 2.47;
    static final int LAUNCH = 43;
    static final double FIRST_JUMP = 0.69;
    static final double JUMP_GROWTH = 0.079;
    static final int BURST = 60;
    static final double BED_CENTER = 0.5;

    @Override
    public String id() {
        return "tornado";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Kit.BedDisplay display = Kit.bed(stage, bed);
        timeline.at(0, () -> {
            stage.sound("mob.enderdragon.wings", middle, 1, 0.49f);
            stage.sound("random.fizz", middle, 0.6f, 0.49f);
        });
        for (int gust = 0; gust < GUSTS.length; gust++) {
            float pitch = GUST_PITCH[gust];
            timeline.at(GUSTS[gust], () -> {
                stage.sound("mob.enderdragon.wings", middle, 0.7f, pitch);
                stage.sound("ambient.weather.rain", middle, 0.4f, 0.49f);
            });
        }
        timeline.repeat(0, BURST, 1, tick -> {
            funnel(bed, stage, tick);
            stage.particle("BLOCK_DUST", middle.add(0, 0.2, 0), 4, new Vec(0.8, 0.1, 0.8), 0.15, Kit.blockId(Material.DIRT, 0));
        });
        timeline.repeat(1, BURST - 1, 1, offset -> {
            int tick = offset + 1;
            double angle = orbit(tick);
            double lift = lift(tick);
            display.footStand().teleport(around(bed, angle, ORBIT).add(0, lift, 0), 0, 0);
            display.headStand().teleport(around(bed, angle + 180, ORBIT).add(0, lift, 0), 0, 0);
            if (tick >= LAUNCH) {
                stage.particle("CLOUD", middle.add(0, lift + BED_CENTER, 0), 2, new Vec(0.3, 0.3, 0.3), 0.02);
            }
        });
        timeline.at(LAUNCH, () -> {
            stage.sound("fireworks.launch", middle, 1.5f, 0.59f);
            stage.sound("mob.enderdragon.wings", middle, 1.5f, 1.19f);
            stage.particle("CLOUD", middle.add(0, 2.5, 0), 30, new Vec(0.5, 0.2, 0.5), 0.2);
        });
        Vec top = middle.add(0, lift(BURST) + BED_CENTER, 0);
        timeline.at(BURST, () -> {
            display.remove();
            stage.particle("EXPLOSION_HUGE", top, 1, Vec.ZERO, 1);
            stage.particle("BLOCK_CRACK", top, 60, new Vec(0.6, 0.6, 0.6), 0.3, Kit.blockId(Material.WOOL, 14));
            stage.sound("random.explode", top, 4, 0.89f);
            stage.sound("fireworks.largeBlast", top, 4, 1);
        });
        Kit.flash(stage, timeline, BURST, top, Kit.fading(Kit.LARGE_BALL, false, false, 0x7FCC19, Kit.WHITE));
    }

    static int layers(int tick) {
        int grown = Math.min(LAYERS, tick + 1);
        if (tick <= LAUNCH) {
            return grown;
        }
        return Math.max(0, grown - (int) Math.round(SHRINK * (tick - LAUNCH)));
    }

    static double radius(int layer) {
        return BASE_RADIUS + RADIUS_GROWTH * layer;
    }

    static double orbit(int tick) {
        if (tick <= HOVER) {
            return ORBIT_START - FIRST_TURN * tick - TURN_GROWTH * tick * tick;
        }
        return orbit(HOVER) - LAUNCH_TURN * (tick - HOVER);
    }

    static double lift(int tick) {
        if (tick <= LIFT_FROM) {
            return 0;
        }
        if (tick <= HOVER) {
            double rest = 1 - (tick - LIFT_FROM) / (double) (HOVER - LIFT_FROM);
            return HOVER_HEIGHT * (1 - rest * rest);
        }
        int jumps = tick - HOVER;
        return HOVER_HEIGHT + FIRST_JUMP * jumps + JUMP_GROWTH * jumps * (jumps - 1) / 2;
    }

    static Vec around(BedFrame bed, double degrees, double radius) {
        double radians = Math.toRadians(degrees);
        return bed.middle().add(bed.forward().multiply(Math.cos(radians) * radius)).add(bed.right().multiply(Math.sin(radians) * radius));
    }

    private static void funnel(BedFrame bed, Stage stage, int tick) {
        int layers = layers(tick);
        double spin = FUNNEL_START + SPIN * tick;
        for (int layer = 0; layer < layers; layer++) {
            double angle = spin - TWIST * layer;
            double radius = radius(layer);
            double up = LAYER_HEIGHT * layer;
            Kit.dust(stage, around(bed, angle, radius).add(0, up, 0), GRAY[0], GRAY[1], GRAY[2]);
            stage.particle("CLOUD", around(bed, angle + 120, radius).add(0, up, 0), 1, Vec.ZERO, 0);
            stage.particle("CLOUD", around(bed, angle - 120, radius).add(0, up, 0), 1, Vec.ZERO, 0);
        }
    }
}
