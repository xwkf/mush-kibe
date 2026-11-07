package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.api.arena.ArenaSchema;
import br.com.muxe.minigame.api.matchmaking.MatchConnector;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenuSettings;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenus;
import br.com.muxe.minigame.bedwars.lobby.top.TagColors;
import br.com.muxe.minigame.bedwars.lobby.top.TopData;
import br.com.muxe.minigame.bedwars.lobby.top.TopTexts;
import java.time.Duration;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

final class PlayAgain {

    private PlayAgain() {
    }

    static LobbyMenus menus(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, ArenaSchema schema, MatchConnector connector) {
        ConfigurationSection lobby = Objects.requireNonNull(config.getConfigurationSection("lobby"), "Missing configuration section lobby");
        TopTexts texts = TopTexts.from(Objects.requireNonNull(lobby.getConfigurationSection("tops"), "Missing configuration section lobby.tops"));
        TopData tops = new TopData(plugin, core, Duration.ofSeconds(texts.refreshSeconds()));
        return new LobbyMenus(plugin, core, LobbyMenuSettings.from(Objects.requireNonNull(lobby.getConfigurationSection("menus"), "Missing configuration section lobby.menus")),
            schema, connector, tops, new TagColors(core), LevelBadges.from(config.getConfigurationSection("levels")), messages);
    }
}
