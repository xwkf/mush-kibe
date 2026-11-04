package br.com.muxe.core.bukkit.nms;

import net.minecraft.server.v1_8_R3.EntityFireball;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

public final class Fireballs {

    private Fireballs() {
    }

    public static void accelerate(Fireball fireball, Vector direction, double acceleration) {
        EntityFireball handle = ((CraftFireball) fireball).getHandle();
        Vector unit = direction.clone().normalize();
        handle.motX = 0;
        handle.motY = 0;
        handle.motZ = 0;
        handle.dirX = unit.getX() * acceleration;
        handle.dirY = unit.getY() * acceleration;
        handle.dirZ = unit.getZ() * acceleration;
    }
}
