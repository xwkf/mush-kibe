package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class MagicalGirlVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int ROCKET_SLOT = 4;
    static final String ROCKET_NAME = "§eFoguete";
    static final int SEATED = 1;
    static final double SEAT_ABOVE_GROUND = 0.65;
    static final double STAR_BELOW_SEAT = 1.78;
    static final int COOLDOWN = 5;
    static final float ROCKET_VOLUME = 2;
    static final double SPARKS_BELOW_SEAT = 0.34;
    static final int SPARKS = 15;
    static final Vec SPARKS_SPREAD = new Vec(0.3, 0.1, 0.3);
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final int STAR_FIRST = 2;
    static final int STAR_EVERY = 8;
    static final int STAR_LAST = 194;
    static final double STAR_LOWEST = 3.7;
    static final double STAR_HIGHEST = 9.5;
    static final int CHIME_LOWEST = 96;
    static final int CHIME_HIGHEST = 125;

    @Override
    public String id() {
        return "garota-magica";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<Ride> rides = Riders.of(scene, stage).stream().map(Ride::new).toList();
        Ride leader = rides.getFirst();
        Show show = new Show(stage, random, rides);
        for (UUID rider : scene.riders()) {
            stage.give(rider, ROCKET_SLOT, Material.FIREWORK, ROCKET_NAME, click -> show.launch(rider));
        }
        timeline.at(0, () -> stage.sound("mob.enderdragon.growl", scene.origin(), 3, 1));
        timeline.at(SEATED, () -> rides.forEach(ride -> ride.start(stage)));
        timeline.repeat(0, LENGTH - 1, 1, show::tick);
        timeline.repeat(LAUNCH_FIRST, LENGTH - 1, LAUNCH_EVERY, tick -> stage.sound("fireworks.launch", leader.flight.position(), 3, 1));
        timeline.repeat(STAR_FIRST - 1, STAR_LAST - 1, STAR_EVERY, offset -> {
            for (int copy = 0; copy < scene.area().copies(); copy++) {
                show.announce(timeline, scene.area(), leader.flight.position(), STAR_FIRST + offset);
            }
        });
        timeline.at(LENGTH, show::finish);
    }

    static boolean ready(Optional<Integer> lastLaunch, int tick) {
        return lastLaunch.map(last -> tick - last >= COOLDOWN).orElse(true);
    }

    static float chime(int recorded) {
        return SoundPitch.of(recorded);
    }

    private static final class Ride {

        private final Riders.Rider rider;
        private final RocketFlight flight;
        private Mount mount;
        private Actor star;
        private double yaw;

        Ride(Riders.Rider rider) {
            this.rider = rider;
            this.flight = new RocketFlight(rider.ground().add(0, SEAT_ABOVE_GROUND, 0), SEAT_ABOVE_GROUND);
            this.yaw = rider.yaw();
        }

        void start(Stage stage) {
            mount = stage.mount(flight.position(), rider.yaw(), rider.seated());
            star = stage.armorStand(standAt(), rider.yaw(), ShootingStar.STAND);
            star.equipSkull(Slot.HELMET, MagicalGirlFinalKill.STAR);
        }

        void fly(Stage stage) {
            flight.step(stage::solid);
            float look = mount.lookYaw();
            yaw = Flight.easeYaw(yaw, look);
            mount.move(flight.position(), look);
            star.teleport(standAt(), (float) yaw, 0);
        }

        void launch(Stage stage) {
            flight.launch(mount.lookYaw());
            Vec seat = flight.position();
            stage.sound("fireworks.launch", seat, ROCKET_VOLUME, 1);
            stage.particle("FIREWORKS_SPARK", seat.add(0, -SPARKS_BELOW_SEAT, 0), SPARKS, SPARKS_SPREAD, 1);
        }

        Vec standAt() {
            return flight.position().add(0, -STAR_BELOW_SEAT, 0);
        }

        void remove() {
            if (star != null) {
                star.remove();
            }
        }
    }

    private static final class Show {

        private final Stage stage;
        private final RandomGenerator random;
        private final List<Ride> rides;
        private final List<ShootingStar> stars = new ArrayList<>();
        private final Map<UUID, Integer> lastLaunch = new ConcurrentHashMap<>();
        private int now;
        private boolean over;

        Show(Stage stage, RandomGenerator random, List<Ride> rides) {
            this.stage = stage;
            this.random = random;
            this.rides = rides;
        }

        void launch(UUID player) {
            Optional<Ride> ride = rides.stream().filter(candidate -> candidate.rider.seated().contains(player)).findFirst();
            if (over || ride.isEmpty() || ride.get().mount == null || !ready(Optional.ofNullable(lastLaunch.get(player)), now)) {
                return;
            }
            lastLaunch.put(player, now);
            ride.get().launch(stage);
        }

        void tick(int tick) {
            now = tick;
            if (tick > SEATED) {
                rides.forEach(ride -> ride.fly(stage));
            }
            stars.removeIf(star -> star.step(stage, random));
        }

        void announce(Timeline timeline, Area area, Vec leader, int fallsAt) {
            Vec spot = area.pick(random, leader.y() + Kit.between(random, STAR_LOWEST, STAR_HIGHEST));
            double degrees = random.nextDouble() * 360;
            stage.sound("note.pling", spot.add(0, ShootingStar.CHIME, 0), 0.5f, chime(random.nextInt(CHIME_LOWEST, CHIME_HIGHEST + 1)));
            timeline.at(fallsAt, () -> {
                if (over) {
                    return;
                }
                ShootingStar star = ShootingStar.fall(stage, spot, degrees);
                if (!star.step(stage, random)) {
                    stars.add(star);
                }
            });
        }

        void finish() {
            over = true;
            stars.forEach(ShootingStar::remove);
            stars.clear();
            rides.forEach(Ride::remove);
        }
    }
}
