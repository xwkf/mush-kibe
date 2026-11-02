package br.com.muxe.core.bukkit.hologram;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class LineAim {

    private LineAim() {
    }

    public static double heightAt(Location eye, double columnX, double columnZ) {
        Vector direction = eye.getDirection();
        return heightAt(eye.getX(), eye.getY(), eye.getZ(), direction.getX(), direction.getY(), direction.getZ(), columnX, columnZ);
    }

    static double heightAt(double eyeX, double eyeY, double eyeZ, double dirX, double dirY, double dirZ, double columnX, double columnZ) {
        double flat = dirX * dirX + dirZ * dirZ;
        if (flat == 0) {
            return eyeY;
        }
        double along = ((columnX - eyeX) * dirX + (columnZ - eyeZ) * dirZ) / flat;
        return eyeY + dirY * Math.max(0, along);
    }
}
