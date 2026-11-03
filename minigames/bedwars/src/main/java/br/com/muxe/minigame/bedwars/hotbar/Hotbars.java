package br.com.muxe.minigame.bedwars.hotbar;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.minigame.bedwars.hotbar.application.command.SaveHotbarCommand;
import br.com.muxe.minigame.bedwars.hotbar.application.query.FindHotbarQuery;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Hotbars implements HotbarStore, Listener {

    private final MuxeCore core;
    private final Logger logger;
    private final Predicate<HotbarEntry> exists;
    private final Map<UUID, HotbarLayout> layouts = new ConcurrentHashMap<>();

    public Hotbars(MuxeCore core, Logger logger, Predicate<HotbarEntry> exists) {
        this.core = core;
        this.logger = logger;
        this.exists = exists;
    }

    @Override
    public HotbarLayout of(UUID playerId) {
        return layouts.getOrDefault(playerId, HotbarLayout.empty());
    }

    @Override
    public void change(UUID playerId, HotbarLayout layout) {
        layouts.put(playerId, layout);
        core.commands().dispatch(new SaveHotbarCommand(playerId, layout)).whenComplete((result, error) -> {
            if (error != null || !result.isSuccess()) {
                logger.log(Level.WARNING, "Could not save the hotbar of " + playerId, error);
            }
        });
    }

    public void load(UUID playerId) {
        core.queries().ask(new FindHotbarQuery(playerId)).whenComplete((found, error) -> {
            if (error != null) {
                logger.log(Level.WARNING, "Could not load the hotbar of " + playerId, error);
                return;
            }
            found.ifPresent(saved -> layouts.putIfAbsent(playerId, saved.known(exists)));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        layouts.remove(event.getPlayer().getUniqueId());
    }
}
