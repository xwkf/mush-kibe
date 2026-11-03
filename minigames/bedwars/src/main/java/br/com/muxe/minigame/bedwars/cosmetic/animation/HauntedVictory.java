package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Burst;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class HauntedVictory implements SceneAnimation {

    static final long NIGHT = 18000;
    static final int END = 200;
    static final List<String> CANDY = List.of(
        "c8713eecbcf5b28e29fda7becb24def977b4e7b28d1785a3eb7b6e0590fd59aa",
        "9e1a1d83f0bb7cd876e9d8c7a1dd56a90756621db1f13eada1814da7d022cdf3",
        "6c3a141f65d4101355e3ef0ed88da088e6d003ab6ec9106548ea13128911f334"
    );
    static final String GHOST = "c11de96c3e7b52fa5e4c794b4be14a05fc67694ca5eff9c9eb8baac672d21c09";
    static final StandStyle CANDY_STYLE = new StandStyle(true, true, false, false, false);
    static final int GHASTS_AT = 1;
    static final double ORBIT_RADIUS = 10;
    static final double ORBIT_STEP = 0.025;
    static final double ORBIT_HEIGHT = 9;
    static final double BOB = 0.8;
    static final int BOB_PERIOD = 80;
    static final int ANNOUNCE_EVERY = 10;
    static final double MOAN_CHANCE = 0.016;
    static final int SCREAM_AT = 40;
    static final int FIRST_HEAD = 2;
    static final int HEAD_EVERY = 8;
    static final int LAST_HEAD = 192;
    static final double MIN_DISTANCE = 3;
    static final double MAX_DISTANCE = 12;
    static final double MIN_HEIGHT = 11.2;
    static final double MAX_HEIGHT = 15.2;
    static final double BASE_BELOW_GROUND = 0.78;
    static final double HEAD_ABOVE_BASE = 0.7;
    static final double FALL_START_SPEED = 0.5;
    static final double FALL_ACCELERATION = 0.0625;
    static final double FALL_SPIN = -23.9;
    static final double FALL_ROLL = 14.4;
    static final int VOID_LIFE = 99;
    static final int LANDED_WAIT = 2;
    static final double TRAIL_SPACING = 0.35;
    static final double[][] TRAIL_COLORS = {
        {0.69, 0.42, 1.0},
        {1.0, 0.37, 0.64},
        {1.0, 0.6, 0.12},
        {0.37, 0.85, 1.0},
        {0.55, 1.0, 0.35},
        {1.0, 0.88, 0.3}
    };
    static final Material[] TREATS = {Material.CAKE, Material.SUGAR, Material.COOKIE};
    static final int TREATS_PER_BURST = 3;
    static final int TREAT_LIFE = 40;
    static final double TREAT_SIDE = 0.15;
    static final double TREAT_MIN_UP = 0.32;
    static final double TREAT_MAX_UP = 0.44;
    static final double BURST_HEIGHT = 0.5;
    static final int POPS_PER_BURST = 3;
    static final double POP_START_BELOW = 0.2;
    static final double POP_LAUNCH = 0.53;
    static final double POP_GRAVITY = 0.08;
    static final double POP_DRIFT = 0.1;
    static final double POP_DRIFT_DECAY = 0.85;
    static final double POP_SPIN = -29.5;
    static final int POP_LIFE = 40;
    static final double FUSE_CHANCE = 0.19;
    static final int FUSE_COLUMN = 4;
    static final int FUSE_HOVER = 12;
    static final double FUSE_JITTER = 0.1;
    static final double FUSE_ROLL = 14;
    static final double FUSE_SMOKE_HEIGHT = 0.3;
    static final double FUSE_SMOKE_STEP = 1.06;
    static final double FUSE_BLAST_HEIGHT = 0.9;
    static final int[] FUSE_COLORS = {0x8A2BE2, 0x7CFF3A};
    static final int GHOSTS = 3;
    static final int GHOST_LIFE = 14;
    static final double GHOST_SPREAD = 1.2;
    static final double GHOST_RISE_START = 0.62;
    static final double GHOST_RISE_DECAY = 0.045;
    static final double GHOST_RISE_MIN = 0.06;
    static final double GHOST_WOBBLE = 10;

    @Override
    public String id() {
        return "apavorante";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Show show = new Show(scene, stage, random);
        timeline.at(0, () -> stage.time(NIGHT));
        timeline.repeat(0, END, 1, show::tick);
        timeline.lasts(END);
    }

    static double drop(int age) {
        return FALL_START_SPEED * age + FALL_ACCELERATION * age * (age + 1) / 2.0;
    }

    static Vec ghastPosition(Vec origin, int age, int index) {
        double angle = ORBIT_STEP * age + index * Math.PI;
        double sign = index == 0 ? 1 : -1;
        double height = ORBIT_HEIGHT + sign * BOB * Math.sin(2 * Math.PI * age / BOB_PERIOD);
        return origin.add(Math.cos(angle) * ORBIT_RADIUS, height, Math.sin(angle) * ORBIT_RADIUS);
    }

    static float ghastYaw(int age, int index) {
        return (float) Math.toDegrees(ORBIT_STEP * age + index * Math.PI);
    }

    static double ghostRise(int age) {
        double rise = 0;
        for (int tick = 0; tick < age; tick++) {
            rise += Math.max(GHOST_RISE_MIN, GHOST_RISE_START - GHOST_RISE_DECAY * tick);
        }
        return rise;
    }

    static Vec ghostPosition(Vec start, double degrees, int age) {
        Vec outward = Kit.horizontal(degrees, GHOST_SPREAD * age / GHOST_LIFE);
        return start.add(outward).add(0, ghostRise(age), 0);
    }

    static double ghostWobble(int age) {
        return GHOST_WOBBLE * (1 - Math.cos(Math.toRadians(45 * age)));
    }

    static boolean headTick(int tick) {
        return tick >= FIRST_HEAD && tick <= LAST_HEAD && (tick - FIRST_HEAD) % HEAD_EVERY == 0;
    }

    static OptionalDouble floor(Stage stage, double x, double y, double z) {
        double probe = y + 0.5;
        double found = Ground.surface(stage::solid, x, probe, z);
        return found == probe ? OptionalDouble.empty() : OptionalDouble.of(found);
    }

    private interface Piece {

        boolean step();

        void remove();
    }

    private static final class Show {

        private final Scene scene;
        private final Stage stage;
        private final RandomGenerator random;
        private final Vec origin;
        private final Actor[] ghasts = new Actor[2];
        private final List<Piece> pieces = new ArrayList<>();

        Show(Scene scene, Stage stage, RandomGenerator random) {
            this.scene = scene;
            this.stage = stage;
            this.random = random;
            this.origin = scene.origin();
        }

        void tick(int tick) {
            if (tick >= END) {
                clear();
                return;
            }
            Vec winner = winner();
            if (tick >= GHASTS_AT) {
                ghasts(tick - GHASTS_AT);
                if ((tick - GHASTS_AT) % ANNOUNCE_EVERY == 0) {
                    stage.sound("fireworks.launch", winner, 3, 1);
                }
            }
            if (headTick(tick)) {
                head(winner);
            }
            for (Piece piece : List.copyOf(pieces)) {
                if (piece.step()) {
                    pieces.remove(piece);
                }
            }
        }

        private Vec winner() {
            if (scene.riders().isEmpty()) {
                return origin;
            }
            return stage.eyes(scene.riders().getFirst()).map(eyes -> eyes.add(0, -1.62, 0)).orElse(origin);
        }

        private void ghasts(int age) {
            for (int index = 0; index < ghasts.length; index++) {
                Vec at = ghastPosition(origin, age, index);
                if (ghasts[index] == null) {
                    ghasts[index] = stage.mob("Ghast", at, ghastYaw(age, index));
                } else {
                    ghasts[index].teleport(at, ghastYaw(age, index), 0);
                    if (random.nextDouble() < MOAN_CHANCE) {
                        stage.sound("mob.ghast.moan", at, 0.6f, (float) Kit.between(random, 0.7, 0.9));
                    }
                }
            }
            if (age == SCREAM_AT) {
                stage.sound("mob.ghast.scream", ghastPosition(origin, age, 0), 0.6f, 0.92f);
            }
        }

        private void head(Vec winner) {
            Vec away = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, MIN_DISTANCE, MAX_DISTANCE));
            double x = winner.x() + away.x();
            double z = winner.z() + away.z();
            OptionalDouble ground = floor(stage, x, winner.y(), z);
            double base = ground.isPresent() ? ground.getAsDouble() : winner.y();
            pieces.add(new Fall(x, z, base + Kit.between(random, MIN_HEIGHT, MAX_HEIGHT), ground));
        }

        private void clear() {
            for (Piece piece : pieces) {
                piece.remove();
            }
            pieces.clear();
            for (Actor ghast : ghasts) {
                if (ghast != null) {
                    ghast.remove();
                }
            }
        }

        private String candy() {
            return CANDY.get(random.nextInt(CANDY.size()));
        }

        private void trail(Vec from, Vec to) {
            int puffs = Math.max(1, (int) Math.ceil(from.distance(to) / TRAIL_SPACING));
            for (int puff = 0; puff < puffs; puff++) {
                Vec at = from.add(to.subtract(from).multiply((puff + 1.0) / puffs));
                if (puff % 2 == 0) {
                    double[] color = TRAIL_COLORS[random.nextInt(TRAIL_COLORS.length)];
                    Kit.dust(stage, at, color[0], color[1], color[2]);
                } else {
                    stage.particle("FIREWORKS_SPARK", at, 1, Vec.ZERO, 0);
                }
            }
        }

        private void burst(double x, double floor, double z) {
            if (random.nextDouble() < FUSE_CHANCE) {
                pieces.add(new Fuse(x, floor, z));
                return;
            }
            Vec centre = new Vec(x, floor + BURST_HEIGHT, z);
            stage.sound("mob.chicken.plop", centre, 0.6f, (float) Kit.between(random, 1.2, 1.5));
            stage.particle("FIREWORKS_SPARK", centre, 10, Vec.ZERO, 0.15);
            for (int treat = 0; treat < TREATS_PER_BURST; treat++) {
                Vec velocity = new Vec(Kit.between(random, -TREAT_SIDE, TREAT_SIDE), Kit.between(random, TREAT_MIN_UP, TREAT_MAX_UP), Kit.between(random, -TREAT_SIDE, TREAT_SIDE));
                pieces.add(new Treat(centre, velocity));
            }
            for (int pop = 0; pop < POPS_PER_BURST; pop++) {
                pieces.add(new Pop(x, floor, z));
            }
        }

        private final class Fall implements Piece {

            private final Actor stand;
            private final double x;
            private final double z;
            private final double startY;
            private final OptionalDouble ground;
            private Vec position;
            private int age;
            private int landedAge = -1;

            Fall(double x, double z, double startY, OptionalDouble ground) {
                this.x = x;
                this.z = z;
                this.startY = startY;
                this.ground = ground;
                this.position = new Vec(x, startY, z);
                this.stand = stage.armorStand(position, 0, CANDY_STYLE);
                this.stand.equipSkull(Slot.HELMET, candy());
            }

            @Override
            public boolean step() {
                age++;
                if (landedAge >= 0) {
                    if (age - landedAge < LANDED_WAIT) {
                        return false;
                    }
                    stand.remove();
                    burst(x, ground.getAsDouble(), z);
                    return true;
                }
                double y = startY - drop(age);
                if (ground.isPresent() && y <= ground.getAsDouble() - BASE_BELOW_GROUND) {
                    y = ground.getAsDouble() - BASE_BELOW_GROUND;
                    landedAge = age;
                }
                Vec next = new Vec(x, y, z);
                stand.teleport(next, (float) (FALL_SPIN * age), 0);
                stand.pose(Pose.STANDING.withHead(new Vec(0, 0, FALL_ROLL * age)));
                trail(position.add(0, HEAD_ABOVE_BASE, 0), next.add(0, HEAD_ABOVE_BASE, 0));
                position = next;
                if (landedAge < 0 && ground.isEmpty() && age >= VOID_LIFE) {
                    stand.remove();
                    return true;
                }
                return false;
            }

            @Override
            public void remove() {
                stand.remove();
            }
        }

        private final class Treat implements Piece {

            private final Actor item;
            private final Body body;
            private int age;

            Treat(Vec centre, Vec velocity) {
                this.body = Body.item(centre, velocity);
                this.item = stage.item(centre, TREATS[random.nextInt(TREATS.length)], 0);
            }

            @Override
            public boolean step() {
                age++;
                body.step(stage);
                item.teleport(body.position(), 0, 0);
                if (age >= TREAT_LIFE) {
                    item.remove();
                    return true;
                }
                return false;
            }

            @Override
            public void remove() {
                item.remove();
            }
        }

        private final class Pop implements Piece {

            private final Actor stand;
            private final double landY;
            private final double yaw;
            private double x;
            private double y;
            private double z;
            private double vy = POP_LAUNCH;
            private double driftX;
            private double driftZ;
            private int age;
            private int landedAge = -1;

            Pop(double x, double floor, double z) {
                this.x = x;
                this.z = z;
                this.landY = floor - BASE_BELOW_GROUND;
                this.y = floor - POP_START_BELOW;
                this.yaw = random.nextDouble() * 360;
                Vec drift = Kit.horizontal(random.nextDouble() * 360, POP_DRIFT);
                this.driftX = drift.x();
                this.driftZ = drift.z();
                this.stand = stage.armorStand(new Vec(x, y, z), (float) yaw, CANDY_STYLE);
                this.stand.equipSkull(Slot.HELMET, candy());
            }

            @Override
            public boolean step() {
                age++;
                if (landedAge >= 0) {
                    stage.sound("mob.chicken.plop", new Vec(x, y + HEAD_ABOVE_BASE, z), 0.3f, (float) Kit.between(random, 1.4, 1.9));
                    stand.remove();
                    return true;
                }
                Vec before = new Vec(x, y, z);
                x += driftX;
                z += driftZ;
                driftX *= POP_DRIFT_DECAY;
                driftZ *= POP_DRIFT_DECAY;
                y += vy;
                vy -= POP_GRAVITY;
                if (vy < 0 && y <= landY) {
                    y = landY;
                    landedAge = age;
                }
                Vec next = new Vec(x, y, z);
                stand.teleport(next, (float) (yaw + POP_SPIN * age), 0);
                trail(before.add(0, HEAD_ABOVE_BASE, 0), next.add(0, HEAD_ABOVE_BASE, 0));
                if (age >= POP_LIFE) {
                    stand.remove();
                    return true;
                }
                return false;
            }

            @Override
            public void remove() {
                stand.remove();
            }
        }

        private final class Fuse implements Piece {

            private final double x;
            private final double floor;
            private final double z;
            private Actor hover;
            private int age;

            Fuse(double x, double floor, double z) {
                this.x = x;
                this.floor = floor;
                this.z = z;
                stage.sound("creeper.primed", new Vec(x, floor + HEAD_ABOVE_BASE, z), 0.7f, 1);
            }

            @Override
            public boolean step() {
                age++;
                double smoke = floor + FUSE_SMOKE_HEIGHT + (age <= FUSE_COLUMN ? FUSE_SMOKE_STEP * (age - 1) : 0);
                if (age < FUSE_HOVER + 1) {
                    stage.particle("SMOKE_LARGE", new Vec(x, smoke, z), 1, new Vec(0.1, 0.1, 0.1), 0);
                    jitter();
                    return false;
                }
                explode();
                return true;
            }

            private void jitter() {
                Vec at = new Vec(x + Kit.between(random, -FUSE_JITTER, FUSE_JITTER), floor, z + Kit.between(random, -FUSE_JITTER, FUSE_JITTER));
                if (hover == null) {
                    hover = stage.armorStand(at, 0, CANDY_STYLE);
                    hover.equipSkull(Slot.HELMET, candy());
                }
                hover.teleport(at, 0, 0);
                hover.pose(Pose.STANDING.withHead(new Vec(0, 0, Kit.between(random, -FUSE_ROLL, FUSE_ROLL))));
            }

            private void explode() {
                if (hover != null) {
                    hover.remove();
                }
                Vec blast = new Vec(x, floor + FUSE_BLAST_HEIGHT, z);
                stage.sound("random.explode", blast, 0.3f, 1.59f);
                stage.sound("mob.ghast.moan", blast, 0.5f, 0.7f);
                stage.particle("SMOKE_LARGE", blast, 15, new Vec(0.4, 0.4, 0.4), 0.05);
                stage.particle("SPELL_WITCH", blast, 20, new Vec(0.3, 0.3, 0.3), 0.3);
                pieces.add(new Rocket(blast));
                double base = random.nextDouble() * 360;
                for (int ghost = 0; ghost < GHOSTS; ghost++) {
                    pieces.add(new Ghost(new Vec(x, floor, z), base + ghost * 360.0 / GHOSTS));
                }
            }

            @Override
            public void remove() {
                if (hover != null) {
                    hover.remove();
                }
            }
        }

        private final class Rocket implements Piece {

            private final Actor rocket;
            private int age;

            Rocket(Vec at) {
                Burst burst = new Burst(Kit.CREEPER, List.of(FUSE_COLORS[0], FUSE_COLORS[1]), List.of(0), false, false);
                this.rocket = stage.firework(at, List.of(burst));
                this.rocket.velocity(new Vec(0, 0.05, 0));
            }

            @Override
            public boolean step() {
                age++;
                if (age == 1) {
                    rocket.status(17);
                    return false;
                }
                rocket.remove();
                return true;
            }

            @Override
            public void remove() {
                rocket.remove();
            }
        }

        private final class Ghost implements Piece {

            private final Actor stand;
            private final Vec start;
            private final double degrees;
            private int age;

            Ghost(Vec start, double degrees) {
                this.start = start;
                this.degrees = degrees;
                this.stand = stage.armorStand(start, (float) degrees, StandStyle.HIDDEN_SMALL);
                this.stand.equipSkull(Slot.HELMET, GHOST);
            }

            @Override
            public boolean step() {
                age++;
                Vec at = ghostPosition(start, degrees, age);
                if (age >= GHOST_LIFE) {
                    stage.particle("FIREWORKS_SPARK", at.add(0, HEAD_ABOVE_BASE, 0), 5, Vec.ZERO, 0.08);
                    stand.remove();
                    return true;
                }
                stand.teleport(at, (float) degrees, 0);
                stand.pose(Pose.STANDING.withHead(new Vec(0, 0, ghostWobble(age))));
                return false;
            }

            @Override
            public void remove() {
                stand.remove();
            }
        }
    }
}
