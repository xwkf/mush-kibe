package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class DragonRide {

    static final String DRAGON = "EnderDragon";
    static final double ABOVE_GROUND = 5;
    static final double SEAT_ABOVE_DRAGON = 6;
    static final double SPEED = 1;
    static final float LEVEL_PITCH = 23;
    static final float STRAFE_TURN = 90;
    static final float BACKWARDS = 180;
    static final double LOWEST = 1;
    static final double HIGHEST = 240;
    static final int GROWL_PAUSE = 80;
    static final int GROWL_ODDS = 1000;
    static final float GROWL_VOLUME = 5;
    static final double GROWL_PITCH_SPREAD = 0.2;

    private final Riders.Rider rider;
    private Vec position;
    private Mount mount;
    private Actor dragon;
    private int growling;

    DragonRide(Riders.Rider rider) {
        this.rider = rider;
        this.position = rider.ground().add(0, ABOVE_GROUND, 0);
    }

    void start(Stage stage) {
        mount = stage.mount(seat(position), rider.yaw(), rider.seated());
        dragon = stage.mob(DRAGON, position, body(rider.yaw()));
    }

    void fly() {
        float lookYaw = mount.lookYaw();
        float heading = heading(lookYaw, mount.strafing());
        float climb = climb(mount.lookPitch());
        position = step(position, heading, climb);
        mount.move(seat(position), lookYaw);
        dragon.teleport(position, body(heading), climb);
    }

    void growl(Stage stage, RandomGenerator random) {
        if (random.nextInt(GROWL_ODDS) < growling++) {
            growling = -GROWL_PAUSE;
            float pitch = (float) ((random.nextDouble() - random.nextDouble()) * GROWL_PITCH_SPREAD + 1);
            stage.sound("mob.enderdragon.growl", position, GROWL_VOLUME, pitch);
        }
    }

    Vec position() {
        return position;
    }

    void remove() {
        dragon.remove();
    }

    static float heading(float lookYaw, float strafing) {
        return lookYaw - STRAFE_TURN * strafing;
    }

    static float climb(float lookPitch) {
        return Math.abs(lookPitch) > LEVEL_PITCH ? lookPitch : 0;
    }

    static Vec step(Vec from, float heading, float climb) {
        Vec next = from.add(Flight.direction(heading, climb).multiply(SPEED));
        return new Vec(next.x(), Math.clamp(next.y(), LOWEST, HIGHEST), next.z());
    }

    static float body(float heading) {
        return heading + BACKWARDS;
    }

    static Vec seat(Vec dragon) {
        return dragon.add(0, SEAT_ABOVE_DRAGON, 0);
    }
}
