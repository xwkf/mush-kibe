package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
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

public final class SixSevenFinalKill implements SceneAnimation {

    static final String DANCER = "b615b909152944c323b75daf96aed4cf800f338b65c475823a575c9f13856556";
    static final StandStyle DANCER_STYLE = new StandStyle(false, false, true, false, true);
    static final int STAND_FLAGS = 10;
    static final byte GROWN = 28;
    static final byte SHRUNK = 29;
    static final Vec LEFT_LEG = new Vec(0, 0, -5);
    static final Vec RIGHT_LEG = new Vec(0, 0, 5);
    static final Pose REST = new Pose(Vec.ZERO, Vec.ZERO, Vec.ZERO, Vec.ZERO, LEFT_LEG, RIGHT_LEG);
    static final Pose SIX_UP = new Pose(new Vec(0, 0, -1), new Vec(0, 0, -1), new Vec(-30, 20, 0), new Vec(-60, -20, 0), LEFT_LEG, RIGHT_LEG);
    static final Pose SEVEN_UP = new Pose(new Vec(0, 0, 1), new Vec(0, 0, 1), new Vec(-60, 20, 0), new Vec(-30, -20, 0), LEFT_LEG, RIGHT_LEG);
    static final Hand HANGING = new Hand(0.4, 0.78, 0);
    static final Hand LOWERED = new Hand(0.257, 0.906, 0.366);
    static final Hand RAISED = new Hand(0.156, 1.188, 0.644);
    static final Hand SHRUNK_LOWERED = new Hand(0, 0.19, 0.19);
    static final Hand SHRUNK_RAISED = new Hand(0, 0.33, 0.31);
    static final int SWAY = 6;
    static final int EASE = 5;
    static final int BEAT = 5;
    static final int CALM_BEATS = 4;
    static final int BEATS = 7;
    static final int FIRST_SHRINK = 21;
    static final int REGROW = 26;
    static final int SECOND_SHRINK = 31;
    static final int BLAST = 35;
    static final int FOUNTAIN_START = 37;
    static final int FOUNTAIN = 10;
    static final double SLOWEST_THROW = 0.13;
    static final double FASTEST_THROW = 0.46;
    static final double LOWEST_THROW = 0.55;
    static final double HIGHEST_THROW = 0.95;
    static final int END = 79;

    @Override
    public String id() {
        return "six-seven";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec toward = scene.facing().multiply(-1);
        Actor dancer = stage.armorStand(origin, origin.yawTowards(origin.add(toward)), DANCER_STYLE);
        dancer.equipSkull(Slot.HELMET, DANCER);
        Actor six = label(stage, place(origin, toward, true, HANGING), SixSevenAnimation.SIX);
        Actor seven = label(stage, place(origin, toward, false, HANGING), SixSevenAnimation.SEVEN);
        timeline.repeat(0, BLAST - 1, 1, tick -> {
            dancer.pose(pose(tick));
            six.teleport(hand(origin, toward, tick, true), 0, 0);
            seven.teleport(hand(origin, toward, tick, false), 0, 0);
        });
        for (int beat = 0; beat < BEATS; beat++) {
            int index = beat;
            timeline.at(beat * BEAT, () -> beat(stage, dancer, origin, index));
        }
        timeline.at(FIRST_SHRINK, () -> dancer.watch(STAND_FLAGS, SHRUNK));
        timeline.at(REGROW, () -> dancer.watch(STAND_FLAGS, GROWN));
        timeline.at(SECOND_SHRINK, () -> dancer.watch(STAND_FLAGS, SHRUNK));
        timeline.at(BLAST, () -> {
            stage.sound("random.explode", origin, 1, 2);
            dancer.remove();
            six.remove();
            seven.remove();
        });
        List<Actor> fountain = new ArrayList<>();
        for (int index = 0; index < FOUNTAIN; index++) {
            throwLabel(stage, timeline, random, origin, index, fountain);
        }
        timeline.at(END, () -> fountain.forEach(Actor::remove));
    }

    static Pose pose(int tick) {
        int sway = sway(tick);
        double progress = progress(tick - sway * SWAY);
        Pose from = sway == 0 ? REST : target(sway - 1);
        Pose to = target(sway);
        return new Pose(lerp(from.head(), to.head(), progress), lerp(from.body(), to.body(), progress), lerp(from.leftArm(), to.leftArm(), progress),
            lerp(from.rightArm(), to.rightArm(), progress), LEFT_LEG, RIGHT_LEG);
    }

    static Vec hand(Vec origin, Vec toward, int tick, boolean six) {
        int sway = sway(tick);
        boolean shrunk = shrunk(tick);
        Hand from = sway == 0 ? HANGING : hand(raised(sway - 1, six), shrunk);
        Hand to = hand(raised(sway, six), shrunk);
        double progress = progress(tick - sway * SWAY);
        Hand between = new Hand(from.side() + (to.side() - from.side()) * progress, from.up() + (to.up() - from.up()) * progress,
            from.toward() + (to.toward() - from.toward()) * progress);
        return place(origin, toward, six, between);
    }

    static Vec place(Vec origin, Vec toward, boolean six, Hand hand) {
        Vec side = new Vec(-toward.z(), 0, toward.x()).multiply(six ? 1 : -1);
        return origin.add(side.multiply(hand.side())).add(toward.multiply(hand.toward())).add(0, hand.up(), 0);
    }

    static boolean shrunk(int tick) {
        return tick >= FIRST_SHRINK && tick < REGROW || tick >= SECOND_SHRINK;
    }

    static double progress(int since) {
        double done = Math.min(1, (since + 1) / (double) EASE);
        return 1 - Math.pow(1 - done, 4);
    }

    private static int sway(int tick) {
        return Math.min(tick / SWAY, (BLAST - 1) / SWAY);
    }

    private static Pose target(int sway) {
        return sway % 2 == 0 ? SIX_UP : SEVEN_UP;
    }

    private static boolean raised(int sway, boolean six) {
        return (sway % 2 == 0) == six;
    }

    private static Hand hand(boolean raised, boolean shrunk) {
        if (shrunk) {
            return raised ? SHRUNK_RAISED : SHRUNK_LOWERED;
        }
        return raised ? RAISED : LOWERED;
    }

    private static Vec lerp(Vec from, Vec to, double progress) {
        return from.add(to.subtract(from).multiply(progress));
    }

    private static void beat(Stage stage, Actor dancer, Vec origin, int beat) {
        if (beat < CALM_BEATS) {
            stage.sound("game.neutral.hurt", origin, 1, 1.49f);
            return;
        }
        stage.sound("random.fizz", origin, 1, beat % 2 == 0 ? 1 : 1.49f);
        stage.sound("game.neutral.hurt", origin, 1, 2);
        dancer.status(2);
    }

    private static Actor label(Stage stage, Vec position, String text) {
        Actor label = stage.armorStand(position, 0, StandStyle.HIDDEN);
        label.watch(SixSevenAnimation.CUSTOM_NAME, text);
        label.watch(SixSevenAnimation.NAME_VISIBLE, (byte) 1);
        return label;
    }

    private static void throwLabel(Stage stage, Timeline timeline, RandomGenerator random, Vec origin, int index, List<Actor> fountain) {
        int tick = FOUNTAIN_START + Math.max(0, index - 1);
        Vec velocity = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, SLOWEST_THROW, FASTEST_THROW))
            .add(0, Kit.between(random, LOWEST_THROW, HIGHEST_THROW), 0);
        Body body = Body.living(origin, velocity);
        Actor[] thrown = new Actor[1];
        timeline.at(tick, () -> {
            thrown[0] = label(stage, origin, index % 2 == 0 ? SixSevenAnimation.SEVEN : SixSevenAnimation.SIX);
            fountain.add(thrown[0]);
            stage.sound("mob.chicken.plop", origin, 1, (float) Kit.between(random, 1, 1.9));
        });
        timeline.repeat(tick + 1, END - 1, 1, ignored -> {
            if (!body.landed()) {
                body.step(stage);
                thrown[0].teleport(body.position(), 0, 0);
            }
        });
    }

    record Hand(double side, double up, double toward) {
    }
}
