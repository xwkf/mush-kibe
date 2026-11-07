package br.com.muxe.minigame.api.cosmetic.aura;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

public final class AuraPattern {

    public static final double STEP_DEGREES = 18;
    public static final int TICKS_PER_TURN = 20;
    public static final int PERIOD_TICKS = 60;
    public static final int PHASE_TICKS = 15;
    public static final double CLIMB_DIVISOR = 30;
    public static final double BASE_HEIGHT = 0.1;
    public static final double MOVED_SQUARED = 1.0E-6;

    private AuraPattern() {
    }

    public static Vec helix(long counter) {
        double angle = Math.toRadians(STEP_DEGREES * Math.floorMod(counter, (long) TICKS_PER_TURN));
        double climb = Math.floorMod(counter + PHASE_TICKS, (long) PERIOD_TICKS) / CLIMB_DIVISOR;
        return new Vec(Math.sin(angle), BASE_HEIGHT + climb, -Math.cos(angle));
    }

    public static Vec feet(Vec position) {
        return position.add(0, BASE_HEIGHT, 0);
    }

    public static boolean moved(Vec before, Vec now) {
        double dx = now.x() - before.x();
        double dz = now.z() - before.z();
        return dx * dx + dz * dz > MOVED_SQUARED;
    }
}
