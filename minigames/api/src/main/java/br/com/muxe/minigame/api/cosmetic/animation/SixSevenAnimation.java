package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class SixSevenAnimation implements BedAnimation {

    public static final String SIX = "§l6";
    public static final String SEVEN = "§l7";
    public static final int CUSTOM_NAME = 2;
    static final int NAME_VISIBLE = 3;
    public static final int[] BEATS = {4, 10, 16, 21, 26, 30, 34, 37, 40, 42};
    static final double[] RISING = {0.34, 0.5, 0.56, 0.6};
    static final double[] BOUNCE = {0.625, 0.78, 0.84};
    public static final double HOVER = 0.875;
    static final double BEFORE_BEAT = 0.72;
    static final double SPREAD = 0.34;
    static final double LABEL_HEIGHT = 2;
    static final double NOTE_HEIGHT = 2.9;
    public static final double SWING_TOP = 2.594;
    public static final double SWING_BOTTOM = 1.594;
    static final int FINALE = 44;
    static final int GATHER = 5;
    static final int BLAST = FINALE + GATHER;
    static final double FINALE_LABEL = 6;
    static final double GATHERED = 4;
    static final double GATHERED_BED = 2.5;
    static final double BLAST_HEIGHT = 3;
    public static final int DUST = 40;
    public static final int FOUNTAIN = 14;
    static final int END = 109;

    @Override
    public String id() {
        return "six-seven";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Vec left = left(bed, breaker);
        Kit.BedDisplay display = Kit.bed(stage, bed);
        Actor six = label(stage, middle.add(left.multiply(SPREAD)).add(0, LABEL_HEIGHT, 0), SIX);
        Actor seven = label(stage, middle.add(left.multiply(-SPREAD)).add(0, LABEL_HEIGHT, 0), SEVEN);
        stage.sound("fireworks.launch", middle, 1, 2);
        timeline.repeat(0, FINALE, 1, tick -> {
            display.move(bed, new Vec(0, bedHeight(tick), 0));
            if (tick > BEATS[0]) {
                six.teleport(swingPoint(middle, left, tick, true), 0, 0);
                seven.teleport(swingPoint(middle, left, tick, false), 0, 0);
            }
            for (int beat = 0; beat < BEATS.length; beat++) {
                if (BEATS[beat] == tick) {
                    boolean sixBeats = beat % 2 == 0;
                    Vec at = middle.add(left.multiply(sixBeats ? SPREAD : -SPREAD)).add(0, NOTE_HEIGHT, 0);
                    stage.particle("NOTE", at, 0, Vec.ZERO, 1);
                    stage.sound("note.pling", at, 1, sixBeats ? 0.89f : 1.29f);
                    stage.sound("game.neutral.hurt", at, 1, sixBeats ? 1.49f : 2);
                }
            }
        });
        List<Actor> finale = new ArrayList<>();
        timeline.at(FINALE, () -> {
            stage.sound("fireworks.launch", middle, 1, 1.49f);
            stage.sound("note.pling", middle, 1, 2);
            finale.add(label(stage, middle.add(0, FINALE_LABEL, 0), SIX + SEVEN));
        });
        Vec gathered = middle.add(0, GATHERED, 0);
        timeline.repeat(FINALE + 1, BLAST - 1, 1, offset -> {
            int step = offset + 1;
            double eased = easeOut(step, GATHER);
            display.move(bed, new Vec(0, HOVER + (GATHERED_BED - HOVER) * eased, 0));
            Vec sixFrom = swingPoint(middle, left, FINALE, true);
            Vec sevenFrom = swingPoint(middle, left, FINALE, false);
            six.teleport(sixFrom.add(gathered.subtract(sixFrom).multiply(eased)), 0, 0);
            seven.teleport(sevenFrom.add(gathered.subtract(sevenFrom).multiply(eased)), 0, 0);
            Vec labelFrom = middle.add(0, FINALE_LABEL, 0);
            finale.getFirst().teleport(labelFrom.add(gathered.subtract(labelFrom).multiply(eased)), 0, 0);
        });
        Vec blast = middle.add(0, BLAST_HEIGHT, 0);
        timeline.at(BLAST, () -> {
            stage.sound("random.explode", blast, 4, 2);
            stage.particle("EXPLOSION_HUGE", blast, 1, Vec.ZERO, 1);
            display.remove();
            six.remove();
            seven.remove();
            finale.forEach(Actor::remove);
            for (int index = 0; index < DUST; index++) {
                Vec speck = blast.add(Kit.between(random, -1.5, 1.5), Kit.between(random, -1, 1), Kit.between(random, -1.5, 1.5));
                Kit.dust(stage, speck, 1, 1, 1);
            }
        });
        Kit.flash(stage, timeline, BLAST, blast, Kit.burst(Kit.LARGE_BALL, true, false, Kit.WHITE));
        List<Actor> fountain = new ArrayList<>();
        for (int index = 0; index < FOUNTAIN; index++) {
            launch(stage, timeline, random, blast, index, fountain);
        }
        timeline.at(END, () -> fountain.forEach(Actor::remove));
    }

    public static double bedHeight(int tick) {
        if (tick < BEATS[0]) {
            return RISING[tick];
        }
        int last = BEATS[0];
        int next = -1;
        for (int beat : BEATS) {
            if (beat <= tick) {
                last = beat;
            } else if (next < 0) {
                next = beat;
            }
        }
        if (next - tick == 1 && next - last >= 5) {
            return BEFORE_BEAT;
        }
        int since = tick - last;
        return since < BOUNCE.length ? BOUNCE[since] : HOVER;
    }

    public static double swingHeight(int tick, boolean six) {
        if (tick <= BEATS[0]) {
            return LABEL_HEIGHT;
        }
        int beat = 0;
        while (beat + 1 < BEATS.length && BEATS[beat + 1] < tick) {
            beat++;
        }
        int from = BEATS[beat];
        int to = beat + 1 < BEATS.length ? BEATS[beat + 1] : FINALE;
        boolean rising = (beat % 2 == 0) == six;
        double start = beat == 0 ? LABEL_HEIGHT : rising ? SWING_BOTTOM : SWING_TOP;
        double target = rising ? SWING_TOP : SWING_BOTTOM;
        return start + (target - start) * easeOut(tick - from, to - from);
    }

    static double easeOut(int step, int steps) {
        double progress = Math.min(1, (double) step / steps);
        return 1 - (1 - progress) * (1 - progress);
    }

    public static Vec left(BedFrame bed, Vec breaker) {
        Vec toBreaker = new Vec(breaker.x() - bed.middle().x(), 0, breaker.z() - bed.middle().z());
        if (toBreaker.length() < 0.01) {
            return bed.right().multiply(-1);
        }
        Vec towards = toBreaker.normalize();
        return new Vec(-towards.z(), 0, towards.x());
    }

    private static Vec swingPoint(Vec middle, Vec left, int tick, boolean six) {
        return middle.add(left.multiply(six ? SPREAD : -SPREAD)).add(0, swingHeight(tick, six), 0);
    }

    private static Actor label(Stage stage, Vec position, String text) {
        Actor label = stage.armorStand(position, 0, StandStyle.HIDDEN);
        label.watch(CUSTOM_NAME, text);
        label.watch(NAME_VISIBLE, (byte) 1);
        return label;
    }

    private static void launch(Stage stage, Timeline timeline, RandomGenerator random, Vec blast, int index, List<Actor> fountain) {
        int tick = BLAST + 1 + index;
        boolean six = index % 2 == 0;
        Vec velocity = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, 0.2, 0.36)).add(0, Kit.between(random, 0.4, 0.75), 0);
        Body body = new Body(blast, velocity, 0.08, 0.98, 0.95);
        timeline.at(tick, () -> {
            fountain.add(label(stage, blast, six ? SIX : SEVEN));
            stage.sound("mob.chicken.plop", blast, 1, six ? 1.19f : 1.79f);
        });
        timeline.repeat(tick + 1, END - 1, 1, ignored -> {
            if (body.landed()) {
                return;
            }
            body.step(stage);
            fountain.get(index).teleport(body.position(), 0, 0);
        });
    }
}
