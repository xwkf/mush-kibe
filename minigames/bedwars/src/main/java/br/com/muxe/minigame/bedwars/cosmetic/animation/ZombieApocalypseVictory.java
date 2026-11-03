package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class ZombieApocalypseVictory implements SceneAnimation {

    static final int END = 200;
    static final int MAX_ALIVE = 20;
    static final int FIRST_SPAWN = 2;
    static final int SPAWN_EVERY = 2;
    static final double MIN_DISTANCE = 5;
    static final double MAX_DISTANCE = 11.3;
    static final double BURIED = 2;
    static final double RISE_STEP = 0.25;
    static final int RISE_TICKS = 8;
    static final double ADULT_SPEED = 0.2;
    static final double BABY_SPEED = 0.3;
    static final double BABY_CHANCE = 0.13;
    static final int BABY_FLAG = 12;
    static final double CLIMB = 0.3;
    static final double REACHED = 1.1;
    static final double SELF_DESTRUCT_CHANCE = 1.0 / 3;
    static final int MIN_FUSE_AGE = 12;
    static final int MAX_FUSE_AGE = 63;
    static final double MIN_FUSE_DISTANCE = 2;
    static final int CHARGE_TICKS = 16;
    static final double TALK_CHANCE = 0.25;
    static final int MAX_TALK_AGE = 60;
    static final int WITCH_EVERY = 8;
    static final double WITCH_HEIGHT = 1.6;
    static final int INTRO_BOLTS = 3;
    static final int[] INTRO_TICKS = {1, 5, 9};
    static final double[][] INTRO_OFFSETS = {{-3.9, -0.9}, {-3.2, -2.5}, {3.4, 2.0}};
    static final int EMERGENCE_BOLTS = 5;
    static final int FIRST_BOLT_STEP = 10;
    static final int BOLT_STEPS = 85;
    static final double SWIRL_RADIUS = 1.1;
    static final double SWIRL_TURN = 11.5;
    static final int SWIRLING = 3;
    static final int SMOKE_EVERY = 4;
    static final double WINNER_EYES = 1.62;
    static final int GROUND_BLOCK = Kit.blockId(Material.STONE, 0);

    @Override
    public String id() {
        return "apocalipse-zumbi";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Show show = new Show(scene, stage, random, boltTicks(random));
        Nightfall.schedule(stage, timeline);
        timeline.repeat(0, END, 1, show::tick);
        timeline.lasts(END);
    }

    static Set<Integer> boltTicks(RandomGenerator random) {
        Set<Integer> ticks = new HashSet<>();
        while (ticks.size() < EMERGENCE_BOLTS) {
            ticks.add(FIRST_SPAWN + SPAWN_EVERY * (FIRST_BOLT_STEP + random.nextInt(BOLT_STEPS)));
        }
        return ticks;
    }

    static boolean spawnTick(int tick) {
        return tick >= FIRST_SPAWN && tick < END && (tick - FIRST_SPAWN) % SPAWN_EVERY == 0;
    }

    static Vec swirl(Vec center, int tick, int index) {
        double degrees = SWIRL_TURN * tick + index * (360.0 / SWIRLING);
        double height = 1.0 + 0.9 * Math.sin(Math.toRadians(SWIRL_TURN * tick) / 2);
        Vec around = Kit.horizontal(degrees, SWIRL_RADIUS);
        return center.add(around.x(), height, around.z());
    }

    static Vec emergence(Vec winner, double degrees, double distance) {
        Vec away = Kit.horizontal(degrees, distance);
        return new Vec(Math.floor(winner.x() + away.x()) + 0.5, 0, Math.floor(winner.z() + away.z()) + 0.5);
    }

    static double speed(boolean baby) {
        return baby ? BABY_SPEED : ADULT_SPEED;
    }

    static Vec advance(Vec from, Vec target, double speed) {
        double dx = target.x() - from.x();
        double dz = target.z() - from.z();
        double distance = Math.hypot(dx, dz);
        if (distance <= speed) {
            return new Vec(target.x(), from.y(), target.z());
        }
        return new Vec(from.x() + dx / distance * speed, from.y(), from.z() + dz / distance * speed);
    }

    static double flat(Vec a, Vec b) {
        return Math.hypot(a.x() - b.x(), a.z() - b.z());
    }

    static double rising(double ground, int age) {
        return ground - BURIED + RISE_STEP * Math.min(age, RISE_TICKS);
    }

    static OptionalDouble floor(Stage stage, double x, double y, double z) {
        double probe = y + 0.5;
        double found = Ground.surface(stage::solid, x, probe, z);
        return found == probe ? OptionalDouble.empty() : OptionalDouble.of(found);
    }

    private static final class Show {

        private final Scene scene;
        private final Stage stage;
        private final RandomGenerator random;
        private final Set<Integer> bolts;
        private final Vec origin;
        private final List<Walker> zombies = new ArrayList<>();

        Show(Scene scene, Stage stage, RandomGenerator random, Set<Integer> bolts) {
            this.scene = scene;
            this.stage = stage;
            this.random = random;
            this.bolts = bolts;
            this.origin = scene.origin();
        }

        void tick(int tick) {
            if (tick >= END) {
                for (Walker zombie : zombies) {
                    zombie.remove();
                }
                zombies.clear();
                return;
            }
            Vec winner = winner();
            if (tick == 0) {
                stage.sound("ambient.weather.thunder", winner, 0.6f, 1);
            }
            for (int bolt = 0; bolt < INTRO_BOLTS; bolt++) {
                if (tick == INTRO_TICKS[bolt]) {
                    strike(origin.x() + INTRO_OFFSETS[bolt][0], origin.y(), origin.z() + INTRO_OFFSETS[bolt][1]);
                }
            }
            if (tick >= 1) {
                around(winner, tick);
            }
            if (spawnTick(tick)) {
                spawn(tick, winner);
            }
            for (Walker zombie : List.copyOf(zombies)) {
                if (zombie.step(winner)) {
                    zombies.remove(zombie);
                }
            }
        }

        private Vec winner() {
            if (scene.riders().isEmpty()) {
                return origin;
            }
            return stage.eyes(scene.riders().getFirst()).map(eyes -> eyes.add(0, -WINNER_EYES, 0)).orElse(origin);
        }

        private void around(Vec winner, int tick) {
            for (int index = 0; index < SWIRLING; index++) {
                stage.particle("SPELL_WITCH", swirl(winner, tick, index), 1, Vec.ZERO, 0);
            }
            if ((tick - 1) % SMOKE_EVERY == 0) {
                stage.particle("SMOKE_NORMAL", winner, 1, new Vec(0.4, 0, 0.4), 0.02);
            }
        }

        private void strike(double x, double y, double z) {
            OptionalDouble ground = floor(stage, x, y, z);
            stage.lightning(new Vec(x, ground.orElse(y), z));
        }

        private void spawn(int tick, Vec winner) {
            boolean bolt = bolts.contains(tick);
            if (!bolt && zombies.size() >= MAX_ALIVE) {
                return;
            }
            Vec spot = emergence(winner, random.nextDouble() * 360, Kit.between(random, MIN_DISTANCE, MAX_DISTANCE));
            OptionalDouble ground = floor(stage, spot.x(), winner.y(), spot.z());
            if (ground.isEmpty()) {
                return;
            }
            Vec point = new Vec(spot.x(), ground.getAsDouble(), spot.z());
            if (bolt) {
                stage.lightning(point);
            }
            if (zombies.size() < MAX_ALIVE) {
                zombies.add(new Walker(point));
            }
        }

        private final class Walker {

            private final Actor actor;
            private final boolean baby;
            private final float yaw;
            private final int fuseAge;
            private final int talkAge;
            private Vec position;
            private double groundY;
            private int age;
            private int charge = -1;

            Walker(Vec ground) {
                this.baby = random.nextDouble() < BABY_CHANCE;
                this.yaw = (float) (random.nextDouble() * 360 - 180);
                this.fuseAge = random.nextDouble() < SELF_DESTRUCT_CHANCE ? MIN_FUSE_AGE + random.nextInt(MAX_FUSE_AGE - MIN_FUSE_AGE + 1) : -1;
                this.talkAge = random.nextDouble() < TALK_CHANCE ? MIN_FUSE_AGE + random.nextInt(MAX_TALK_AGE) : -1;
                this.groundY = ground.y();
                this.position = new Vec(ground.x(), rising(groundY, 0), ground.z());
                stage.sound("dig.gravel", ground, 0.6f, 0.59f);
                this.actor = stage.mob("Zombie", position, yaw);
                if (baby) {
                    actor.watch(BABY_FLAG, (byte) 1);
                }
            }

            boolean step(Vec winner) {
                age++;
                if (age <= RISE_TICKS) {
                    rise();
                    return false;
                }
                if (charge >= 0) {
                    return charging();
                }
                return walk(winner);
            }

            void remove() {
                actor.remove();
            }

            private void rise() {
                position = new Vec(position.x(), rising(groundY, age), position.z());
                actor.teleport(position, yaw, 0);
                Vec ground = new Vec(position.x(), groundY, position.z());
                stage.particle("BLOCK_CRACK", ground, 6, new Vec(0.3, 0.1, 0.3), 0.1, GROUND_BLOCK);
                stage.particle("SMOKE_LARGE", ground, 2, new Vec(0.3, 0.1, 0.3), 0.02);
                stage.particle("SPELL_WITCH", ground.add(0, 0.5, 0), 2, new Vec(0.3, 0.6, 0.3), 0);
            }

            private boolean walk(Vec winner) {
                double distance = flat(position, winner);
                if (distance <= REACHED) {
                    vanish();
                    return true;
                }
                if (fuseAge >= 0 && age >= fuseAge && distance >= MIN_FUSE_DISTANCE) {
                    charge = 0;
                    stage.sound("mob.creeper.say", position, 0.4f, (float) Kit.between(random, 0.83, 0.98));
                    return false;
                }
                Vec next = advance(position, winner, speed(baby));
                OptionalDouble ground = floor(stage, next.x(), position.y(), next.z());
                if (ground.isEmpty()) {
                    vanish();
                    return true;
                }
                double step = Math.max(-CLIMB, Math.min(CLIMB, ground.getAsDouble() - position.y()));
                groundY = ground.getAsDouble();
                position = new Vec(next.x(), position.y() + step, next.z());
                actor.teleport(position, position.yawTowards(winner), 0);
                if (age % WITCH_EVERY == 0) {
                    stage.particle("SPELL_WITCH", position.add(0, WITCH_HEIGHT, 0), 1, new Vec(0.2, 0.2, 0.2), 0);
                }
                if (age == talkAge) {
                    stage.sound("mob.zombie.say", position, 0.5f, (float) Kit.between(random, 0.86, 0.98));
                }
                return false;
            }

            private boolean charging() {
                charge++;
                Vec chest = position.add(0, 1.0, 0);
                stage.particle("SPELL_WITCH", chest, 4, new Vec(0.3, 0.5, 0.3), 0);
                if (charge % 4 == 0) {
                    stage.particle("SMOKE_LARGE", chest, 3, new Vec(0.2, 0.4, 0.2), 0.02);
                }
                if (charge < CHARGE_TICKS) {
                    return false;
                }
                stage.particle("EXPLOSION_LARGE", chest, 1, Vec.ZERO, 1.0);
                stage.particle("SMOKE_LARGE", chest, 3, new Vec(0.2, 0.4, 0.2), 0.02);
                stage.sound("random.explode", position, 0.6f, (float) Kit.between(random, 1.0, 1.19));
                actor.remove();
                return true;
            }

            private void vanish() {
                Vec chest = position.add(0, 1.0, 0);
                stage.particle("SMOKE_LARGE", chest, 20, new Vec(0.3, 0.8, 0.3), 0.02);
                stage.particle("SPELL_WITCH", chest, 10, new Vec(0.3, 0.8, 0.3), 0.02);
                stage.sound("random.fizz", position, 0.15f, (float) Kit.between(random, 0.73, 0.86));
                actor.remove();
            }
        }
    }
}
