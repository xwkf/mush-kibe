package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

record DragonFlight(Vec spawn, float yaw, float climb) {

    static final double SPEED = 0.114;
    static final int STEPS = 35;
    static final double HEAD_ABOVE_STAND = 1.5;
    static final int PUFFS = 4;
    static final int TRAIL_LAG = 2;

    Vec step() {
        double yawRadians = Math.toRadians(yaw);
        double climbRadians = Math.toRadians(climb);
        double flat = Math.cos(climbRadians) * SPEED;
        return new Vec(-Math.sin(yawRadians) * flat, Math.sin(climbRadians) * SPEED, Math.cos(yawRadians) * flat);
    }

    Vec stand(int tick) {
        return at(tick + 1);
    }

    Vec trail(int tick) {
        if (tick < PUFFS) {
            return at(1).add(0, HEAD_ABOVE_STAND * (tick + 1), 0);
        }
        return at(tick - TRAIL_LAG).add(0, HEAD_ABOVE_STAND, 0);
    }

    private Vec at(int steps) {
        return spawn.add(step().multiply(Math.clamp(steps, 0, STEPS)));
    }
}
