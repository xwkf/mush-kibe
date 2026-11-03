package br.com.muxe.core.bukkit.cosmetic.animation;

import java.util.random.RandomGenerator;

public record Area(double minX, double minZ, double maxX, double maxZ) {

    public static final int MAX_COPIES = 2;
    static final double REFERENCE_SIZE = 32 * 32;

    public Area {
        if (maxX < minX || maxZ < minZ) {
            throw new IllegalArgumentException("An area goes from its smaller corner to its bigger one, got " + minX + "," + minZ + " to " + maxX + "," + maxZ);
        }
    }

    public static Area around(Vec center, double radius) {
        return new Area(center.x() - radius, center.z() - radius, center.x() + radius, center.z() + radius);
    }

    public Vec pick(RandomGenerator random, double y) {
        return new Vec(minX + random.nextDouble() * (maxX - minX), y, minZ + random.nextDouble() * (maxZ - minZ));
    }

    public double size() {
        return (maxX - minX) * (maxZ - minZ);
    }

    public int copies() {
        return (int) Math.max(1, Math.min(MAX_COPIES, Math.round(size() / REFERENCE_SIZE)));
    }
}
