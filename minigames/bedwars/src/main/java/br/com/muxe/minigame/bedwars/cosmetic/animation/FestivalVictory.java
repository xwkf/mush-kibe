package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class FestivalVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final double SEAT_ABOVE_GROUND = 2.125;
    static final double SHEEP_BELOW_SEAT = 1;
    static final int WOOL_INDEX = 16;
    static final int[] WOOLS = {14, 1, 4, 5, 3, 11, 10};
    static final double[][] GLOWS = {{0.6, 0.2, 0.2}, {0.847, 0.498, 0.2}, {0.898, 0.898, 0.2}, {0.498, 0.8, 0.098}, {0.4, 0.6, 0.847},
        {0.2, 0.298, 0.698}, {0.498, 0.247, 0.698}};
    static final int WOOL_EVERY = 4;
    static final int GLOWS_PER_TICK = 5;
    static final double GLOW_SPREAD = 1;
    static final double GLOW_ABOVE = 0.13;
    static final float POP_PITCH = SoundPitch.of(31);
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final int SHOWER_FIRST = 1;
    static final int SHOWER_EVERY = 5;
    static final int SHOWER_LAST = 196;
    static final int SHOWER = 30;
    static final Material[] TREASURES = {Material.DOUBLE_PLANT, Material.GOLD_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND};
    static final double NEAREST = 10;
    static final double FARTHEST = 34;
    static final double WIDEST = 72;
    static final double RISE = 5.55;
    static final double RISE_PER_PITCH = 8.5;
    static final double RISE_SPREAD = 4.8;
    static final double TOSS_SIDE = 0.1;
    static final double TOSS_UP = 0.2;
    static final int SHORTEST_LIFE = 84;
    static final int LONGEST_LIFE = 100;

    @Override
    public String id() {
        return "festival-dourado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<Ride> rides = Riders.of(scene, stage).stream().map(Ride::new).toList();
        Ride leader = rides.getFirst();
        WoolTunnel tunnel = new WoolTunnel();
        Shower shower = new Shower(stage, random, perRide(rides.size()));
        timeline.at(0, () -> rides.forEach(ride -> ride.start(stage)));
        timeline.repeat(0, LENGTH - 1, 1, tick -> {
            for (Ride ride : rides) {
                if (tick > 0) {
                    ride.fly();
                }
                ride.shine(stage, random, tick);
                dig(stage, tunnel, ride.body());
            }
            shower.expire(tick);
        });
        timeline.repeat(SHOWER_FIRST, SHOWER_LAST, SHOWER_EVERY, offset -> rides.forEach(ride -> shower.pour(ride, SHOWER_FIRST + offset)));
        timeline.repeat(LAUNCH_FIRST, LENGTH - 1, LAUNCH_EVERY, tick -> stage.sound("fireworks.launch", leader.flight.position(), 3, 1));
        timeline.at(LENGTH, () -> {
            rides.forEach(Ride::remove);
            shower.clear();
        });
    }

    static int wool(int tick) {
        return WOOLS[tick / WOOL_EVERY % WOOLS.length];
    }

    static double[] glow(int tick) {
        return GLOWS[tick / WOOL_EVERY % GLOWS.length];
    }

    static int perRide(int rides) {
        return Math.max(1, SHOWER / rides);
    }

    static Material treasure(int index) {
        return TREASURES[index % TREASURES.length];
    }

    static Vec spot(Vec body, float yaw, float pitch, double distance, double degrees, double rise) {
        Vec ahead = Flight.direction(yaw + degrees, 0).multiply(distance);
        return body.add(ahead).add(0, RISE - RISE_PER_PITCH * Math.sin(Math.toRadians(pitch)) + rise, 0);
    }

    static Vec toss(RandomGenerator random) {
        return new Vec(Kit.between(random, -TOSS_SIDE, TOSS_SIDE), TOSS_UP, Kit.between(random, -TOSS_SIDE, TOSS_SIDE));
    }

    private static void dig(Stage stage, WoolTunnel tunnel, Vec body) {
        List<BlockChange> changes = tunnel.dig(stage, body);
        if (changes.isEmpty()) {
            return;
        }
        stage.blocks(changes);
        for (BlockChange change : changes) {
            stage.particle("SNOW_SHOVEL", new Vec(change.x() + 0.5, change.y() + 0.5, change.z() + 0.5), 0, Vec.ZERO, 1);
        }
        stage.sound("random.pop", body, 1, POP_PITCH);
    }

    private static final class Ride {

        private final Riders.Rider rider;
        private final Flight flight;
        private Mount mount;
        private Actor sheep;

        Ride(Riders.Rider rider) {
            this.rider = rider;
            this.flight = new Flight(rider.ground().add(0, SEAT_ABOVE_GROUND, 0), rider.yaw());
        }

        void start(Stage stage) {
            mount = stage.mount(flight.position(), rider.yaw(), rider.seated());
            sheep = stage.mob("Sheep", body(), rider.yaw());
            sheep.watch(WOOL_INDEX, (byte) wool(0));
        }

        void fly() {
            float lookYaw = mount.lookYaw();
            flight.step(mount.pushing(), lookYaw, mount.lookPitch());
            mount.move(flight.position(), lookYaw);
            sheep.teleport(body(), flight.yaw(), 0);
        }

        void shine(Stage stage, RandomGenerator random, int tick) {
            if (tick > 0 && tick % WOOL_EVERY == 0) {
                sheep.watch(WOOL_INDEX, (byte) wool(tick));
            }
            double[] color = glow(tick);
            for (int glow = 0; glow < GLOWS_PER_TICK; glow++) {
                Vec at = body().add(Kit.between(random, -GLOW_SPREAD, GLOW_SPREAD), GLOW_ABOVE, Kit.between(random, -GLOW_SPREAD, GLOW_SPREAD));
                Kit.dust(stage, at, color[0], color[1], color[2]);
            }
        }

        Vec body() {
            return flight.position().add(0, -SHEEP_BELOW_SEAT, 0);
        }

        void remove() {
            if (sheep != null) {
                sheep.remove();
            }
        }
    }

    private static final class Shower {

        private final Stage stage;
        private final RandomGenerator random;
        private final int perRide;
        private final List<Treasure> treasures = new ArrayList<>();
        private int poured;

        Shower(Stage stage, RandomGenerator random, int perRide) {
            this.stage = stage;
            this.random = random;
            this.perRide = perRide;
        }

        void pour(Ride ride, int tick) {
            for (int count = 0; count < perRide; count++) {
                double distance = Kit.between(random, NEAREST, FARTHEST);
                double degrees = (random.nextDouble() - random.nextDouble()) * WIDEST;
                Vec at = spot(ride.body(), ride.mount.lookYaw(), ride.mount.lookPitch(), distance, degrees, random.nextDouble() * RISE_SPREAD);
                Actor item = stage.item(at, treasure(poured++), 0);
                item.velocity(toss(random));
                treasures.add(new Treasure(item, tick + random.nextInt(SHORTEST_LIFE, LONGEST_LIFE + 1)));
            }
        }

        void expire(int tick) {
            treasures.removeIf(treasure -> {
                if (tick < treasure.until()) {
                    return false;
                }
                treasure.item().remove();
                return true;
            });
        }

        void clear() {
            treasures.forEach(treasure -> treasure.item().remove());
            treasures.clear();
        }
    }

    private record Treasure(Actor item, int until) {
    }
}
