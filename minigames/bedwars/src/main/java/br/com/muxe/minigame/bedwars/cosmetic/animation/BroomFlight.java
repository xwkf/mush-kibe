package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

final class BroomFlight {

    static final double TOP_SPEED = 1.0;
    static final double EASING = 0.3;
    static final double TURN_EASING = 0.2;
    static final double BOB_STEP = 0.1;
    static final double BOB_RATE = 0.2;

    private Vec position;
    private Vec velocity = Vec.ZERO;
    private double yaw;
    private int ticks;

    BroomFlight(Vec position, float yaw) {
        this.position = position;
        this.yaw = yaw;
    }

    void step(boolean pushing, float lookYaw, float lookPitch) {
        Vec target = pushing ? Flight.direction(lookYaw, lookPitch).multiply(TOP_SPEED) : Vec.ZERO;
        velocity = velocity.add(target.subtract(velocity).multiply(EASING));
        position = position.add(velocity).add(0, bob(ticks), 0);
        yaw += TURN_EASING * Flight.wrap(lookYaw - yaw);
        ticks++;
    }

    void hover() {
        step(false, (float) yaw, 0);
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

    static double bob(int tick) {
        return BOB_STEP * Math.cos(BOB_RATE * tick);
    }
}
