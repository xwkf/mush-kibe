package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
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

public final class FlyingHyenaVictory implements SceneAnimation {

    static final String HYENA = "ebd46b38b21b342caf917ad9ca42afb68388a5591bcc9aded1e8e346e18890";
    static final List<String> GIFTS = List.of(
        "103416095268d93265c4cfc219415335fcaa72a51bb1ebca3cefc5cb302e1941",
        "28893da6c340975c37c9b990d3dc8a8944c5a62cb1034ace3add66980a59344",
        "143e0f73d8f447a573f33226fe4f9683b64dda42e7142c130b5b33c29f160183",
        "5b85e29f29ec9a90e482ea5b8391bcb4560bbae0dcd15d7ce1d86016b4356e98",
        "e00afa9898346fbaca43bbf93ddc13db64d23202b30de479457ca883871aa86c",
        "2b1ec7dc753061ca174424ea45cf9490b39cd5dcca477d138a603e6be755ec72",
        "69b564a6f73283112a70b9ce7e15753eb86bd12e7659ec4d0dc0855c6bea76e",
        "626b199c9cce33d96c6730de34c2ad9ec75eefad6fafe06b6c636ab53cdd2fb0",
        "ee3a8fd0852977444d9fd7797cac07b8d3948addc43f0bb5ce25ae72d95dc"
    );
    static final int[] FIREWORK_COLORS = {0xB5251A, 0x2C7F28, 0xFFFFFF};
    static final StandStyle UNSEEN = new StandStyle(true, false, false, false, false);
    static final double SEAT_ABOVE_GROUND = 1.375;
    static final double HYENA_BELOW_SEAT = 1.8125;
    static final int PRESENTS_PER_BATCH = 6;
    static final int FIRST_BATCH = 1;
    static final int LAST_BATCH = 186;
    static final int BATCH_EVERY = 5;
    static final double PRESENT_MIN_HEIGHT = 8;
    static final double PRESENT_MAX_HEIGHT = 13.5;
    static final double FALL_SPEED = 1.5;
    static final double FALL_ACCELERATION = 0.04;
    static final double REST_BELOW_TOP = 0.4375;
    static final int GROUND_TICKS = 20;
    static final int LAST_FIREWORK = 190;
    static final int FIREWORK_EVERY = 10;
    static final double FIREWORK_RING = 15;
    static final double FIREWORK_BELOW_SEAT = 0.35;
    static final int LENGTH = 200;

    @Override
    public String id() {
        return "feliz-natal";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<Ride> rides = Riders.of(scene, stage).stream().map(rider -> new Ride(rider, new Flight(rider.ground().add(0, SEAT_ABOVE_GROUND, 0), rider.yaw()))).toList();
        Ride leader = rides.getFirst();
        List<Present> presents = new ArrayList<>();
        int[] spawned = {0};
        timeline.at(0, () -> rides.forEach(ride -> ride.start(stage)));
        timeline.repeat(1, LENGTH - 1, 1, tick -> rides.forEach(ride -> fly(ride.flight, ride.mount, ride.hyena)));
        timeline.repeat(FIRST_BATCH, LAST_BATCH, BATCH_EVERY, batch -> {
            for (int count = 0; count < PRESENTS_PER_BATCH * scene.area().copies(); count++) {
                presents.add(present(stage, random, scene.area(), leader.mount.seat(), skin(spawned[0]++)));
            }
        });
        timeline.repeat(0, LENGTH - 1, 1, tick -> presents.removeIf(present -> present.step(stage)));
        timeline.repeat(0, LAST_FIREWORK, FIREWORK_EVERY, index -> firework(stage, timeline, random, leader.mount.seat(), index * FIREWORK_EVERY, FIREWORK_COLORS[index % FIREWORK_COLORS.length]));
        timeline.at(LENGTH, () -> {
            presents.forEach(Present::remove);
            presents.clear();
            rides.forEach(ride -> ride.hyena.remove());
        });
    }

    private static final class Ride {

        private final Riders.Rider rider;
        private final Flight flight;
        private Mount mount;
        private Actor hyena;

        Ride(Riders.Rider rider, Flight flight) {
            this.rider = rider;
            this.flight = flight;
        }

        void start(Stage stage) {
            Vec seat = flight.position();
            mount = stage.mount(seat, rider.yaw(), rider.seated());
            hyena = stage.armorStand(seat.add(0, -HYENA_BELOW_SEAT, 0), rider.yaw(), UNSEEN);
            hyena.equipSkull(Slot.HELMET, HYENA);
        }
    }

    static String skin(int counter) {
        return GIFTS.get(counter % GIFTS.size());
    }

    static double fallen(int ticks) {
        return FALL_SPEED * ticks + FALL_ACCELERATION / 2 * ticks * (ticks + 1);
    }

    static Vec presentSpot(Area area, Vec seat, RandomGenerator random) {
        return area.pick(random, seat.y() + Kit.between(random, PRESENT_MIN_HEIGHT, PRESENT_MAX_HEIGHT));
    }

    static Vec fireworkSpot(Vec seat, RandomGenerator random) {
        return seat.add(Kit.horizontal(random.nextDouble() * 360, FIREWORK_RING)).add(0, -FIREWORK_BELOW_SEAT, 0);
    }

    private static void fly(Flight flight, Mount mount, Actor hyena) {
        float lookYaw = mount.lookYaw();
        flight.step(mount.pushing(), lookYaw, mount.lookPitch());
        mount.move(flight.position(), lookYaw);
        hyena.teleport(flight.position().add(0, -HYENA_BELOW_SEAT, 0), flight.yaw(), 0);
    }

    private static Present present(Stage stage, RandomGenerator random, Area area, Vec seat, String texture) {
        Vec spot = presentSpot(area, seat, random);
        Actor box = stage.armorStand(spot, 0, UNSEEN);
        box.equipSkull(Slot.HELMET, texture);
        return new Present(box, spot);
    }

    private static void firework(Stage stage, Timeline timeline, RandomGenerator random, Vec seat, int tick, int color) {
        Vec spot = fireworkSpot(seat, random);
        Actor rocket = stage.firework(spot, List.of(Kit.burst(Kit.STAR, false, true, color)));
        stage.sound("fireworks.launch", seat, 3, 1);
        rocket.velocity(new Vec(0, 0.05, 0));
        timeline.at(tick + 1, () -> rocket.status(17));
        timeline.at(tick + 2, rocket::remove);
    }

    static final class Present {

        private final Actor actor;
        private final double x;
        private final double z;
        private final double startY;
        private double y;
        private int age;
        private int grounded = -1;

        Present(Actor actor, Vec start) {
            this.actor = actor;
            this.x = start.x();
            this.z = start.z();
            this.startY = start.y();
            this.y = start.y();
        }

        boolean step(Stage stage) {
            if (grounded >= 0) {
                grounded++;
                if (grounded >= GROUND_TICKS) {
                    actor.remove();
                    return true;
                }
                return false;
            }
            age++;
            double next = startY - fallen(age);
            double top = Ground.surface(stage::solid, x, y, z);
            double rest = top - REST_BELOW_TOP;
            if (top != y && rest <= y && rest >= next) {
                y = rest;
                grounded = 0;
            } else {
                y = next;
            }
            actor.teleport(new Vec(x, y, z), 0, 0);
            return false;
        }

        void remove() {
            actor.remove();
        }
    }
}
