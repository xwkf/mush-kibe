package br.com.muxe.minigame.bedwars.lobby;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.minigame.bedwars.level.LevelBar;
import br.com.muxe.minigame.bedwars.stats.application.query.FindPlayerStatsQuery;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class StatsCache implements Listener {

    public static final long REFRESH_TICKS = 20L * 30;

    private final Plugin plugin;
    private final MuxeCore core;
    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();

    public StatsCache(Plugin plugin, MuxeCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> load(player.getUniqueId()));
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> plugin.getServer().getOnlinePlayers().forEach(player -> load(player.getUniqueId())), REFRESH_TICKS, REFRESH_TICKS);
    }

    public PlayerStats of(UUID playerId) {
        return stats.getOrDefault(playerId, PlayerStats.EMPTY);
    }

    public CompletableFuture<PlayerStats> load(UUID playerId) {
        return core.queries().ask(new FindPlayerStatsQuery(playerId)).whenComplete((loaded, error) -> {
            if (error != null) {
                plugin.getLogger().log(Level.WARNING, "Could not load the BedWars stats of " + playerId, error);
            } else if (plugin.getServer().getPlayer(playerId) != null) {
                stats.put(playerId, loaded);
                core.mainThread().execute(() -> {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player != null) {
                        LevelBar.show(player, loaded.general().xp());
                    }
                });
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        stats.remove(player.getUniqueId());
    }
}
