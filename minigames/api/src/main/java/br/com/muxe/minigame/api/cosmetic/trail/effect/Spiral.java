package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

public record Spiral(double radius, double start, double step) {

    private static final double STILL = 1.0E-6;
    private static final Vec UP = new Vec(0, 1, 0);
    private static final Vec FALLBACK_SIDE = new Vec(1, 0, 0);

    public Vec offset(Vec velocity, int age) {
        Vec heading = new Vec(-velocity.x(), -velocity.y(), velocity.z()).normalize();
        if (heading.length() < STILL) {
            heading = UP;
        }
        Vec side = cross(heading, UP);
        side = side.length() < STILL ? FALLBACK_SIDE : side.normalize();
        Vec up = cross(side, heading).normalize();
        double angle = start - age * step;
        return side.multiply(Math.cos(angle) * radius).add(up.multiply(Math.sin(angle) * radius));
    }

    private static Vec cross(Vec a, Vec b) {
        return new Vec(a.y() * b.z() - a.z() * b.y(), a.z() * b.x() - a.x() * b.z(), a.x() * b.y() - a.y() * b.x());
    }
}
