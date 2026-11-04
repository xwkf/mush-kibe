package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.arena.BedWarsSchema;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenuSettings;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenus;
import br.com.muxe.minigame.bedwars.lobby.top.TagColors;
import br.com.muxe.minigame.bedwars.lobby.top.TopData;
import br.com.muxe.minigame.bedwars.lobby.top.TopTexts;
import br.com.muxe.minigame.bedwars.matchmaking.MatchConnector;
import br.com.muxe.minigame.bedwars.network.RoomPublisher;
import br.com.muxe.minigame.bedwars.room.RoomService;
import java.time.Duration;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

final class PlayAgain {

    private PlayAgain() {
    }

    static LobbyMenus menus(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, BedWarsSchema schema, RoomService rooms,
                            RoomPublisher publisher) {
        ConfigurationSection lobby = Objects.requireNonNull(config.getConfigurationSection("lobby"), "Missing configuration section lobby");
        MatchConnector connector = new MatchConnector(core, messages, mode -> schema.modes().contains(mode) ? schema.mode(mode).displayName() : mode,
            (player, request) -> rooms.reserve(player.getUniqueId(), request.mode(), request.wantedArena(), request.wantedRoom()).map(reserved -> {
                rooms.moveToReserved(player);
                return true;
            }).orElse(false));
        connector.start();
        publisher.onPublish(connector.directory()::update);
        TopTexts texts = TopTexts.from(Objects.requireNonNull(lobby.getConfigurationSection("tops"), "Missing configuration section lobby.tops"));
        TopData tops = new TopData(plugin, core, Duration.ofSeconds(texts.refreshSeconds()));
        return new LobbyMenus(plugin, core, LobbyMenuSettings.from(Objects.requireNonNull(lobby.getConfigurationSection("menus"), "Missing configuration section lobby.menus")),
            schema, connector, tops, new TagColors(core), LevelBadges.from(config.getConfigurationSection("levels")), messages);
    }
}
