package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.mission.config.MissionConfig;
import br.com.muxe.core.bukkit.mission.menu.MissionMenus;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.mission.MissionModule;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.server.ServerKind;
import br.com.muxe.minigame.bedwars.arena.BedWarsSchema;
import br.com.muxe.minigame.bedwars.command.BedWarsCommand;
import br.com.muxe.minigame.bedwars.listener.Bedbugs;
import br.com.muxe.minigame.bedwars.listener.MatchListener;
import br.com.muxe.minigame.bedwars.mission.BedWarsGoal;
import br.com.muxe.minigame.bedwars.mission.MissionRules;
import br.com.muxe.minigame.bedwars.network.RoomPublisher;
import br.com.muxe.minigame.bedwars.room.RoomService;
import br.com.muxe.minigame.bedwars.settings.BedWarsSettings;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog;
import br.com.muxe.minigame.bedwars.shop.ShopService;
import br.com.muxe.minigame.bedwars.shop.UpgradeCatalog;
import br.com.muxe.minigame.bedwars.stats.StatsModule;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.infrastructure.postgres.PostgresBedWarsStatsRepository;
import br.com.muxe.minigame.engine.MinigameEngine;
import br.com.muxe.minigame.engine.arena.ArenaCatalog;
import br.com.muxe.minigame.engine.mode.GameMode;
import br.com.muxe.minigame.engine.mode.GameModeLoader;
import br.com.muxe.minigame.engine.setup.ArenaFiles;
import br.com.muxe.minigame.engine.setup.session.SetupCommand;
import br.com.muxe.minigame.engine.setup.session.SetupMessages;
import br.com.muxe.minigame.engine.setup.session.SetupService;
import br.com.muxe.minigame.engine.world.server.MatchWorld;
import br.com.muxe.minigame.engine.world.server.MuxeWorlds;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class BedWarsBootstrap {

    public static final String GAME = "bedwars";
    public static final ZoneId STATS_ZONE = ZoneId.of("America/Sao_Paulo");

    private final JavaPlugin plugin;
    private SetupService setup;
    private RoomService rooms;
    private CosmeticFeatures cosmetics;
    private LobbyFeatures lobbyFeatures;
    private RoomPublisher publisher;
    private Bedbugs bedbugs;

    public BedWarsBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public MinigameEngine enable() {
        plugin.saveDefaultConfig();
        MuxeCore core = MuxeCore.get();
        FileConfiguration config = plugin.getConfig();
        MessageCatalog messages = SetupMessages.withOverrides(core.messages().merge(MessageCatalog.from(config.getConfigurationSection("messages"))));
        ConfigurationSection modeSection = Objects.requireNonNull(config.getConfigurationSection("modes"), "Missing configuration section modes");
        List<GameMode> modes = GameModeLoader.load(modeSection);
        BedWarsSchema schema = BedWarsSchema.from(modes, modeSection);
        BedWarsSettings settings = BedWarsSettings.from(config);
        MinigameEngine engine = new MinigameEngine(GAME, modes, core.metrics());
        Commands commands = Commands.of(plugin, messages);
        boolean lobby = core.identity().kind() == ServerKind.LOBBY;
        plugin.getLogger().info("Schema '" + GAME + "' at version " + core.migrations().migrate(GAME, plugin.getClass().getClassLoader()).schemaVersion());
        StatsModule.register(core.commands(), core.queries(), core.transactions(), core.clock(), STATS_ZONE);
        MissionConfig missions = MissionConfig.from(config.getConfigurationSection("missions"), BedWarsGoal::find);
        BedWarsStatsRepository missionXp = new PostgresBedWarsStatsRepository();
        core.missions().register(GAME, missions.catalog(),
            (playerId, completed, at) -> missionXp.addXp(playerId, completed.stream().mapToLong(Mission::xp).sum(), at, STATS_ZONE));
        MissionMenus missionMenus = new MissionMenus(GAME, missions, core.queries(), core.mainThread(), core.clock(), MissionModule.ZONE, plugin.getLogger());
        cosmetics = CosmeticFeatures.start(plugin, core, config, messages, commands, lobby);
        cosmetics.onMissions(missionMenus);
        if (lobby) {
            lobbyFeatures = LobbyFeatures.start(plugin, core, config, messages, schema, missionMenus);
            plugin.getLogger().info("BedWars ready in lobby mode");
            return engine;
        }
        Path data = plugin.getDataFolder().toPath();

        MuxeWorlds worlds = new MuxeWorlds(plugin);
        worlds.deleteLeftovers();
        MatchWorld matchWorld = new MatchWorld(plugin, worlds, GAME);
        ArenaCatalog catalog = new ArenaCatalog(new ArenaFiles(data.resolve("arenas")), data.resolve("maps"), matchWorld::fits, plugin.getLogger());
        try {
            plugin.getLogger().info("Loaded " + catalog.loadAll() + " ready arenas");
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not load the arenas", exception);
        }

        rooms = new RoomService(plugin, core, settings, messages, schema, catalog, matchWorld);
        rooms.missions(missions, MissionRules.from(config.getConfigurationSection("missions")));
        rooms.start();
        rooms.onBedBreak(cosmetics.bedBreaks());
        ShopService shop = new ShopService(
            ShopCatalog.from(Objects.requireNonNull(config.getConfigurationSection("shop"), "Missing configuration section shop")),
            UpgradeCatalog.from(Objects.requireNonNull(config.getConfigurationSection("upgrades"), "Missing configuration section upgrades")),
            messages
        );
        rooms.onNpcClick((player, click) -> shop.open(player, click.runtime(), click.upgrades()));
        publisher = new RoomPublisher(plugin, core, rooms, settings.lobbyGroup());
        publisher.start();
        bedbugs = new Bedbugs(plugin, rooms, core.clock());
        bedbugs.start();
        plugin.getServer().getPluginManager().registerEvents(new MatchListener(plugin, core, rooms, settings, messages, cosmetics::openMenu, bedbugs), plugin);

        setup = new SetupService(plugin, schema, messages, worlds, data);
        setup.onSaved(rooms::arenaSaved);
        setup.start();
        rooms.busyWhen(player -> setup.sessionOf(player).isPresent());
        commands.register(new SetupCommand(setup), new BedWarsCommand(rooms, messages));

        plugin.getLogger().info("BedWars ready with modes " + engine.modes().keySet());
        return engine;
    }

    public void disable() {
        if (lobbyFeatures != null) {
            lobbyFeatures.stop();
        }
        if (publisher != null) {
            publisher.stop();
        }
        if (bedbugs != null) {
            bedbugs.stop();
        }
        if (cosmetics != null) {
            cosmetics.stop();
        }
        if (setup != null) {
            setup.stop();
        }
        if (rooms != null) {
            rooms.stop();
        }
    }
}
