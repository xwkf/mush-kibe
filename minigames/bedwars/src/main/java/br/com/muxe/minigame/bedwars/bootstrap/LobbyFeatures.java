package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.arena.BedWarsSchema;
import br.com.muxe.minigame.bedwars.lobby.LobbyNpcs;
import br.com.muxe.minigame.bedwars.lobby.StatsCache;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenuSettings;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenus;
import br.com.muxe.minigame.bedwars.lobby.settings.LobbyNpc;
import br.com.muxe.minigame.bedwars.lobby.settings.StatsNpcSettings;
import br.com.muxe.minigame.bedwars.lobby.top.TagColors;
import br.com.muxe.minigame.bedwars.lobby.top.TopBoardSettings;
import br.com.muxe.minigame.bedwars.lobby.top.TopBoards;
import br.com.muxe.minigame.bedwars.lobby.top.TopData;
import br.com.muxe.minigame.bedwars.lobby.top.TopTexts;
import br.com.muxe.minigame.bedwars.matchmaking.MatchConnector;
import java.time.Duration;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyFeatures {

    public static final String DUELS_ACTION = "duelos";
    public static final String NO_ACTION = "nenhuma";

    private final LobbyNpcs npcs;
    private final TopBoards boards;

    private LobbyFeatures(LobbyNpcs npcs, TopBoards boards) {
        this.npcs = npcs;
        this.boards = boards;
    }

    public static LobbyFeatures start(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, BedWarsSchema schema) {
        ConfigurationSection lobby = section(config, "lobby");
        LevelBadges badges = LevelBadges.from(section(config, "levels"));
        TopTexts texts = TopTexts.from(section(lobby, "tops"));
        MatchConnector connector = new MatchConnector(core, messages, mode -> schema.modes().contains(mode) ? schema.mode(mode).displayName() : mode);
        connector.start();
        StatsCache stats = new StatsCache(plugin, core);
        stats.start();
        TagColors colors = new TagColors(core);
        TopData tops = new TopData(plugin, core, Duration.ofSeconds(texts.refreshSeconds()));
        LobbyMenus menus = new LobbyMenus(plugin, core, LobbyMenuSettings.from(section(lobby, "menus")), schema, connector, tops, colors, badges, messages);
        LobbyNpcs npcs = new LobbyNpcs(plugin, LobbyNpc.from(section(lobby, "npcs")), lobby.getStringList("blink-colors"),
            StatsNpcSettings.from(section(lobby, "stats-npc")), connector.directory(), stats, badges, (player, action) -> act(player, action, menus, schema));
        npcs.start();
        TopBoards boards = new TopBoards(plugin, TopBoardSettings.from(section(section(lobby, "tops"), "boards")), texts, tops, colors, badges, menus::modeTitle, menus::openTopModes);
        boards.start();
        return new LobbyFeatures(npcs, boards);
    }

    public void stop() {
        npcs.stop();
        boards.stop();
    }

    private static void act(Player player, String action, LobbyMenus menus, BedWarsSchema schema) {
        if (DUELS_ACTION.equals(action)) {
            menus.openDuels(player);
        } else if (LobbyNpcs.STATS_ACTION.equals(action)) {
            menus.openStats(player);
        } else if (!NO_ACTION.equals(action) && schema.modes().contains(action)) {
            menus.openMode(player, action);
        }
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
    }
}
