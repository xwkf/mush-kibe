package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class BeehiveFinalKill implements SceneAnimation {

    static final String HIVE = "2c680149ad17e46fbbf7606b284cc83a03b1a67cd8a52717b44bfa3ad5914f14";
    static final String BEE = "722cc5ed03e6bdce81675fa44f740bb9aff0c1a662f185c31b05371c94fb7029";
    static final int HONEY = 0xC86A08;
    static final double FIREWORK_HEIGHT = 1;
    static final double HIVE_BASE = 0.78125;
    static final double HIVE_CENTER = 2.5;
    static final int BEES = 10;
    static final int FIRST_BEE = 5;
    static final int BEE_GAP_THIRDS = 14;
    static final double BEE_BASE = 1.5625;
    static final double BEE_HEAD = 0.957;
    static final int BEE_LIFE = 40;
    static final double BOB_CENTER = 0.5;
    static final double BOB_CLIMB = 0.01;
    static final double BOB_AMPLITUDE = 0.5;
    static final double BOB_PERIOD = 31.5;
    static final double BOB_PHASE = 12;
    static final double START_SPEED = 0.0625;
    static final double MIN_SPEED = 0.03;
    static final double MAX_SPEED = 0.2;
    static final double MAX_TURN = 25;
    static final double MAX_HEAD_TILT = 22;
    static final int FIRST_BUZZ = 23;
    static final int BUZZ_GAP_HALVES = 29;
    static final int BUZZES = 5;
    static final int END = 95;

    @Override
    public String id() {
        return "colmeia";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec hiveCenter = origin.add(0, HIVE_CENTER, 0);
        Kit.flash(stage, timeline, 0, origin.add(0, FIREWORK_HEIGHT, 0), Kit.burst(Kit.BALL, false, true, HONEY));
        Actor hive = stage.armorStand(origin.add(0, HIVE_BASE, 0), VictimBody.facingKiller(scene), StandStyle.HIDDEN);
        hive.equipSkull(Slot.HELMET, HIVE);
        stage.sound("random.wood_click", hiveCenter, 1, 0.79f);
        timeline.at(3, () -> stage.sound("random.door_open", hiveCenter, 1, 1.49f));
        for (int buzz = 0; buzz < BUZZES; buzz++) {
            timeline.at(FIRST_BUZZ + buzz * BUZZ_GAP_HALVES / 2, () -> stage.sound("mob.bat.loop", hiveCenter, 0.3f, 1.79f));
        }
        for (int bee = 0; bee < BEES; bee++) {
            bee(stage, timeline, origin, spawn(bee), random);
        }
        timeline.at(END, () -> {
            stage.particle("CLOUD", hiveCenter, 15, new Vec(0.01, 0.01, 0.01), 0.1);
            hive.remove();
        });
    }

    static int spawn(int bee) {
        return FIRST_BEE + bee * BEE_GAP_THIRDS / 3;
    }

    static double bob(int step) {
        return BOB_CENTER + BOB_CLIMB * step + BOB_AMPLITUDE * Math.sin(2 * Math.PI * step / BOB_PERIOD + Math.toRadians(BOB_PHASE));
    }

    private static void bee(Stage stage, Timeline timeline, Vec origin, int start, RandomGenerator random) {
        Wander wander = new Wander(random.nextInt(8) * 45.0, START_SPEED);
        Actor[] bee = new Actor[1];
        Vec[] at = {origin.add(0, BEE_BASE, 0)};
        double tilt = Kit.between(random, -MAX_HEAD_TILT, MAX_HEAD_TILT);
        timeline.at(start, () -> {
            bee[0] = stage.armorStand(at[0], 0, StandStyle.HIDDEN_SMALL);
            bee[0].equipSkull(Slot.HELMET, BEE);
            bee[0].pose(Pose.STANDING.withHead(new Vec(tilt, 0, 0)));
        });
        timeline.repeat(start, start + BEE_LIFE - 1, 1, step -> {
            Vec move = wander.step(random);
            at[0] = new Vec(at[0].x() + move.x(), origin.y() + BEE_BASE + bob(step), at[0].z() + move.z());
            bee[0].teleport(at[0], Vec.ZERO.yawTowards(move), 0);
        });
        timeline.at(start + BEE_LIFE, () -> {
            Vec head = at[0].add(0, BEE_HEAD, 0);
            stage.particle("CLOUD", head, 15, new Vec(0.01, 0.01, 0.01), 0.1);
            stage.sound("mob.chicken.plop", head, 1, 1);
            bee[0].remove();
        });
    }

    static final class Wander {

        private double heading;
        private double speed;

        Wander(double heading, double speed) {
            this.heading = heading;
            this.speed = speed;
        }

        Vec step(RandomGenerator random) {
            Vec move = Kit.horizontal(heading, speed);
            heading += Kit.between(random, -MAX_TURN, MAX_TURN);
            speed = Math.clamp(speed + Kit.between(random, -0.025, 0.03), MIN_SPEED, MAX_SPEED);
            return move;
        }
    }
}
