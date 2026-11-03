package br.com.muxe.minigame.bedwars.teamcolor;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.minigame.bedwars.teamcolor.application.command.SaveFavouriteColorCommand;
import br.com.muxe.minigame.bedwars.teamcolor.application.query.FindFavouriteColorQuery;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class FavouriteColors implements TeamColorStore, Listener {

    private final MuxeCore core;
    private final Logger logger;
    private final Map<UUID, TeamColor> colors = new ConcurrentHashMap<>();

    public FavouriteColors(MuxeCore core, Logger logger) {
        this.core = core;
        this.logger = logger;
    }

    @Override
    public Optional<TeamColor> of(UUID playerId) {
        return Optional.ofNullable(colors.get(playerId));
    }

    @Override
    public void change(UUID playerId, Optional<TeamColor> color) {
        color.ifPresentOrElse(chosen -> colors.put(playerId, chosen), () -> colors.remove(playerId));
        core.commands().dispatch(new SaveFavouriteColorCommand(playerId, color)).whenComplete((result, error) -> {
            if (error != null || !result.isSuccess()) {
                logger.log(Level.WARNING, "Could not save the favourite team colour of " + playerId, error);
            }
        });
    }

    public void load(UUID playerId) {
        core.queries().ask(new FindFavouriteColorQuery(playerId)).whenComplete((found, error) -> {
            if (error != null) {
                logger.log(Level.WARNING, "Could not load the favourite team colour of " + playerId, error);
                return;
            }
            found.ifPresent(saved -> colors.putIfAbsent(playerId, saved));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        colors.remove(event.getPlayer().getUniqueId());
    }
}
