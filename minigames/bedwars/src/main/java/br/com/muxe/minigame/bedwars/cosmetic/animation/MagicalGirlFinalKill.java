package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class MagicalGirlFinalKill implements SceneAnimation {

    static final String STAR = "a64c1490c1254807b7e3fe7b3a5a3b3f15cc699ab3872cc02968e9712570ef99";
    static final double RAINBOW_ABOVE = 1;
    static final double RAINBOW_SPAN = 4;
    static final int RAINBOW_GROWTH = 14;
    static final int RAINBOW_LEAD = 2;
    static final int RAINBOW_EVERY = 2;
    static final int STARS = 10;
    static final int FIRST_STAR = 14;
    static final int STAR_EVERY = 2;
    static final double STAR_SPREAD = 0.25;
    static final double STAR_LIFT = 1.45;
    static final double HEAD_ABOVE_BASE = 1.44;
    static final double TRAIL_ABOVE_BODY = 0.36;
    static final double TRAIL_STEP = 0.13;
    static final double RIBBON_GAP = 0.1;
    static final double[][] RIBBON = {{1, 0.96, 0.64}, {1, 0.84, 0}, {0.9, 0.66, 0}};
    static final double SPARK_CHANCE = 0.5;
    static final float SPIN = 91.4f;
    static final double SPIN_DECAY = 0.873;
    static final float FIRST_CHIME = 1;
    static final float CHIME_STEP = 0.1f;
    static final float FIRST_ORB = 94 / 63f;
    static final float ORB_STEP = 3.1f / 63;
    static final int END = 92;

    @Override
    public String id() {
        return "garota-magica";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        stage.sound("mob.enderdragon.growl", origin, 1, 1);
        Rainbow rainbow = rainbow(scene);
        timeline.repeat(0, RAINBOW_GROWTH - RAINBOW_LEAD, 1, tick -> rainbow.draw(stage, growth(tick)));
        timeline.repeat(RAINBOW_GROWTH, END, RAINBOW_EVERY, tick -> rainbow.draw(stage, 1));
        double first = random.nextDouble() * 360;
        for (int star = 0; star < STARS; star++) {
            star(stage, timeline, random, origin, star, first + star * 360.0 / STARS);
        }
        timeline.lasts(END + 1);
    }

    static Rainbow rainbow(Scene scene) {
        return new Rainbow(scene.origin().add(0, RAINBOW_ABOVE, 0), scene.right().multiply(-1), RAINBOW_SPAN);
    }

    static double growth(int tick) {
        return Math.min(1, (double) (tick + RAINBOW_LEAD) / RAINBOW_GROWTH);
    }

    static Vec launch(double degrees) {
        return Kit.horizontal(degrees, STAR_SPREAD).add(0, STAR_LIFT, 0);
    }

    static Vec ribbonSide(Vec step, Vec heading) {
        double along = step.x() * heading.x() + step.z() * heading.z();
        return heading.multiply(-step.y()).add(0, along, 0).normalize();
    }

    private static void star(Stage stage, Timeline timeline, RandomGenerator random, Vec origin, int index, double degrees) {
        int start = FIRST_STAR + index * STAR_EVERY;
        Body body = Body.living(origin, launch(degrees));
        Vec heading = Kit.horizontal(degrees, 1);
        List<Vec> path = new ArrayList<>(List.of(origin.add(0, TRAIL_ABOVE_BODY, 0)));
        Actor[] head = new Actor[1];
        float[] spin = {SPIN};
        float[] yaw = {0};
        timeline.at(start, () -> {
            head[0] = stage.armorStand(origin.add(0, -HEAD_ABOVE_BASE, 0), 0, new StandStyle(true, false, false, false, false));
            head[0].equipSkull(Slot.HELMET, STAR);
            stage.sound("note.pling", origin, 0.6f, FIRST_CHIME + index * CHIME_STEP);
            stage.sound("random.orb", origin, 0.3f, FIRST_ORB + index * ORB_STEP);
        });
        timeline.repeat(start + 1, END - 1, 1, offset -> {
            if (body.landed()) {
                return;
            }
            Vec before = body.position();
            Vec at = body.step(stage) ? landing(stage, before, body.position()) : body.position();
            yaw[0] -= spin[0];
            spin[0] *= (float) SPIN_DECAY;
            head[0].teleport(at.add(0, -HEAD_ABOVE_BASE, 0), yaw[0], 0);
            Vec from = path.getLast();
            Vec to = at.add(0, TRAIL_ABOVE_BODY, 0);
            trail(stage, from, to, heading);
            path.add(to);
            if (random.nextDouble() < SPARK_CHANCE) {
                stage.particle("FIREWORKS_SPARK", path.get(random.nextInt(path.size())), 0, Vec.ZERO, 0);
            }
        });
        timeline.at(END, () -> head[0].remove());
    }

    static Vec landing(Stage stage, Vec before, Vec landed) {
        int depth = (int) Math.ceil(before.y() - landed.y()) + 1;
        double floor = Ground.hit(stage::solid, landed.x(), before.y(), landed.z(), depth).orElse(landed.y());
        return new Vec(landed.x(), Math.max(floor, landed.y()), landed.z());
    }

    private static void trail(Stage stage, Vec from, Vec to, Vec heading) {
        Vec step = to.subtract(from);
        Vec side = ribbonSide(step, heading).multiply(RIBBON_GAP);
        int points = Math.max(1, (int) Math.ceil(step.length() / TRAIL_STEP));
        for (int point = 1; point <= points; point++) {
            Vec at = from.add(step.multiply((double) point / points));
            for (int strand = 0; strand < RIBBON.length; strand++) {
                double[] color = RIBBON[strand];
                Kit.dust(stage, at.add(side.multiply(1 - strand)), color[0], color[1], color[2]);
            }
        }
    }
}
