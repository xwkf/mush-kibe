package br.com.muxe.minigame.bedwars.game;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

public final class ExplosionKnockback {

    public static final int TNT_FUSE_TICKS = 60;
    public static final float TNT_YIELD = 3.0f;
    public static final float FIREBALL_YIELD = 2.0f;
    public static final double FIREBALL_ACCELERATION = 0.1;
    static final double GROUNDED_LIFT = 0.7;
    static final double AIRBORNE_LIFT = 1.0;
    private static final double TINY = 1e-6;

    public enum Source {
        FIREBALL(3.125, false),
        TNT(1.875, true);

        private final double strength;
        private final boolean flat;

        Source(double strength, boolean flat) {
            this.strength = strength;
            this.flat = flat;
        }
    }

    private ExplosionKnockback() {
    }

    public static Vec of(Source source, Vec explosion, Vec feet, boolean onGround) {
        double lift = onGround ? GROUNDED_LIFT : AIRBORNE_LIFT;
        double dx = feet.x() - explosion.x();
        double dy = source.flat ? 0 : feet.y() - explosion.y();
        double dz = feet.z() - explosion.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < TINY) {
            return new Vec(0, lift, 0);
        }
        return new Vec(dx / length * source.strength, lift, dz / length * source.strength);
    }
}
