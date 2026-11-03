package br.com.muxe.core.bukkit.cosmetic.animation;

public final class Ground {

    public static final int DEPTH = 32;
    public static final int ABOVE = 2;

    private Ground() {
    }

    public static double surface(Solid solid, double x, double y, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int top = (int) Math.floor(y) + ABOVE;
        for (int blockY = top; blockY >= top - DEPTH; blockY--) {
            if (solid.at(blockX, blockY, blockZ) && !solid.at(blockX, blockY + 1, blockZ)) {
                return blockY + 1;
            }
        }
        return y;
    }

    public static Vec surface(Solid solid, Vec position) {
        return new Vec(position.x(), surface(solid, position.x(), position.y(), position.z()), position.z());
    }

    @FunctionalInterface
    public interface Solid {

        boolean at(int x, int y, int z);
    }
}
