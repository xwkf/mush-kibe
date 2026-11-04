package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

final class BroomFlight {

    static final double TOP_SPEED = 1.0;
    static final double EASING = 0.3;
    static final double TURN_EASING = 0.35;
    static final double MAX_CLIMB = 1.0;
    static final Vec START_VELOCITY = new Vec(0, 0.1, 0);

    private Vec position;
    private Vec velocity = START_VELOCITY;
    private double yaw;

    BroomFlight(Vec position, float yaw) {
        this.position = position;
        this.yaw = yaw;
    }

    void step(boolean pushing, float lookYaw, float lookPitch) {
        yaw += TURN_EASING * Flight.wrap(lookYaw - yaw);
        velocity = velocity.add(target(pushing, yaw, lookPitch).subtract(velocity).multiply(EASING));
        position = position.add(velocity);
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

    static Vec target(boolean pushing, double yaw, double pitch) {
        if (!pushing) {
            return Vec.ZERO;
        }
        return forward(yaw).multiply(TOP_SPEED).add(0, climb(pitch), 0);
    }

    static double climb(double pitch) {
        return Math.clamp(-Math.tan(Math.toRadians(pitch)) * TOP_SPEED, -MAX_CLIMB, MAX_CLIMB);
    }

    static Vec forward(double yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec(-Math.sin(radians), 0, Math.cos(radians));
    }

    static Vec right(double yaw) {
        Vec forward = forward(yaw);
        return new Vec(-forward.z(), 0, forward.x());
    }
}
