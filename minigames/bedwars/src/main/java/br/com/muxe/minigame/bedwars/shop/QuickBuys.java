package br.com.muxe.minigame.bedwars.shop;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.minigame.bedwars.quickbuy.application.command.SaveQuickBuyCommand;
import br.com.muxe.minigame.bedwars.quickbuy.application.query.FindQuickBuyQuery;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
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

public final class QuickBuys implements Listener {

    private final MuxeCore core;
    private final Logger logger;
    private final QuickBuy defaults;
    private final Predicate<String> exists;
    private final Map<UUID, QuickBuy> layouts = new ConcurrentHashMap<>();

    public QuickBuys(MuxeCore core, Logger logger, QuickBuy defaults, Predicate<String> exists) {
        this.core = core;
        this.logger = logger;
        this.defaults = defaults.known(exists);
        this.exists = exists;
    }

    public QuickBuy of(UUID playerId) {
        return layouts.getOrDefault(playerId, defaults);
    }

    public void change(UUID playerId, QuickBuy quickBuy) {
        layouts.put(playerId, quickBuy);
        core.commands().dispatch(new SaveQuickBuyCommand(playerId, quickBuy)).whenComplete((result, error) -> {
            if (error != null || !result.isSuccess()) {
                logger.log(Level.WARNING, "Could not save the quick buy of " + playerId, error);
            }
        });
    }

    public void load(UUID playerId) {
        core.queries().ask(new FindQuickBuyQuery(playerId)).whenComplete((found, error) -> {
            if (error != null) {
                logger.log(Level.WARNING, "Could not load the quick buy of " + playerId, error);
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
