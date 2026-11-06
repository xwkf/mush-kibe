package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

final class ShootingStar {

    static final StandStyle STAND = new StandStyle(true, false, false, false, false);
    static final double HEAD = 1.69;
    static final double CHIME = 1.34;
    static final double DRIFT = 0.35;
    static final double FIRST_FALL = 0.38;
    static final double GRAVITY = 0.05;
    static final double DRAG = 0.985;
    static final float SPIN = 18;
    static final double[] SETTLE = {0.12, 0.09, 0.03};
    static final int FADE_AFTER = 4;
    static final int LIFETIME = 100;
    static final double TRAIL_STEP = 0.13;
    static final double STRAND_GAP = 0.1;
    static final double[][] RAINBOW = {{0.4, 0.2, 0.984}, {0.012, 0.608, 0.965}, {0.192, 0.996, 0.008}, {0.992, 0.988, 0}, {0.996, 0.604, 0.004},
        {0.988, 0.008, 0}};
    static final double SECOND_SPARK = 0.3;
    static final double SPARK_JITTER = 0.4;
    static final double GLITTER_CHANCE = 0.36;
    static final int GLITTER_REACH = 10;
    static final double GLITTER_JITTER = 0.5;
    static final int BURST = 20;
    static final double BURST_SPEED = 0.15;
    static final int FADE = 8;
    static final Vec FADE_SPREAD = new Vec(0.2, 0.2, 0.2);
    static final double FADE_SPEED = 0.05;
    static final float HIGH = SoundPitch.of(126);
    static final float TWINKLE = SoundPitch.of(94);

    private final Actor stand;
    private final Vec birth;
    private final Vec heading;
    private final List<Vec> path = new ArrayList<>();
    private Vec position;
    private Vec velocity;
    private float yaw;
    private int age;
    private int landedAt = -1;

    private ShootingStar(Actor stand, Vec position, Vec heading) {
        this.stand = stand;
        this.position = position;
        this.birth = head(position);
        this.heading = heading;
        this.velocity = heading.multiply(DRIFT).add(0, -FIRST_FALL, 0);
        path.add(birth);
    }

    static ShootingStar fall(Stage stage, Vec position, double degrees) {
        Actor stand = stage.armorStand(position, 0, STAND);
        stand.equipSkull(Slot.HELMET, MagicalGirlFinalKill.STAR);
        return new ShootingStar(stand, position, Kit.horizontal(degrees, 1));
    }

    static Vec head(Vec position) {
        return position.add(0, HEAD, 0);
    }

    static Vec fallStep(Vec velocity) {
        return new Vec(velocity.x(), (velocity.y() - GRAVITY) * DRAG, velocity.z());
    }

    static List<Vec> strands(Vec at, Vec step, Vec heading) {
        Vec side = MagicalGirlFinalKill.ribbonSide(step, heading).multiply(STRAND_GAP);
        double middle = (RAINBOW.length - 1) / 2.0;
        List<Vec> strands = new ArrayList<>();
        for (int strand = 0; strand < RAINBOW.length; strand++) {
            strands.add(at.add(side.multiply(strand - middle)));
        }
        return strands;
    }

    boolean step(Stage stage, RandomGenerator random) {
        age++;
        yaw -= SPIN;
        if (landedAt >= 0) {
            return settle(stage);
        }
        if (age > LIFETIME || position.y() < 0) {
            stand.remove();
            return true;
        }
        Vec before = head(position);
        Vec next = position.add(velocity);
        if (velocity.y() < 0 && stage.solid((int) Math.floor(next.x()), (int) Math.floor(next.y()), (int) Math.floor(next.z()))) {
            position = new Vec(next.x(), Math.floor(next.y()) + 1, next.z());
            land(stage);
        } else {
            position = next;
            velocity = fallStep(velocity);
        }
        stand.teleport(position, yaw, 0);
        Vec after = head(position);
        trail(stage, before, after);
        path.add(after);
        sparkle(stage, random, after);
        return false;
    }

    void remove() {
        stand.remove();
    }

    Vec position() {
        return position;
    }

    boolean landed() {
        return landedAt >= 0;
    }

    private void land(Stage stage) {
        landedAt = age;
        stage.particle("FIREWORKS_SPARK", birth, BURST, Vec.ZERO, BURST_SPEED);
        stage.sound("mob.chicken.plop", birth, 0.6f, HIGH);
        stage.sound("fireworks.twinkle", birth, 0.4f, TWINKLE);
    }

    private boolean settle(Stage stage) {
        int since = age - landedAt;
        if (since <= SETTLE.length) {
            position = position.add(0, SETTLE[since - 1], 0);
        }
        stand.teleport(position, yaw, 0);
        if (since < FADE_AFTER) {
            return false;
        }
        Vec at = head(position);
        stage.sound("random.fizz", at, 0.3f, HIGH);
        stage.particle("FIREWORKS_SPARK", at, FADE, FADE_SPREAD, FADE_SPEED);
        stand.remove();
        return true;
    }

    private void trail(Stage stage, Vec from, Vec to) {
        Vec step = to.subtract(from);
        int points = Math.max(1, (int) Math.ceil(step.length() / TRAIL_STEP));
        for (int point = 1; point <= points; point++) {
            List<Vec> strands = strands(from.add(step.multiply((double) point / points)), step, heading);
            for (int strand = 0; strand < strands.size(); strand++) {
                double[] color = RAINBOW[strand];
                Kit.dust(stage, strands.get(strand), color[0], color[1], color[2]);
            }
        }
    }

    private void sparkle(Stage stage, RandomGenerator random, Vec at) {
        spark(stage, random, at);
        if (random.nextDouble() < SECOND_SPARK) {
            spark(stage, random, at);
        }
        if (random.nextDouble() < GLITTER_CHANCE) {
            Vec along = path.get(Math.max(0, path.size() - 1 - random.nextInt(GLITTER_REACH)));
            stage.particle("CRIT_MAGIC", along.add(jitter(random, GLITTER_JITTER), jitter(random, GLITTER_JITTER), jitter(random, GLITTER_JITTER)), 0, Vec.ZERO, 0);
        }
    }

    private static void spark(Stage stage, RandomGenerator random, Vec at) {
        stage.particle("FIREWORKS_SPARK", at.add(jitter(random, SPARK_JITTER), jitter(random, SPARK_JITTER), jitter(random, SPARK_JITTER)), 1, Vec.ZERO, 0);
    }

    private static double jitter(RandomGenerator random, double reach) {
        return Kit.between(random, -reach, reach);
    }
}
