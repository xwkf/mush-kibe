package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
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

public final class WitchVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final double SEAT_ABOVE_GROUND = 0.75;
    static final int WAND_SLOT = 4;
    static final String WAND_NAME = "§dVarinha Mágica";
    static final int COOLDOWN = 13;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;

    @Override
    public String id() {
        return "bruxa";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<Ride> rides = Riders.of(scene, stage).stream().map(Ride::new).toList();
        Ride leader = rides.getFirst();
        Coven coven = new Coven(stage, random, rides);
        for (UUID rider : scene.riders()) {
            stage.give(rider, WAND_SLOT, Material.STICK, WAND_NAME, click -> coven.cast(rider));
        }
        timeline.at(0, () -> {
            rides.forEach(ride -> ride.start(stage));
            stage.sound("mob.enderdragon.growl", leader.flight.position(), 3, 1);
        });
        timeline.repeat(1, LENGTH - 1, 1, coven::tick);
        timeline.repeat(LAUNCH_FIRST, LENGTH - 1, LAUNCH_EVERY, tick -> stage.sound("fireworks.launch", leader.flight.position(), 3, 1));
        timeline.at(LENGTH, coven::finish);
        timeline.lasts(LENGTH);
    }

    static boolean ready(Optional<Integer> lastCast, int tick) {
        return lastCast.map(last -> tick - last >= COOLDOWN).orElse(true);
    }

    static Vec aim(Stage stage, Vec eyes, Vec look) {
        return GrapplerVictory.trace(stage, eyes, look).orElse(eyes.add(look.normalize().multiply(GrapplerVictory.RANGE)));
    }

    private static final class Ride {

        private final Riders.Rider rider;
        private final BroomFlight flight;
        private final List<Companion> companions = new ArrayList<>();
        private Mount mount;
        private Broom broom;

        Ride(Riders.Rider rider) {
            this.rider = rider;
            this.flight = new BroomFlight(rider.ground().add(0, SEAT_ABOVE_GROUND, 0), rider.yaw());
        }

        void start(Stage stage) {
            Vec seat = flight.position();
            mount = stage.mount(seat, flight.yaw(), rider.seated());
            broom = Broom.build(stage, seat, flight.yaw());
            for (int index = 0; index < Companion.PLACES.size(); index++) {
                companions.add(Companion.spawn(stage, Companion.place(seat, flight.yaw(), Companion.PLACES.get(index)), flight.yaw(), WitchSkins.of(index)));
            }
        }

        void fly() {
            flight.step(mount.pushing(), mount.lookYaw(), mount.lookPitch());
            mount.move(flight.position(), flight.yaw());
            broom.place(flight.position(), flight.yaw());
            for (int index = 0; index < companions.size(); index++) {
                companions.get(index).follow(Companion.place(flight.position(), flight.yaw(), Companion.PLACES.get(index)));
            }
        }

        void remove() {
            broom.remove();
            companions.forEach(Companion::remove);
        }
    }

    private static final class Coven {

        private final Stage stage;
        private final RandomGenerator random;
        private final List<Ride> rides;
        private final Map<UUID, Integer> lastCast = new ConcurrentHashMap<>();
        private final List<Spell> spells = new ArrayList<>();
        private int now;
        private boolean over;

        Coven(Stage stage, RandomGenerator random, List<Ride> rides) {
            this.stage = stage;
            this.random = random;
            this.rides = rides;
        }

        void cast(UUID player) {
            Optional<Ride> ride = rides.stream().filter(candidate -> candidate.rider.seated().contains(player)).findFirst();
            if (over || ride.isEmpty() || ride.get().mount == null || !ready(Optional.ofNullable(lastCast.get(player)), now)) {
                return;
            }
            Optional<Vec> eyes = stage.eyes(player);
            Optional<Vec> look = stage.look(player);
            if (eyes.isEmpty() || look.isEmpty() || look.get().length() == 0) {
                return;
            }
            Ride caster = ride.get();
            Vec hit = aim(stage, eyes.get(), look.get());
            lastCast.put(player, now);
            int color = Hex.COLORS.get(random.nextInt(Hex.COLORS.size()));
            spells.add(new Spell(caster, Hex.cast(stage, caster.flight.position(), caster.flight.yaw(), hit, now, color)));
        }

        void tick(int offset) {
            now = offset + 1;
            rides.forEach(Ride::fly);
            spells.removeIf(spell -> spell.hex().step(stage, spell.ride().flight.position(), spell.ride().flight.yaw(), now));
        }

        void finish() {
            over = true;
            spells.forEach(spell -> spell.hex().fade());
            spells.clear();
            rides.forEach(Ride::remove);
        }
    }

    private record Spell(Ride ride, Hex hex) {
    }
}
