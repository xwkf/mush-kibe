package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.preview.KitPlay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class TrailPreview implements KitPlay {

    private static final String GROUP = "preview";

    private final Function<String, Optional<ProjectileTrail>> trails;
    private final TrailDriver driver;
    private BukkitTask task;

    private TrailPreview(Function<String, Optional<ProjectileTrail>> trails, TrailDriver driver) {
        this.trails = trails;
        this.driver = driver;
    }

    public static TrailPreview start(Plugin plugin, Function<String, Optional<ProjectileTrail>> trails) {
        TrailPreview started = new TrailPreview(trails, new TrailDriver(plugin));
        started.task = plugin.getServer().getScheduler().runTaskTimer(plugin, started::tick, TrailTask.PERIOD_TICKS, TrailTask.PERIOD_TICKS);
        return started;
    }

    @Override
    public boolean covers(BedCosmetic cosmetic) {
        return trails.apply(cosmetic.id()).isPresent();
    }

    @Override
    public void launched(Player viewer, BedCosmetic cosmetic, Projectile projectile) {
        List<Player> viewers = List.of(viewer);
        trails.apply(cosmetic.id()).ifPresent(trail -> driver.launch(projectile, trail, viewer.getUniqueId(), GROUP, () -> viewers));
    }

    @Override
    public void ended(Player viewer) {
        driver.stop(viewer.getUniqueId());
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        driver.stopAll();
    }

    private void tick() {
        if (!driver.idle()) {
            driver.tick(TrailPreview::active);
        }
    }

    private static boolean active(UUID owner, String group) {
        return true;
    }
}
