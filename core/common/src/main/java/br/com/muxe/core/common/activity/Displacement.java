package br.com.muxe.core.common.activity;

public record Displacement(double x, double y, double z) {

    public static Displacement between(double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        return new Displacement(toX - fromX, toY - fromY, toZ - fromZ);
    }

    public boolean walked() {
        return x != 0 || z != 0 || y > 0;
    }
}
