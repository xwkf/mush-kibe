package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class WitherVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final double SPEED = 0.5;
    static final double LEVEL_PITCH = 5;
    static final double SEAT_ABOVE = 3.375;
    static final double SEAT_BEHIND = 0.475;
    static final int SHOOT_FIRST = 20;
    static final int SHOOT_EVERY = 20;
    static final int IDLE_FIRST = 80;
    static final int IDLE_EVERY = 80;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final float EXPLOSION_VOLUME = 10;

    @Override
    public String id() {
        return "wither";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        List<Ride> rides = Riders.of(scene, stage).stream().map(Ride::new).toList();
        Ride leader = rides.getFirst();
        Barrage barrage = new Barrage(stage, new Blight(stage, random));
        timeline.at(0, () -> rides.forEach(ride -> ride.start(stage)));
        timeline.repeat(0, LENGTH - 1, 1, tick -> {
            rides.forEach(Ride::fly);
            barrage.step();
        });
        timeline.repeat(SHOOT_FIRST, LENGTH - 1, SHOOT_EVERY, tick -> rides.forEach(ride -> stage.sound("mob.wither.shoot", ride.position, 1, 1)));
        timeline.repeat(SHOOT_FIRST + 1, LENGTH - 1, SHOOT_EVERY, tick -> rides.forEach(ride -> barrage.fire(ride)));
        timeline.repeat(IDLE_FIRST, LENGTH - 1, IDLE_EVERY, tick -> rides.forEach(ride -> stage.sound("mob.wither.idle", ride.position, 1, 1)));
        timeline.repeat(LAUNCH_FIRST, LENGTH - 1, LAUNCH_EVERY, tick -> stage.sound("fireworks.launch", leader.mount.seat(), 3, 1));
        timeline.at(LENGTH, () -> {
            barrage.clear();
            rides.forEach(Ride::remove);
        });
        timeline.lasts(LENGTH);
    }

    static Vec velocity(float yaw, float pitch) {
        Vec heading = Flight.direction(yaw, pitch).multiply(SPEED);
        return Math.abs(pitch) <= LEVEL_PITCH ? new Vec(heading.x(), 0, heading.z()) : heading;
    }

    static Vec seat(Vec wither, float yaw) {
        return wither.add(Flight.direction(yaw, 0).multiply(-SEAT_BEHIND)).add(0, SEAT_ABOVE, 0);
    }

    private static final class Ride {

        private final Riders.Rider rider;
        private Vec position;
        private float yaw;
        private float pitch;
        private Mount mount;
        private Actor wither;

        Ride(Riders.Rider rider) {
            this.rider = rider;
            this.position = rider.ground();
            this.yaw = rider.yaw();
        }

        void start(Stage stage) {
            wither = stage.mob("WitherBoss", position, yaw);
            mount = stage.mount(seat(position, yaw), yaw, rider.seated());
        }

        void fly() {
            yaw = mount.lookYaw();
            pitch = mount.lookPitch();
            position = position.add(velocity(yaw, pitch));
            wither.teleport(position, yaw, 0);
            mount.move(seat(position, yaw), yaw);
        }

        void remove() {
            wither.remove();
        }
    }

    private static final class Barrage {

        private final Stage stage;
        private final Blight blight;
        private final List<WitherSkull> skulls = new ArrayList<>();

        Barrage(Stage stage, Blight blight) {
            this.stage = stage;
            this.blight = blight;
        }

        void fire(Ride ride) {
            WitherSkull skull = WitherSkull.launch(stage, ride.position, ride.yaw, ride.pitch);
            skulls.add(skull);
            fly(skull);
        }

        void step() {
            List.copyOf(skulls).forEach(this::fly);
            blight.step();
        }

        void clear() {
            skulls.forEach(WitherSkull::remove);
            skulls.clear();
        }

        private void fly(WitherSkull skull) {
            skull.fly(stage).ifPresent(point -> {
                stage.particle("EXPLOSION_HUGE", point, 1, Vec.ZERO, 1);
                stage.sound("random.explode", point, EXPLOSION_VOLUME, 1);
                skull.remove();
                skulls.remove(skull);
                blight.spread(point);
            });
        }
    }
}
