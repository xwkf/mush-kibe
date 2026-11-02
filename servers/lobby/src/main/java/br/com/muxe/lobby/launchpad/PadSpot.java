package br.com.muxe.lobby.launchpad;

public record PadSpot(String world, int x, int y, int z) {

    public static final int CHUNK_SHIFT = 4;

    public int chunkX() {
        return x >> CHUNK_SHIFT;
    }

    public int chunkZ() {
        return z >> CHUNK_SHIFT;
    }

    public boolean within(String otherWorld, double otherX, double otherY, double otherZ, double radius) {
        if (!world.equals(otherWorld)) {
            return false;
        }
        double dx = x + 0.5 - otherX;
        double dy = y + 0.5 - otherY;
        double dz = z + 0.5 - otherZ;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}
