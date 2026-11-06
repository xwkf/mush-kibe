package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

final class EntityFlightSource implements FlightSource {

    private final Projectile projectile;
    private final Location location;

    EntityFlightSource(Projectile projectile) {
        this.projectile = projectile;
        this.location = projectile.getLocation();
    }

    @Override
    public Vec position() {
        projectile.getLocation(location);
        return new Vec(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public Vec velocity() {
        Vector velocity = projectile.getVelocity();
        return new Vec(velocity.getX(), velocity.getY(), velocity.getZ());
    }

    @Override
    public boolean valid() {
        return projectile.isValid();
    }

    @Override
    public boolean onGround() {
        return projectile.isOnGround();
    }
}
