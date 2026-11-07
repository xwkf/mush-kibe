package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class DuskVictory implements SceneAnimation {

    static final double OWL_ABOVE_GROUND = 1.5625;
    static final double SEAT_BELOW_OWL = 0.0625;
    static final double SEAT_BEHIND = 3.1;
    static final double SEAT_ABOVE_OWL = 1.15;
    static final int SETTLE = 15;
    static final double CRUISE = 0.73;
    static final double CLIMB = 0.41;
    static final double EASING = 0.12;
    static final double REIN_ABOVE = 1.4375;
    static final byte INVISIBLE = 32;
    static final int FEATHER_FROM = 5;
    static final int FEATHER_LAG = 3;
    static final double FEATHER_HEIGHT = 1.5;
    static final int FEATHERS_PER_TICK = 2;
    static final double FEATHER_SPREAD = 0.3;
    static final int WINGS_FIRST = 8;
    static final int WINGS_EVERY = 8;
    static final float WINGS_VOLUME = 0.15f;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final int LAUNCH_LAST = 191;
    static final float THUNDER_VOLUME = 0.6f;
    static final float SCREECH_VOLUME = 0.4f;
    static final int LENGTH = 200;

    @Override
    public String id() {
        return "anoitecer";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<Ride> rides = Riders.of(scene, stage).stream().map(Ride::new).toList();
        Ride leader = rides.getFirst();
        Nightfall.schedule(stage, timeline);
        timeline.at(0, () -> {
            rides.forEach(ride -> ride.start(stage, random));
            stage.sound("mob.enderdragon.growl", leader.seatAt(0), 3, 1);
        });
        timeline.repeat(1, LENGTH - 1, 1, tick -> rides.forEach(ride -> ride.fly(tick + 1)));
        timeline.repeat(FEATHER_FROM, LENGTH - 1, 1, offset -> rides.forEach(ride -> ride.feathers(stage, random, FEATHER_FROM + offset)));
        timeline.repeat(WINGS_FIRST, LENGTH, WINGS_EVERY, offset -> rides.forEach(ride ->
            stage.sound("mob.enderdragon.wings", ride.owlAt(WINGS_FIRST + offset), WINGS_VOLUME, SoundPitch.of(113))));
        timeline.repeat(LAUNCH_FIRST, LAUNCH_LAST, LAUNCH_EVERY, offset -> stage.sound("fireworks.launch", leader.seatAt(LAUNCH_FIRST + offset), 3, 1));
        timeline.at(LENGTH, () -> rides.forEach(Ride::remove));
    }

    static double flown(int tick) {
        double kept = 1 - EASING;
        return CRUISE * (tick - kept * (1 - Math.pow(kept, tick)) / EASING);
    }

    static Vec owl(Vec start, Vec heading, int tick) {
        return start.add(heading.multiply(flown(tick))).add(0, CLIMB * tick, 0);
    }

    static Vec seat(Vec start, Vec heading, int tick) {
        double settled = Math.min(1, (double) tick / SETTLE);
        double above = -SEAT_BELOW_OWL + (SEAT_ABOVE_OWL + SEAT_BELOW_OWL) * settled;
        return owl(start, heading, tick).add(heading.multiply(-SEAT_BEHIND * settled)).add(0, above, 0);
    }

    private static final class Ride {

        private final Riders.Rider rider;
        private final Vec start;
        private final Vec heading;
        private final float yaw;
        private Mount mount;
        private Actor owl;
        private Optional<Actor> rein = Optional.empty();
        private OwlFlock flock;

        Ride(Riders.Rider rider) {
            this.rider = rider;
            this.start = rider.ground().add(0, OWL_ABOVE_GROUND, 0);
            this.heading = rider.facing();
            this.yaw = rider.yaw();
        }

        Vec owlAt(int tick) {
            return DuskVictory.owl(start, heading, tick);
        }

        Vec seatAt(int tick) {
            return DuskVictory.seat(start, heading, tick);
        }

        void start(Stage stage, RandomGenerator random) {
            mount = stage.mount(seatAt(0), yaw, rider.seated());
            owl = stage.armorStand(start, yaw, DuskAnimation.OWL_STAND);
            owl.equipSkull(Slot.HELMET, DuskAnimation.OWL);
            if (!rider.seated().isEmpty()) {
                Actor chicken = stage.mob("Chicken", start.add(0, REIN_ABOVE, 0), yaw);
                chicken.watch(0, INVISIBLE);
                stage.leash(chicken, rider.seated().getFirst());
                rein = Optional.of(chicken);
            }
            Vec cry = start.add(0, OwlFlock.PUFF_ABOVE, 0);
            OwlFlock.puff(stage, start);
            stage.sound("ambient.weather.thunder", rider.ground(), THUNDER_VOLUME, 1);
            stage.sound("mob.bat.idle", cry, SCREECH_VOLUME, SoundPitch.of(40));
            flock = new OwlFlock(stage, random);
        }

        void fly(int tick) {
            Vec at = owlAt(tick);
            mount.move(seatAt(tick), yaw);
            owl.teleport(at, yaw, 0);
            rein.ifPresent(chicken -> chicken.teleport(at.add(0, REIN_ABOVE, 0), yaw, 0));
            flock.step(at, tick);
        }

        void feathers(Stage stage, RandomGenerator random, int tick) {
            Vec behind = owlAt(Math.max(0, tick - FEATHER_LAG)).add(0, FEATHER_HEIGHT, 0);
            for (int feather = 0; feather < FEATHERS_PER_TICK; feather++) {
                double[] color = DuskAnimation.FEATHERS[random.nextInt(DuskAnimation.FEATHERS.length)];
                Vec at = behind.add(Kit.between(random, -FEATHER_SPREAD, FEATHER_SPREAD), Kit.between(random, -FEATHER_SPREAD, FEATHER_SPREAD),
                    Kit.between(random, -FEATHER_SPREAD, FEATHER_SPREAD));
                Kit.dust(stage, at, color[0], color[1], color[2]);
            }
        }

        void remove() {
            owl.remove();
            rein.ifPresent(Actor::remove);
            flock.remove();
        }
    }
}
