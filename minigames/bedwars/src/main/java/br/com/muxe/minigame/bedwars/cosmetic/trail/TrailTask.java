package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.minigame.api.match.Match;
import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.match.Participant;
import br.com.muxe.minigame.api.runtime.MatchRuntime;
import br.com.muxe.minigame.api.runtime.Matches;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class TrailTask implements Listener {

    public static final long PERIOD_TICKS = 1L;

    private final Matches<?> rooms;
    private final TrailCosmetics trails;
    private final TrailDriver driver;
    private final Map<String, List<Player>> viewers = new HashMap<>();
    private final Set<UUID> inMatch = new HashSet<>();
    private BukkitTask task;

    private TrailTask(Matches<?> rooms, TrailCosmetics trails, TrailDriver driver) {
        this.rooms = rooms;
        this.trails = trails;
        this.driver = driver;
    }

    public static TrailTask start(Plugin plugin, Matches<?> rooms, TrailCosmetics trails) {
        TrailTask started = new TrailTask(rooms, trails, new TrailDriver(plugin));
        plugin.getServer().getPluginManager().registerEvents(started, plugin);
        started.task = plugin.getServer().getScheduler().runTaskTimer(plugin, started::tick, PERIOD_TICKS, PERIOD_TICKS);
        return started;
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        HandlerList.unregisterAll(this);
        driver.stopAll();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }
        UUID id = shooter.getUniqueId();
        Optional<? extends MatchRuntime<?>> found = rooms.of(id);
        if (found.isEmpty()) {
            return;
        }
        MatchRuntime<?> runtime = found.get();
        Match room = runtime.match();
        Optional<Participant> member = room.player(id);
        if (member.isEmpty() || !TrailRules.launches(room.phase(), member.get().state(), runtime.isSpectator(id))) {
            return;
        }
        Optional<ProjectileTrail> trail = trails.of(id, room.code(), ThreadLocalRandom.current());
        if (trail.isEmpty()) {
            return;
        }
        String code = room.code();
        viewers.computeIfAbsent(code, ignored -> runtime.onlinePlayers());
        driver.launch(projectile, trail.get(), id, code, () -> viewers.getOrDefault(code, List.of()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        driver.stop(id);
        trails.forget(id);
    }

    private void tick() {
        viewers.clear();
        inMatch.clear();
        boolean tracking = !driver.idle();
        for (MatchRuntime<?> runtime : rooms.all()) {
            Match room = runtime.match();
            if (room.phase() != MatchPhase.PLAYING) {
                continue;
            }
            room.players().forEach(player -> inMatch.add(player.id()));
            if (tracking) {
                viewers.put(room.code(), runtime.onlinePlayers());
            }
        }
        if (tracking) {
            driver.tick((owner, room) -> viewers.containsKey(room) && rooms.of(owner).map(runtime -> runtime.match().code().equals(room)).orElse(false));
        }
        trails.retain(inMatch);
    }
}
