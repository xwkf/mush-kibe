package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class BalloonsVictory implements SceneAnimation {

    static final String[] SKINS = {
        "52dd11da04252f76b6934bc26612f54f264f30eed74df89941209e191bebc0a2",
        "76387fc246893d92a6dd9ea1b52dcd581e991eeee2e263b27fff1bcf1b154eb7",
        "4f85522ee815d110587fffc74113f419d929598e2463b8ce9d39caa9fb6ff5ab",
        "eef162def845aa3dc7d46cd08a7bf95bbdfd32d381215aa41bffad5224298728",
        "a26ec7cd3b6ae249997137c1b94867c66e97499da071bf50adfd37034132fa03",
        "7a2df315b43583b1896231b77bae1a507dbd7e43ad86c1cfbe3b2b8ef3430e9e",
        "f868e6a5c4a445d60a3050b5bec1d37af1b25943745d2d479800c8436488065a",
        "f052be1c06a4a325129d6f41bb84f0ea1ca6f9f69ebdfff4316e742451c79c21",
        "cb1ae7a471729651b5667b81694e492808c5090c2b168f0a9190fd002ee50a26"
    };
    static final StandStyle BALLOON_STAND = new StandStyle(true, false, false, false, false);
    static final int LENGTH = 200;
    static final int HAND_BALLOONS = 25;
    static final int LOOSE_BALLOONS = 30;
    static final int LAUNCH_EVERY = 10;
    static final double SEAT_SPEED = 0.25;
    static final int SLOW_START_TICKS = 15;
    static final double SLOW_START_LIFT = 0.016;
    static final double LIFT_BASE = 0.05;
    static final double LIFT_WAVE = 0.07;
    static final double LIFT_PERIOD = 42;
    static final double LIFT_PHASE = 30;
    static final double ANGULAR_SPEED = 0.05;
    static final double SWAY_DEGREES = 25;
    static final double WOBBLE = 0.5;
    static final double CIRCLE_RADIUS = 0.25;
    static final double CLIMB = 0.05;
    static final double TAIL_CLIMB = 0.0505;
    static final int BURST_TICKS = 20;
    static final double BURST_SPEED = 0.31;
    static final double CHASE_ACCELERATION = 0.01;
    static final double CHASE_EASE = 0.12;
    static final double CHASE_LIMIT = 0.3;
    static final double HAND_MIN_X = -2.4;
    static final double HAND_MAX_X = 2.75;
    static final double HAND_MIN_Y = -3.97;
    static final double HAND_MAX_Y = 1.09;
    static final double HAND_MIN_Z = -2.0;
    static final double HAND_MAX_Z = 2.4;
    static final double LOOSE_RANGE = 10;
    static final double LOOSE_MIN_Y = -0.44;
    static final double LOOSE_MAX_Y = 1.5;
    static final int POP_FROM = 82;
    static final int POP_UNTIL = 120;
    static final Vec ANCHOR_OFFSET = new Vec(0.75, 1.64, -0.5);
    static final Vec TAIL_OFFSET = new Vec(0.75, 1.06, -0.5);
    static final Vec CHICKEN_OFFSET = new Vec(0, 0.44, -0.16);
    static final Vec HEAD_OFFSET = new Vec(0, 1.42, 0);

    @Override
    public String id() {
        return "altas-aventuras";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Flight flight = new Flight(scene, stage, random);
        timeline.at(0, flight::start);
        timeline.repeat(0, LENGTH - LAUNCH_EVERY, LAUNCH_EVERY, offset -> stage.sound("fireworks.launch", flight.seat, 3, 1));
        timeline.repeat(1, LENGTH, 1, offset -> flight.advance(offset + 1));
        timeline.at(LENGTH, flight::remove);
        timeline.lasts(LENGTH);
    }

    static double lift(int tick) {
        if (tick < SLOW_START_TICKS) {
            return SLOW_START_LIFT;
        }
        return LIFT_BASE + LIFT_WAVE * Math.cos(2 * Math.PI * (tick - LIFT_PHASE) / LIFT_PERIOD);
    }

    static Vec step(Vec seat, float yaw, boolean pushing, int tick) {
        double radians = Math.toRadians(yaw);
        double dx = pushing ? -Math.sin(radians) * SEAT_SPEED : 0;
        double dz = pushing ? Math.cos(radians) * SEAT_SPEED : 0;
        return seat.add(dx, lift(tick), dz);
    }

    static double angle(int tick, double phase) {
        return ANGULAR_SPEED * tick + phase;
    }

    static float sway(int tick, double phase) {
        return (float) (SWAY_DEGREES * Math.sin(angle(tick, phase)));
    }

    static Vec circle(int tick, double phase) {
        double now = 2 * angle(tick, phase);
        double start = 2 * phase;
        return new Vec(CIRCLE_RADIUS * (Math.cos(now) - Math.cos(start)), 0, CIRCLE_RADIUS * (Math.sin(now) - Math.sin(start)));
    }

    static double drift(int tick, double phase) {
        return CLIMB * tick + WOBBLE * (Math.sin(angle(tick, phase)) - Math.sin(phase));
    }

    static double rise(int tick, double phase) {
        if (tick <= BURST_TICKS) {
            return BURST_SPEED * tick;
        }
        int after = tick - BURST_TICKS;
        return BURST_SPEED * BURST_TICKS + CLIMB * after + WOBBLE * (Math.sin(angle(tick, phase)) - Math.sin(angle(BURST_TICKS, phase)));
    }

    static Vec chase(Vec velocity, Vec gap) {
        double distance = Math.hypot(gap.x(), gap.z());
        Vec wanted = distance == 0 ? Vec.ZERO : new Vec(gap.x(), 0, gap.z()).multiply(Math.min(CHASE_LIMIT, distance * CHASE_EASE) / distance);
        Vec change = wanted.subtract(velocity);
        double size = change.length();
        Vec limited = size > CHASE_ACCELERATION ? change.multiply(CHASE_ACCELERATION / size) : change;
        return velocity.add(limited);
    }

    static int popTick(RandomGenerator random) {
        return POP_FROM + random.nextInt(POP_UNTIL - POP_FROM + 1);
    }

    static Vec handSpawn(RandomGenerator random) {
        return new Vec(Kit.between(random, HAND_MIN_X, HAND_MAX_X), Kit.between(random, HAND_MIN_Y, HAND_MAX_Y), Kit.between(random, HAND_MIN_Z, HAND_MAX_Z));
    }

    static Vec looseSpawn(RandomGenerator random) {
        return new Vec(Kit.between(random, -LOOSE_RANGE, LOOSE_RANGE), Kit.between(random, LOOSE_MIN_Y, LOOSE_MAX_Y), Kit.between(random, -LOOSE_RANGE, LOOSE_RANGE));
    }

    static String skin(int index) {
        return SKINS[index % SKINS.length];
    }

    private static Actor invisibleChicken(Stage stage, Vec position) {
        Actor chicken = stage.mob("Chicken", position, 0);
        chicken.watch(0, (byte) 32);
        return chicken;
    }

    private static Actor balloonStand(Stage stage, Vec position, String skin) {
        Actor stand = stage.armorStand(position, 0, BALLOON_STAND);
        stand.equipSkull(Slot.HELMET, skin);
        return stand;
    }

    private static final class Flight {

        private final Scene scene;
        private final Stage stage;
        private final RandomGenerator random;
        private final List<HandBalloon> hands = new ArrayList<>();
        private final List<LooseBalloon> loose = new ArrayList<>();
        private Mount mount;
        private Actor anchor;
        private Vec seat;

        Flight(Scene scene, Stage stage, RandomGenerator random) {
            this.scene = scene;
            this.stage = stage;
            this.random = random;
            this.seat = scene.origin();
        }

        void start() {
            seat = scene.origin();
            mount = stage.mount(seat, Vec.ZERO.yawTowards(scene.facing()), scene.riders());
            anchor = invisibleChicken(stage, seat.add(ANCHOR_OFFSET));
            for (int index = 0; index < HAND_BALLOONS; index++) {
                hands.add(new HandBalloon(stage, random, seat, anchor, index));
            }
            for (int index = 0; index < LOOSE_BALLOONS; index++) {
                loose.add(new LooseBalloon(stage, random, seat));
            }
        }

        void advance(int tick) {
            seat = step(seat, mount.lookYaw(), mount.pushing(), tick);
            mount.move(seat, mount.lookYaw());
            anchor.teleport(seat.add(ANCHOR_OFFSET), 0, 0);
            for (HandBalloon balloon : hands) {
                balloon.advance(tick, seat);
            }
            for (LooseBalloon balloon : loose) {
                balloon.advance(tick);
            }
        }

        void remove() {
            anchor.remove();
            hands.forEach(HandBalloon::remove);
            loose.forEach(LooseBalloon::remove);
        }
    }

    private static final class HandBalloon {

        private final Stage stage;
        private final Actor stand;
        private final Actor chicken;
        private final int index;
        private final double phase;
        private final double spawnHeight;
        private final Vec home;
        private Vec base;
        private Vec velocity = Vec.ZERO;

        HandBalloon(Stage stage, RandomGenerator random, Vec seat, Actor anchor, int index) {
            this.stage = stage;
            this.index = index;
            this.phase = random.nextDouble() * 2 * Math.PI;
            Vec spawn = handSpawn(random);
            this.home = new Vec(spawn.x(), 0, spawn.z());
            this.spawnHeight = seat.y() + spawn.y();
            this.base = new Vec(seat.x() + spawn.x(), 0, seat.z() + spawn.z());
            Vec position = new Vec(base.x(), spawnHeight, base.z());
            this.stand = balloonStand(stage, position, skin(index));
            this.chicken = invisibleChicken(stage, position.add(CHICKEN_OFFSET));
            chicken.leash(anchor);
        }

        void advance(int tick, Vec seat) {
            Vec target = new Vec(seat.x() + home.x(), 0, seat.z() + home.z());
            velocity = chase(velocity, target.subtract(base));
            base = base.add(velocity);
            Vec sway = circle(tick, phase);
            Vec position = new Vec(base.x() + sway.x(), spawnHeight + rise(tick, phase), base.z() + sway.z());
            stand.teleport(position, sway(tick, phase), 0);
            chicken.teleport(position.add(CHICKEN_OFFSET), 0, 0);
            stage.particle(index % 2 == 0 ? "CRIT" : "CRIT_MAGIC", position.add(HEAD_OFFSET), 1, Vec.ZERO, 0);
        }

        void remove() {
            stand.remove();
            chicken.remove();
        }
    }

    private static final class LooseBalloon {

        private final Stage stage;
        private final Actor stand;
        private final Actor chicken;
        private final Actor tail;
        private final double phase;
        private final int pop;
        private final Vec spawn;
        private final Vec tailSpawn;
        private boolean popped;

        LooseBalloon(Stage stage, RandomGenerator random, Vec seat) {
            this.stage = stage;
            this.phase = random.nextDouble() * 2 * Math.PI;
            this.pop = popTick(random);
            this.spawn = seat.add(looseSpawn(random));
            this.tailSpawn = spawn.add(TAIL_OFFSET);
            this.stand = balloonStand(stage, spawn, skin(random.nextInt(SKINS.length)));
            this.chicken = invisibleChicken(stage, spawn.add(CHICKEN_OFFSET));
            this.tail = invisibleChicken(stage, tailSpawn);
            chicken.leash(tail);
        }

        void advance(int tick) {
            if (popped) {
                return;
            }
            Vec sway = circle(tick, phase);
            Vec position = new Vec(spawn.x() + sway.x(), spawn.y() + drift(tick, phase), spawn.z() + sway.z());
            if (tick >= pop) {
                popped = true;
                stage.sound("mob.chicken.plop", position.add(CHICKEN_OFFSET), 1, 1);
                stage.particle("CRIT", position.add(HEAD_OFFSET), 1, Vec.ZERO, 0);
                remove();
                return;
            }
            stand.teleport(position, sway(tick, phase), 0);
            chicken.teleport(position.add(CHICKEN_OFFSET), 0, 0);
            tail.teleport(tailSpawn.add(0, TAIL_CLIMB * tick, 0), 0, 0);
        }

        void remove() {
            stand.remove();
            chicken.remove();
            tail.remove();
        }
    }
}
