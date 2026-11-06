package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.stage.MountGuard;
import br.com.muxe.core.bukkit.cosmetic.stage.PacketStage;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;

public final class TrailDriver {

    private final MountGuard guard;
    private final TrailFlights flights;

    public TrailDriver(Plugin plugin) {
        this.guard = MountGuard.of(plugin);
        this.flights = new TrailFlights((trail, exception) -> plugin.getLogger().log(Level.WARNING, "Projectile trail " + trail + " failed", exception));
    }

    public void launch(Projectile projectile, ProjectileTrail trail, UUID owner, String group, Supplier<Collection<Player>> viewers) {
        PacketStage stage = new PacketStage(projectile.getWorld(), viewers, guard);
        flights.add(owner, group, new TrailFlight(trail, ProjectileKinds.of(projectile.getType()), new EntityFlightSource(projectile), stage, stage::close,
            ThreadLocalRandom.current()));
    }

    public void tick(BiPredicate<UUID, String> active) {
        flights.tick(active);
    }

    public boolean idle() {
        return flights.isEmpty();
    }

    public void stop(UUID owner) {
        flights.stop(owner);
    }

    public void stopAll() {
        flights.stopAll();
    }
}
