package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

final class Flight {

    static final double TOP_SPEED = 1.0;
    static final double EASING = 0.3;
    static final double BOB_AMPLITUDE = 0.1;
    static final double BOB_RATE = 0.2;
    static final double TURN_EASING = 0.2;
    static final Vec START_VELOCITY = new Vec(0, 0.1, 0);

    private Vec position;
    private Vec velocity = START_VELOCITY;
    private double yaw;
    private int ticks;

    Flight(Vec position, float yaw) {
        this.position = position;
        this.yaw = yaw;
    }

    void step(boolean pushing, float lookYaw, float lookPitch) {
        velocity = ease(velocity, target(pushing, lookYaw, lookPitch, ticks));
        position = position.add(velocity);
        yaw = easeYaw(yaw, lookYaw);
        ticks++;
    }

    Vec position() {
        return position;
    }

    Vec velocity() {
        return velocity;
    }

    float yaw() {
        return (float) yaw;
    }

    int ticks() {
        return ticks;
    }

    static Vec direction(double yaw, double pitch) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        return new Vec(-Math.sin(yawRadians) * Math.cos(pitchRadians), -Math.sin(pitchRadians), Math.cos(yawRadians) * Math.cos(pitchRadians));
    }

    static double bob(int tick) {
        return BOB_AMPLITUDE * Math.cos(BOB_RATE * (tick + 1));
    }

    static Vec target(boolean pushing, double yaw, double pitch, int tick) {
        Vec heading = pushing ? direction(yaw, pitch).multiply(TOP_SPEED) : Vec.ZERO;
        return heading.add(0, bob(tick), 0);
    }

    static Vec ease(Vec velocity, Vec target) {
        return velocity.add(target.subtract(velocity).multiply(EASING));
    }

    static double easeYaw(double yaw, double target) {
        return yaw + TURN_EASING * wrap(target - yaw);
    }

    static double wrap(double degrees) {
        double wrapped = degrees % 360;
        if (wrapped >= 180) {
            wrapped -= 360;
        } else if (wrapped < -180) {
            wrapped += 360;
        }
        return wrapped;
    }
}
