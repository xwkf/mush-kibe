package br.com.muxe.minigame.bedwars.preferences;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.minigame.bedwars.preferences.application.command.SavePreferencesCommand;
import br.com.muxe.minigame.bedwars.preferences.application.query.FindPreferencesQuery;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerPreferences implements Listener {

    private final MuxeCore core;
    private final Logger logger;
    private final Map<UUID, Preferences> loaded = new ConcurrentHashMap<>();

    public PlayerPreferences(MuxeCore core, Logger logger) {
        this.core = core;
        this.logger = logger;
    }

    public Preferences of(UUID playerId) {
        return loaded.getOrDefault(playerId, Preferences.DEFAULTS);
    }

    public void change(UUID playerId, Preferences preferences) {
        loaded.put(playerId, preferences);
        core.commands().dispatch(new SavePreferencesCommand(playerId, preferences)).whenComplete((result, error) -> {
            if (error != null || !result.isSuccess()) {
                logger.log(Level.WARNING, "Could not save the BedWars preferences of " + playerId, error);
            }
        });
    }

    public void load(UUID playerId) {
        core.queries().ask(new FindPreferencesQuery(playerId)).whenComplete((found, error) -> {
            if (error != null) {
                logger.log(Level.WARNING, "Could not load the BedWars preferences of " + playerId, error);
                return;
            }
            found.ifPresent(saved -> loaded.putIfAbsent(playerId, saved));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loaded.remove(event.getPlayer().getUniqueId());
    }
}
