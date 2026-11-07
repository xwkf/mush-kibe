package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.mission.menu.MissionMenus;
import br.com.muxe.core.bukkit.npc.NpcLabelLayout;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.api.arena.ArenaSchema;
import br.com.muxe.minigame.api.matchmaking.MatchConnector;
import br.com.muxe.minigame.bedwars.lobby.LevelChat;
import br.com.muxe.minigame.bedwars.lobby.LobbyNpcs;
import br.com.muxe.minigame.bedwars.lobby.LobbySidebar;
import br.com.muxe.minigame.bedwars.lobby.StatsCache;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenuSettings;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenus;
import br.com.muxe.minigame.bedwars.lobby.settings.LobbyNpc;
import br.com.muxe.minigame.bedwars.lobby.settings.LobbyScoreboardSettings;
import br.com.muxe.minigame.bedwars.lobby.settings.StatsNpcSettings;
import br.com.muxe.minigame.bedwars.lobby.top.TagColors;
import br.com.muxe.minigame.bedwars.lobby.top.TopBoardSettings;
import br.com.muxe.minigame.bedwars.lobby.top.TopBoards;
import br.com.muxe.minigame.bedwars.lobby.top.TopData;
import br.com.muxe.minigame.bedwars.lobby.top.TopSelectorSettings;
import br.com.muxe.minigame.bedwars.lobby.top.TopTexts;
import br.com.muxe.minigame.bedwars.preferences.PlayerPreferences;
import br.com.muxe.minigame.engine.level.LevelUps;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyFeatures {

    public static final String DUELS_ACTION = "duelos";
    public static final String NO_ACTION = "nenhuma";
    public static final String MISSIONS_ACTION = "missoes";

    private final MuxeCore core;
    private final LobbyNpcs npcs;
    private final TopBoards boards;

    private LobbyFeatures(MuxeCore core, LobbyNpcs npcs, TopBoards boards) {
        this.core = core;
        this.npcs = npcs;
        this.boards = boards;
    }

    public static LobbyFeatures start(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, ArenaSchema schema, MissionMenus missions,
                                      PlayerPreferences preferences, MatchConnector connector) {
        ConfigurationSection lobby = section(config, "lobby");
        LevelBadges badges = LevelBadges.from(section(config, "levels"));
        TopTexts texts = TopTexts.from(section(lobby, "tops"));
        StatsCache stats = new StatsCache(plugin, core, new LevelUps(messages.get("level-up"), badges::of));
        stats.start();
        plugin.getServer().getPluginManager().registerEvents(new LevelChat(stats, badges, preferences), plugin);
        TagColors colors = new TagColors(core);
        TopData tops = new TopData(plugin, core, Duration.ofSeconds(texts.refreshSeconds()));
        LobbyMenus menus = new LobbyMenus(plugin, core, LobbyMenuSettings.from(section(lobby, "menus")), schema, connector, tops, colors, badges, messages);
        LobbyNpcs npcs = new LobbyNpcs(plugin, LobbyNpc.from(section(lobby, "npcs")), NpcLabelLayout.from(lobby.getConfigurationSection("npc-labels")), lobby.getStringList("blink-colors"),
            StatsNpcSettings.from(section(lobby, "stats-npc")), connector.directory(), stats, badges, (player, action) -> act(player, action, menus, schema, missions));
        npcs.start();
        ConfigurationSection topSection = section(lobby, "tops");
        TopBoards boards = new TopBoards(plugin, TopBoardSettings.from(section(topSection, "boards")), texts, tops, colors, badges, menus::modeTitle, menus::openTopModes,
            TopSelectorSettings.from(topSection.getConfigurationSection("selector")), menus.topModeOrder(), menus::modeLabel);
        boards.start();
        LobbyScoreboardSettings scoreboard = LobbyScoreboardSettings.from(section(lobby, "scoreboard"));
        core.sidebars().provide(player -> Optional.of(LobbySidebar.content(scoreboard, stats.of(player.getUniqueId()), badges, schema.modes(),
            menus::modeName, core.servers().gamePlayers(BedWarsBootstrap.GAME))));
        return new LobbyFeatures(core, npcs, boards);
    }

    public void stop() {
        core.sidebars().clear();
        npcs.stop();
        boards.stop();
    }

    private static void act(Player player, String action, LobbyMenus menus, ArenaSchema schema, MissionMenus missions) {
        if (MISSIONS_ACTION.equals(action)) {
            missions.open(player, Optional.empty());
        } else if (DUELS_ACTION.equals(action)) {
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
