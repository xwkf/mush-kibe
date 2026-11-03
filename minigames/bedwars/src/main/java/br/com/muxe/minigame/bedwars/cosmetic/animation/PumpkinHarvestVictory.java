package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Burst;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class PumpkinHarvestVictory implements SceneAnimation {

    static final StandStyle BIG_STAND = new StandStyle(true, false, false, false, false);
    static final StandStyle SMALL_STAND = new StandStyle(true, true, false, false, false);
    static final Burst ORANGE_BALL = new Burst(Kit.BALL, List.of(0xFF7A00, 0xFFA733), List.of(0xFFE14D), false, false);
    static final int LENGTH = 200;
    static final int PUMPKINS = 20;
    static final int FIRST_SPAWN = 2;
    static final int SPAWN_EVERY = 10;
    static final int LIFETIME = 120;
    static final int NO_FLOOR_LIFE = 99;
    static final double MIN_AHEAD = 2;
    static final double MAX_AHEAD = 13;
    static final double MIN_SIDE = -5.5;
    static final double MAX_SIDE = 7.2;
    static final double MIN_HEIGHT = 12.9;
    static final double MAX_HEIGHT = 16.5;
    static final double FALL_START = 0.625;
    static final double FALL_STEP = 0.056;
    static final double SPIN_DEGREES = 18.28;
    static final double HEAD_BELOW_FLOOR = 1.375;
    static final double HEAD_CENTER = 1.95;
    static final double TRAIL_SPACING = 0.205;
    static final int LAUNCH_SPARKS = 3;
    static final double LAUNCH_SPARK_HEIGHT = 2.75;
    static final double LAUNCH_SPARK_SPEED = 1.67;
    static final double LIT_CHANCE = 0.3;
    static final double SMALL_LIT_CHANCE = 0.46;
    static final int BURST_DELAY = 2;
    static final double BURST_HEIGHT = 1.88;
    static final int ITEMS = 3;
    static final int ITEM_LIFE = 40;
    static final double ITEM_SPEED = 0.15;
    static final double ITEM_MIN_LIFT = 0.30;
    static final double ITEM_MAX_LIFT = 0.44;
    static final double FIREWORK_CHANCE = 0.35;
    static final int SMALLS = 3;
    static final double SMALL_START_HEIGHT = 0.66;
    static final double SMALL_MIN_SPEED = 0.06;
    static final double SMALL_MAX_SPEED = 0.22;
    static final double SMALL_MIN_LIFT = 0.5;
    static final double SMALL_MAX_LIFT = 0.8;
    static final double SMALL_GRAVITY = 0.09;
    static final double SMALL_SPIN_DEGREES = 23.9;
    static final double SMALL_BELOW_FLOOR = 0.72;
    static final double SPARK_HEIGHT = 0.89;
    static final int SMALL_VOID_LIFE = 79;
    static final int SMALL_BREAK_MIN = 2;
    static final int SMALL_BREAK_MAX = 3;

    @Override
    public String id() {
        return "colheita-de-aboboras";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        List<Harvest> harvests = new ArrayList<>();
        timeline.repeat(1, LENGTH - 9, SPAWN_EVERY, offset -> stage.sound("fireworks.launch", origin, 3, 1));
        for (int index = 0; index < PUMPKINS; index++) {
            int start = FIRST_SPAWN + index * SPAWN_EVERY;
            if (start >= LENGTH) {
                break;
            }
            Vec spot = spot(origin, scene.facing(), scene.right(), random);
            Vec spawn = new Vec(spot.x(), origin.y() + Kit.between(random, MIN_HEIGHT, MAX_HEIGHT), spot.z());
            Harvest harvest = new Harvest(stage, random, spawn, floorAt(stage, spot.x(), origin.y(), spot.z()));
            harvests.add(harvest);
            timeline.repeat(start, Math.min(LENGTH, start + LIFETIME), 1, harvest::tick);
        }
        timeline.at(LENGTH, () -> harvests.forEach(Harvest::remove));
        timeline.lasts(LENGTH);
    }

    static Vec spot(Vec origin, Vec forward, Vec right, RandomGenerator random) {
        return origin.add(forward.multiply(Kit.between(random, MIN_AHEAD, MAX_AHEAD))).add(right.multiply(Kit.between(random, MIN_SIDE, MAX_SIDE)));
    }

    static OptionalDouble floorAt(Stage stage, double x, double near, double z) {
        double top = Ground.surface(stage::solid, x, near, z);
        boolean found = stage.solid((int) Math.floor(x), (int) Math.floor(top) - 1, (int) Math.floor(z));
        return found ? OptionalDouble.of(top) : OptionalDouble.empty();
    }

    static double fallen(int steps) {
        return FALL_START * steps + FALL_STEP * steps * (steps - 1) / 2;
    }

    static double rest(double floor) {
        return floor - HEAD_BELOW_FLOOR;
    }

    static float spin(int steps) {
        return (float) (-SPIN_DEGREES * steps % 360);
    }

    static List<Vec> trail(Vec from, Vec to) {
        List<Vec> points = new ArrayList<>();
        Vec span = to.subtract(from);
        double length = span.length();
        if (length == 0) {
            return points;
        }
        Vec direction = span.multiply(1 / length);
        for (double along = TRAIL_SPACING; along <= length; along += TRAIL_SPACING) {
            points.add(from.add(direction.multiply(along)));
        }
        return points;
    }

    static Vec itemVelocity(RandomGenerator random) {
        Vec side = Kit.horizontal(random.nextDouble() * 360, ITEM_SPEED);
        return new Vec(side.x(), Kit.between(random, ITEM_MIN_LIFT, ITEM_MAX_LIFT), side.z());
    }

    static Vec smallVelocity(RandomGenerator random) {
        Vec side = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, SMALL_MIN_SPEED, SMALL_MAX_SPEED));
        return new Vec(side.x(), Kit.between(random, SMALL_MIN_LIFT, SMALL_MAX_LIFT), side.z());
    }

    private static Material pumpkin(RandomGenerator random, double litChance) {
        return random.nextDouble() < litChance ? Material.JACK_O_LANTERN : Material.PUMPKIN;
    }

    private static final class Harvest {

        private final Stage stage;
        private final RandomGenerator random;
        private final Vec spawn;
        private final OptionalDouble floor;
        private final List<Item> items = new ArrayList<>();
        private final List<Small> smalls = new ArrayList<>();
        private Actor big;
        private Actor rocket;
        private Vec head;
        private int landedAge = -1;
        private int burstAge = -1;

        Harvest(Stage stage, RandomGenerator random, Vec spawn, OptionalDouble floor) {
            this.stage = stage;
            this.random = random;
            this.spawn = spawn;
            this.floor = floor;
            this.head = spawn.add(0, HEAD_CENTER, 0);
        }

        void tick(int age) {
            if (age == 0) {
                launch();
                return;
            }
            fall(age);
            if (burstAge >= 0 && age == burstAge + 1) {
                scatter(age);
            }
            items.forEach(item -> item.advance(age));
            smalls.forEach(small -> small.advance(age));
            if (rocket != null && !rocket.removed()) {
                fireRocket(age);
            }
        }

        void remove() {
            if (big != null) {
                big.remove();
            }
            if (rocket != null) {
                rocket.remove();
            }
            items.forEach(Item::remove);
            smalls.forEach(Small::remove);
        }

        private void launch() {
            big = stage.armorStand(spawn, 0, BIG_STAND);
            big.equip(Slot.HELMET, pumpkin(random, LIT_CHANCE), 0);
            stage.sound("fireworks.launch", spawn.add(0, 1.4, 0), 1.5f, (float) Kit.between(random, 0.51, 0.68));
        }

        private void fall(int age) {
            if (big.removed()) {
                return;
            }
            double rest = floor.isPresent() ? rest(floor.getAsDouble()) : Double.NEGATIVE_INFINITY;
            double height = Math.max(spawn.y() - fallen(age), rest);
            Vec position = new Vec(spawn.x(), height, spawn.z());
            big.teleport(position, spin(age), 0);
            if (age <= LAUNCH_SPARKS) {
                stage.particle("FLAME", spawn.add(0, LAUNCH_SPARK_HEIGHT + LAUNCH_SPARK_SPEED * (age - 1), 0), 1, new Vec(0.1, 0.1, 0.1), 0);
            }
            Vec next = position.add(0, HEAD_CENTER, 0);
            List<Vec> points = trail(head, next);
            for (int index = 0; index < points.size(); index++) {
                if (index % 2 == 0) {
                    stage.particle("FLAME", points.get(index), 1, new Vec(0.1, 0.1, 0.1), 0);
                } else {
                    stage.particle("SMOKE_NORMAL", points.get(index), 1, Vec.ZERO, 0);
                }
            }
            head = next;
            if (floor.isEmpty()) {
                if (age >= NO_FLOOR_LIFE) {
                    big.remove();
                }
                return;
            }
            if (landedAge < 0 && height <= rest) {
                landedAge = age;
            }
            if (landedAge >= 0 && age == landedAge + BURST_DELAY) {
                burst(age, new Vec(spawn.x(), rest + BURST_HEIGHT, spawn.z()));
            }
        }

        private void burst(int age, Vec point) {
            big.remove();
            burstAge = age;
            stage.sound("dig.wood", point, 1.5f, (float) Kit.between(random, 0.70, 0.75));
            stage.particle("BLOCK_DUST", point, 20, new Vec(0.4, 0.3, 0.4), 0.1, Kit.blockId(Material.PUMPKIN, 0));
            stage.particle("FLAME", point, 8, Vec.ZERO, 0.08);
            for (int index = 0; index < ITEMS; index++) {
                Material kind = random.nextBoolean() ? Material.PUMPKIN_SEEDS : Material.PUMPKIN_PIE;
                items.add(new Item(stage, stage.item(point, kind, 0), Body.item(point, itemVelocity(random)), age + ITEM_LIFE));
            }
            if (random.nextDouble() < FIREWORK_CHANCE) {
                rocket = stage.firework(point, List.of(ORANGE_BALL));
                rocket.velocity(new Vec(0, 0.05, 0));
                stage.sound("fireworks.launch", point, 3, 1);
            }
        }

        private void scatter(int age) {
            double ground = floor.getAsDouble();
            Vec start = new Vec(spawn.x(), rest(ground) + SMALL_START_HEIGHT, spawn.z());
            for (int index = 0; index < SMALLS; index++) {
                Actor stand = stage.armorStand(start, 0, SMALL_STAND);
                stand.equip(Slot.HELMET, pumpkin(random, SMALL_LIT_CHANCE), 0);
                int delay = SMALL_BREAK_MIN + random.nextInt(SMALL_BREAK_MAX - SMALL_BREAK_MIN + 1);
                smalls.add(new Small(stage, random, stand, start, smallVelocity(random), age, ground, delay));
            }
        }

        private void fireRocket(int age) {
            if (age == burstAge + 1) {
                rocket.status(17);
            } else if (age >= burstAge + 2) {
                rocket.remove();
            }
        }
    }

    private static final class Item {

        private final Stage stage;
        private final Actor actor;
        private final Body body;
        private final int until;

        Item(Stage stage, Actor actor, Body body, int until) {
            this.stage = stage;
            this.actor = actor;
            this.body = body;
            this.until = until;
        }

        void advance(int age) {
            if (actor.removed()) {
                return;
            }
            if (age >= until) {
                actor.remove();
                return;
            }
            body.step(stage);
            actor.teleport(body.position(), 0, 0);
        }

        void remove() {
            actor.remove();
        }
    }

    private static final class Small {

        private final Stage stage;
        private final RandomGenerator random;
        private final Actor stand;
        private final int spawnAge;
        private final double burstFloor;
        private final int breakDelay;
        private Vec position;
        private Vec velocity;
        private double landedFloor;
        private int landedAge = -1;

        Small(Stage stage, RandomGenerator random, Actor stand, Vec position, Vec velocity, int spawnAge, double burstFloor, int breakDelay) {
            this.stage = stage;
            this.random = random;
            this.stand = stand;
            this.position = position;
            this.velocity = velocity;
            this.spawnAge = spawnAge;
            this.burstFloor = burstFloor;
            this.breakDelay = breakDelay;
        }

        void advance(int age) {
            if (stand.removed()) {
                return;
            }
            int steps = age - spawnAge;
            if (landedAge >= 0) {
                if (age >= landedAge + breakDelay) {
                    shatter();
                }
                return;
            }
            if (steps >= SMALL_VOID_LIFE) {
                stand.remove();
                return;
            }
            position = position.add(velocity);
            velocity = velocity.add(0, -SMALL_GRAVITY, 0);
            if (velocity.y() < 0) {
                OptionalDouble ground = floorAt(stage, position.x(), burstFloor, position.z());
                if (ground.isPresent() && position.y() <= ground.getAsDouble() - SMALL_BELOW_FLOOR) {
                    landedAge = age;
                    landedFloor = ground.getAsDouble();
                    position = new Vec(position.x(), landedFloor - SMALL_BELOW_FLOOR, position.z());
                }
            }
            stand.teleport(position, (float) (-SMALL_SPIN_DEGREES * steps % 360), 0);
            if (landedAge < 0) {
                stage.particle("FIREWORKS_SPARK", position.add(0, SPARK_HEIGHT, 0), 1, Vec.ZERO, 0);
            }
        }

        void remove() {
            stand.remove();
        }

        private void shatter() {
            Vec point = new Vec(position.x(), landedFloor + 1, position.z());
            stage.sound("mob.chicken.plop", point, 0.8f, (float) Kit.between(random, 0.81, 1.35));
            stage.sound("dig.wood", point, 0.6f, (float) Kit.between(random, 1.22, 1.59));
            stage.particle("BLOCK_DUST", point, 8, new Vec(0.2, 0.1, 0.2), 0.1, Kit.blockId(Material.PUMPKIN, 0));
            stand.remove();
        }
    }
}
