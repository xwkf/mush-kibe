package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;

public final class DuskFinalKill implements SceneAnimation {

    static final String OWL = "d6cd9fc319e14176dba72cf9e83030e538f5499235e0cf2531959eaa2e31dbcb";
    static final List<double[]> FEATHERS = List.of(
        new double[] {0.91, 0.863, 0.784},
        new double[] {1, 1, 1},
        new double[] {0.361, 0.227, 0.118},
        new double[] {0.545, 0.353, 0.169}
    );
    static final double GRIP = 0.40625;
    static final double HEAD = 1.6;
    static final double START_SIDE = -9.934;
    static final double START_UP = 4.156;
    static final double START_AHEAD = 0.877;
    static final double GLIDE = 0.69;
    static final int ARRIVAL = 10;
    static final int GRAB = ARRIVAL - 1;
    static final int DEPARTURE = 16;
    static final double CLIMB = 0.465;
    static final double DRIFT = -0.0883;
    static final double CRUISE = 0.58;
    static final double BOOST = 0.029;
    static final double ACCELERATION = 1.43;
    static final int END = GRAB + DEPARTURE;
    static final double CRY_HEIGHT = 4;
    static final int[] WINGS = {0, 5, 7, 8, 12, 14, 17, 21};
    static final int SCREECH = 19;
    static final int TRAIL_START = 5;
    static final int TRAIL_LAG = 2;
    static final int FEATHERS_PER_TICK = 3;
    static final double FEATHER_SPREAD = 0.3;

    @Override
    public String id() {
        return "anoitecer";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec grip = origin.add(0, GRIP, 0);
        Vec right = scene.right();
        Vec ahead = scene.facing();
        float towardHitter = origin.yawTowards(origin.subtract(ahead));
        float heading = Vec.ZERO.yawTowards(right.add(ahead.multiply(DRIFT)));
        Vec[] path = new Vec[END + 1];
        for (int tick = 0; tick <= END; tick++) {
            path[tick] = position(grip, right, ahead, tick);
        }
        stage.sound("mob.bat.idle", origin, 1, 0.587f);
        Actor body = scene.skin().map(skin -> stage.playerWearing(origin, towardHitter, skin)).orElseGet(() -> stage.player(origin, towardHitter));
        Actor owl = stage.armorStand(path[0], heading, new StandStyle(true, false, false, false, false));
        owl.equipSkull(Slot.HELMET, OWL);
        timeline.repeat(1, END, 1, offset -> {
            int tick = offset + 1;
            owl.teleport(path[tick], heading, 0);
            if (tick > GRAB) {
                body.teleport(path[tick].add(0, -GRIP, 0), towardHitter, 0);
            }
        });
        for (int tick : WINGS) {
            timeline.at(tick, () -> stage.sound("mob.enderdragon.wings", path[tick].add(0, HEAD, 0), 0.4f, 1.79f));
        }
        timeline.at(GRAB, () -> {
            Vec cry = origin.add(0, CRY_HEIGHT, 0);
            stage.sound("mob.bat.takeoff", cry, 1, 0.794f);
            stage.sound("mob.bat.idle", cry, 1, 0.587f);
            stage.particle("EXPLOSION_NORMAL", cry, 10, new Vec(0.3, 0.3, 0.3), 0.05);
        });
        timeline.at(SCREECH, () -> {
            Vec head = path[SCREECH].add(0, HEAD, 0);
            stage.sound("mob.bat.idle", head, 3, 0.49f);
            stage.sound("mob.enderdragon.wings", head, 3, 1.19f);
        });
        timeline.repeat(TRAIL_START, END - 1, 1, offset -> {
            Vec behind = path[TRAIL_START + offset - TRAIL_LAG].add(0, HEAD, 0);
            for (int feather = 0; feather < FEATHERS_PER_TICK; feather++) {
                double[] color = FEATHERS.get(random.nextInt(FEATHERS.size()));
                Vec at = behind.add(Kit.between(random, -FEATHER_SPREAD, FEATHER_SPREAD), Kit.between(random, -FEATHER_SPREAD, FEATHER_SPREAD),
                    Kit.between(random, -FEATHER_SPREAD, FEATHER_SPREAD));
                Kit.dust(stage, at, color[0], color[1], color[2]);
            }
        });
        timeline.at(END, () -> {
            Vec head = path[END].add(0, HEAD, 0);
            stage.sound("mob.chicken.plop", head, 3, 0.794f);
            stage.sound("random.fizz", head, 1.5f, 1.49f);
            stage.particle("CLOUD", head, 25, new Vec(0.5, 1, 0.5), 0.05);
            owl.remove();
            body.remove();
        });
        timeline.lasts(END + 1);
    }

    static double glide(int step) {
        double last = Math.pow(GLIDE, ARRIVAL);
        return (Math.pow(GLIDE, Math.min(step, ARRIVAL)) - last) / (1 - last);
    }

    static double flown(int step) {
        double total = 0;
        for (int index = 0; index < step; index++) {
            total += CRUISE + BOOST * Math.pow(ACCELERATION, index);
        }
        return total;
    }

    static Vec position(Vec grip, Vec right, Vec ahead, int tick) {
        if (tick <= GRAB) {
            double weight = glide(tick == 0 ? 0 : tick + 1);
            return grip.add(right.multiply(START_SIDE * weight)).add(ahead.multiply(START_AHEAD * weight)).add(0, START_UP * weight, 0);
        }
        double distance = flown(tick - GRAB);
        return grip.add(right.multiply(distance)).add(ahead.multiply(DRIFT * distance)).add(0, CLIMB * distance, 0);
    }
}
