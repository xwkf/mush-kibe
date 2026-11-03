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
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class NecromancerVictory implements SceneAnimation {

    static final long NIGHT = 18000;
    static final int END = 200;
    static final int SKULLS = 3;
    static final StandStyle SKULL_STYLE = new StandStyle(true, true, false, false, true);
    static final StandStyle LABEL_STYLE = new StandStyle(true, false, false, false, true);
    static final int WITHER_SKULL = 1;
    static final String GREY = "§7";
    static final double SKULL_RADIUS = 1.3;
    static final double SKULL_TURN = -7.2;
    static final double SKULL_HEIGHT = 0.735;
    static final double SKULL_BOB = 0.145;
    static final double SKULL_BOB_PERIOD = 42.5;
    static final double SKULL_BOB_PHASE = 28.5;
    static final int RING_POINTS = 16;
    static final double RING_RADIUS = 3;
    static final double RING_HEIGHT = 0.1;
    static final double RING_TURN = 5.7;
    static final double[] RING_COLOR = {0.54, 0.17, 0.89};
    static final int TRIO = 3;
    static final double TRIO_RADIUS = 1.8;
    static final double TRIO_HEIGHT = 0.2;
    static final double TRIO_TURN = -11.4;
    static final int EMIT_EVERY = 2;
    static final int PORTAL_EVERY = 60;
    static final int INTRO_BOLTS = 3;
    static final int[] INTRO_TICKS = {1, 5, 9};
    static final double[][] INTRO_OFFSETS = {{-3.5, -1.9}, {3.2, 2.4}, {-3.25, 2.3}};
    static final int DEAD = 10;
    static final List<String> NAMES = List.of(
        "Ana", "Bruno", "Carla", "Diego", "Elisa", "Felipe", "Gabriela", "Heitor", "Iris", "Joao",
        "Lara", "Marcos", "Nina", "Otavio", "Paula", "Rafael", "Sofia", "Tiago", "Vitor", "Yasmin"
    );
    static final double MIN_DISTANCE = 5;
    static final double MAX_DISTANCE = 11;
    static final double BURIED = 2;
    static final double RISE_STEP = 0.25;
    static final int RISE_TICKS = 8;
    static final double SPEED = 0.2;
    static final double CLIMB = 0.3;
    static final double SINK_DISTANCE = 1.1;
    static final double SINK_DEPTH = 6;
    static final int SUNK_TICKS = 6;
    static final int MAX_WALK = 120;
    static final double NAME_HEIGHT = 2.0;
    static final double BOLT_CHANCE = 0.12;
    static final double UNFECT_CHANCE = 0.3;
    static final double TALK_CHANCE = 0.25;
    static final double WITHER_CHANCE = 0.05;
    static final double WINNER_EYES = 1.62;
    static final int GROUND_BLOCK = Kit.blockId(Material.STONE, 0);

    @Override
    public String id() {
        return "necromante";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Show show = new Show(scene, stage, random, names(random));
        timeline.at(0, () -> stage.time(NIGHT));
        timeline.repeat(0, END, 1, show::tick);
        timeline.lasts(END);
    }

    static List<String> names(RandomGenerator random) {
        List<String> pool = new ArrayList<>(NAMES);
        Collections.shuffle(pool, random);
        return List.copyOf(pool.subList(0, DEAD));
    }

    static Vec skull(Vec winner, int tick, int index) {
        double degrees = SKULL_TURN * tick + index * (360.0 / SKULLS);
        double height = SKULL_HEIGHT + SKULL_BOB * Math.sin(2 * Math.PI * (tick - SKULL_BOB_PHASE) / SKULL_BOB_PERIOD);
        Vec around = Kit.horizontal(degrees, SKULL_RADIUS);
        return winner.add(around.x(), height, around.z());
    }

    static float skullYaw(int tick, int index) {
        return (float) (SKULL_TURN * tick + index * (360.0 / SKULLS));
    }

    static Vec ring(Vec winner, int emission, int index) {
        Vec around = Kit.horizontal(RING_TURN * emission + index * (360.0 / RING_POINTS), RING_RADIUS);
        return winner.add(around.x(), RING_HEIGHT, around.z());
    }

    static Vec trio(Vec winner, int emission, int index) {
        Vec around = Kit.horizontal(TRIO_TURN * emission + index * (360.0 / TRIO), TRIO_RADIUS);
        return winner.add(around.x(), TRIO_HEIGHT, around.z());
    }

    static Vec emergence(Vec winner, double degrees, double distance) {
        Vec away = Kit.horizontal(degrees, distance);
        return new Vec(Math.floor(winner.x() + away.x()) + 0.5, 0, Math.floor(winner.z() + away.z()) + 0.5);
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
        private final List<String> names;
        private final Vec origin;
        private final Actor[] skulls = new Actor[SKULLS];
        private final List<Risen> dead = new ArrayList<>();

        Show(Scene scene, Stage stage, RandomGenerator random, List<String> names) {
            this.scene = scene;
            this.stage = stage;
            this.random = random;
            this.names = names;
            this.origin = scene.origin();
        }

        void tick(int tick) {
            if (tick >= END) {
                clear();
                return;
            }
            Vec winner = winner();
            if (tick == 0) {
                stage.sound("ambient.weather.thunder", winner, 0.6f, 1);
            }
            for (int bolt = 0; bolt < INTRO_BOLTS; bolt++) {
                if (tick == INTRO_TICKS[bolt]) {
                    Vec spot = origin.add(INTRO_OFFSETS[bolt][0], 0, INTRO_OFFSETS[bolt][1]);
                    stage.lightning(new Vec(spot.x(), floor(stage, spot.x(), spot.y(), spot.z()).orElse(spot.y()), spot.z()));
                }
            }
            orbit(winner, tick);
            if (tick % EMIT_EVERY == 0) {
                emit(winner, tick / EMIT_EVERY);
            }
            if (tick % PORTAL_EVERY == 0) {
                stage.sound("portal.portal", winner, 0.15f, 0.79f);
            }
            if (tick < DEAD) {
                dead.add(new Risen(names.get(tick), winner));
            }
            for (Risen risen : dead) {
                risen.step(winner);
            }
        }

        private Vec winner() {
            if (scene.riders().isEmpty()) {
                return origin;
            }
            return stage.eyes(scene.riders().getFirst()).map(eyes -> eyes.add(0, -WINNER_EYES, 0)).orElse(origin);
        }

        private void orbit(Vec winner, int tick) {
            for (int index = 0; index < SKULLS; index++) {
                Vec at = skull(winner, tick, index);
                if (skulls[index] == null) {
                    skulls[index] = stage.armorStand(at, skullYaw(tick, index), SKULL_STYLE);
                    skulls[index].equip(Slot.HELMET, Material.SKULL_ITEM, WITHER_SKULL);
                } else {
                    skulls[index].teleport(at, skullYaw(tick, index), 0);
                }
            }
        }

        private void emit(Vec winner, int emission) {
            for (int index = 0; index < RING_POINTS; index++) {
                Kit.dust(stage, ring(winner, emission, index), RING_COLOR[0], RING_COLOR[1], RING_COLOR[2]);
            }
            for (int index = 0; index < TRIO; index++) {
                stage.particle("SPELL_WITCH", trio(winner, emission, index), 1, Vec.ZERO, 0);
            }
        }

        private void clear() {
            for (Actor skull : skulls) {
                if (skull != null) {
                    skull.remove();
                }
            }
            for (Risen risen : dead) {
                risen.remove();
            }
            dead.clear();
        }

        private final class Risen {

            private final Actor body;
            private final Actor label;
            private Vec position;
            private double groundY;
            private float yaw;
            private boolean rising;
            private boolean walking;
            private int age;
            private int sunk;

            Risen(String name, Vec winner) {
                this.position = origin.add(0, -3, 0);
                this.body = stage.player(position, 0, name);
                this.label = stage.armorStand(position.add(0, NAME_HEIGHT, 0), 0, LABEL_STYLE);
                this.label.watch(2, GREY + name);
                this.label.watch(3, (byte) 0);
                this.sunk = SUNK_TICKS;
                emerge(winner);
            }

            void step(Vec winner) {
                if (rising) {
                    rise();
                } else if (walking) {
                    walk(winner);
                } else {
                    sunk++;
                    if (sunk >= SUNK_TICKS) {
                        emerge(winner);
                    }
                }
            }

            void remove() {
                body.remove();
                label.remove();
            }

            private void emerge(Vec winner) {
                Vec spot = emergence(winner, random.nextDouble() * 360, Kit.between(random, MIN_DISTANCE, MAX_DISTANCE));
                OptionalDouble ground = floor(stage, spot.x(), winner.y(), spot.z());
                if (ground.isEmpty()) {
                    return;
                }
                groundY = ground.getAsDouble();
                Vec point = new Vec(spot.x(), groundY, spot.z());
                stage.sound("dig.gravel", point, 0.6f, 0.59f);
                stage.sound("mob.endermen.portal", point, 0.25f, (float) Kit.between(random, 0.52, 0.65));
                if (random.nextDouble() < UNFECT_CHANCE) {
                    stage.sound("mob.zombie.unfect", point, 0.3f, (float) Kit.between(random, 0.81, 0.9));
                }
                if (random.nextDouble() < BOLT_CHANCE) {
                    stage.lightning(point);
                }
                age = 0;
                yaw = point.yawTowards(winner);
                position = new Vec(point.x(), rising(groundY, 0), point.z());
                label.watch(3, (byte) 1);
                place();
                rising = true;
            }

            private void rise() {
                age++;
                position = new Vec(position.x(), rising(groundY, age), position.z());
                place();
                Vec ground = new Vec(position.x(), groundY, position.z());
                stage.particle("BLOCK_CRACK", ground, 3, new Vec(0.3, 0.1, 0.3), 0.1, GROUND_BLOCK);
                stage.particle("CLOUD", ground, 1, new Vec(0.3, 0.1, 0.3), 0.02);
                if (age >= RISE_TICKS) {
                    rising = false;
                    walking = true;
                    age = 0;
                }
            }

            private void walk(Vec winner) {
                age++;
                if (flat(position, winner) <= SINK_DISTANCE || age > MAX_WALK) {
                    sink();
                    return;
                }
                Vec next = advance(position, winner, SPEED);
                OptionalDouble ground = floor(stage, next.x(), position.y(), next.z());
                if (ground.isEmpty()) {
                    sink();
                    return;
                }
                double step = Math.max(-CLIMB, Math.min(CLIMB, ground.getAsDouble() - position.y()));
                groundY = ground.getAsDouble();
                position = new Vec(next.x(), position.y() + step, next.z());
                yaw = position.yawTowards(winner);
                place();
            }

            private void sink() {
                Vec chest = position.add(0, 1.0, 0);
                stage.particle("CLOUD", chest, 8, new Vec(0.3, 0.8, 0.3), 0.02);
                stage.sound("random.fizz", position, 0.15f, (float) Kit.between(random, 0.7, 0.8));
                stage.sound("mob.ghast.moan", position, 0.15f, (float) Kit.between(random, 1.55, 1.8));
                if (random.nextDouble() < WITHER_CHANCE) {
                    stage.sound("mob.wither.idle", position, 0.15f, 1.74f);
                }
                if (random.nextDouble() < TALK_CHANCE) {
                    stage.sound("mob.zombie.say", position, 0.25f, (float) Kit.between(random, 0.9, 1.06));
                }
                position = position.add(0, -SINK_DEPTH, 0);
                label.watch(3, (byte) 0);
                place();
                walking = false;
                sunk = 0;
            }

            private void place() {
                body.teleport(position, yaw, 0);
                label.teleport(position.add(0, NAME_HEIGHT, 0), 0, 0);
            }
        }
    }
}
