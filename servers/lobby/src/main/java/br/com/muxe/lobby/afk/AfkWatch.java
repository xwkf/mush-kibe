package br.com.muxe.lobby.afk;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.common.activity.IdleTracker;
import br.com.muxe.core.common.afk.AfkReturns;
import br.com.muxe.lobby.config.LobbySettings;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public final class AfkWatch {

    public static final String BYPASS = "muxe.afk.bypass";

    private AfkWatch() {
    }

    public static void start(JavaPlugin plugin, MuxeCore core, AfkSettings settings) {
        if (!settings.enabled()) {
            return;
        }
        IdleTracker idle = new IdleTracker(core.clock(), settings.idleAfter());
        AfkSender sender = new AfkSender(idle, new AfkReturns(core.store()), core.servers(), core.network(), core.identity().id(), settings, core.async());
        plugin.getServer().getPluginManager().registerEvents(new AfkActivityListener(idle), plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> idle.active(player.getUniqueId()));
        long period = LobbySettings.ticks(settings.checkInterval());
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> sender.check(candidates(plugin)), period, period);
    }

    private static List<AfkCandidate> candidates(JavaPlugin plugin) {
        return plugin.getServer().getOnlinePlayers().stream()
            .map(player -> new AfkCandidate(player.getUniqueId(), player.hasPermission(BYPASS)))
            .toList();
    }
}
