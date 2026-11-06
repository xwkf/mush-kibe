package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.command.config.ConfigPathSuggestions;
import br.com.muxe.core.bukkit.command.config.ConfigReplies;
import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.config.PluginConfig;
import br.com.muxe.core.bukkit.config.edit.ConfigEditor;
import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.bukkit.config.edit.PluginConfigStore;
import br.com.muxe.core.bukkit.mission.config.MissionConfig;
import br.com.muxe.core.bukkit.mission.menu.MissionMenus;
import br.com.muxe.core.bukkit.prompt.AnvilPrompts;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.messaging.channel.PacketTarget;
import br.com.muxe.core.common.mission.MissionModule;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.server.ServerKind;
import br.com.muxe.minigame.bedwars.arena.BedWarsSchema;
import br.com.muxe.minigame.bedwars.command.BedWarsCommand;
import br.com.muxe.minigame.bedwars.command.BedWarsConfigCommand;
import br.com.muxe.minigame.bedwars.command.GlobalChatCommand;
import br.com.muxe.minigame.bedwars.cosmetic.aura.AuraTask;
import br.com.muxe.minigame.bedwars.cosmetic.trail.TrailTask;
import br.com.muxe.minigame.bedwars.hotbar.HotbarChoices;
import br.com.muxe.minigame.bedwars.hotbar.HotbarDelivery;
import br.com.muxe.minigame.bedwars.hotbar.HotbarModule;
import br.com.muxe.minigame.bedwars.hotbar.Hotbars;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarEditor;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarIcons;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarMenuSettings;
import br.com.muxe.minigame.bedwars.invisibility.InvisiblePlayers;
import br.com.muxe.minigame.bedwars.listener.Bedbugs;
import br.com.muxe.minigame.bedwars.listener.MatchListener;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.lobby.menu.LobbyMenus;
import br.com.muxe.minigame.bedwars.mission.BedWarsGoal;
import br.com.muxe.minigame.bedwars.mission.MissionRules;
import br.com.muxe.minigame.bedwars.network.RoomPublisher;
import br.com.muxe.minigame.bedwars.preferences.PlayerPreferences;
import br.com.muxe.minigame.bedwars.preferences.PreferencesModule;
import br.com.muxe.minigame.bedwars.preferences.menu.PreferencesMenu;
import br.com.muxe.minigame.bedwars.quickbuy.QuickBuyModule;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.room.RoomService;
import br.com.muxe.minigame.bedwars.settings.BedWarsSettings;
import br.com.muxe.minigame.bedwars.shop.QuickBuyEditor;
import br.com.muxe.minigame.bedwars.shop.QuickBuys;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog;
import br.com.muxe.minigame.bedwars.shop.ShopService;
import br.com.muxe.minigame.bedwars.shop.UpgradeCatalog;
import br.com.muxe.minigame.bedwars.stats.StatsModule;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.infrastructure.postgres.PostgresBedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.teamcolor.FavouriteColors;
import br.com.muxe.minigame.bedwars.teamcolor.TeamColorModule;
import br.com.muxe.minigame.bedwars.teamcolor.menu.FavouriteColorMenu;
import br.com.muxe.minigame.bedwars.teamcolor.menu.FavouriteColorMenuSettings;
import br.com.muxe.minigame.bedwars.title.EndTitles;
import br.com.muxe.minigame.bedwars.tnt.TntTimerSettings;
import br.com.muxe.minigame.bedwars.tnt.TntTimers;
import br.com.muxe.minigame.engine.MinigameEngine;
import br.com.muxe.minigame.engine.arena.ArenaCatalog;
import br.com.muxe.minigame.engine.arena.ArenaSavedPacket;
import br.com.muxe.minigame.engine.arena.ArenaStore;
import br.com.muxe.minigame.engine.arena.DatabaseArenaStore;
import br.com.muxe.minigame.engine.mode.GameMode;
import br.com.muxe.minigame.engine.mode.GameModeLoader;
import br.com.muxe.minigame.engine.setup.session.SetupCommand;
import br.com.muxe.minigame.engine.setup.session.SetupMessages;
import br.com.muxe.minigame.engine.setup.session.SetupService;
import br.com.muxe.minigame.engine.world.server.MatchWorld;
import br.com.muxe.minigame.engine.world.server.MuxeWorlds;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    private AuraTask auras;
    private TrailTask trails;

    public BedWarsBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public MinigameEngine enable() {
        FileConfiguration config = PluginConfig.load(plugin);
        MuxeCore core = MuxeCore.get();
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
        QuickBuyModule.register(core.commands(), core.queries(), core.transactions(), core.clock());
        HotbarModule.register(core.commands(), core.queries(), core.transactions(), core.clock());
        PreferencesModule.register(core.commands(), core.queries(), core.transactions(), core.clock());
        TeamColorModule.register(core.commands(), core.queries(), core.transactions(), core.clock());
        MissionConfig missions = MissionConfig.from(config.getConfigurationSection("missions"), BedWarsGoal::find);
        BedWarsStatsRepository missionXp = new PostgresBedWarsStatsRepository();
        core.missions().register(GAME, missions.catalog(),
            (playerId, completed, at) -> missionXp.addXp(playerId, completed.stream().mapToLong(Mission::xp).sum(), at, STATS_ZONE));
        MissionMenus missionMenus = new MissionMenus(GAME, missions, core.queries(), core.mainThread(), core.clock(), MissionModule.ZONE, plugin.getLogger());
        EndTitles titles = EndTitles.from(plugin, config.getConfigurationSection("end-titles"));
        ConfigReloads reloads = new ConfigReloads();
        ConfigEditor configEditor = new ConfigEditor(new PluginConfigStore(plugin, core.async()), reloads);
        ConfigPathSuggestions.register(commands, configEditor);
        commands.register(new BedWarsConfigCommand(configEditor, new ConfigReplies(messages)));
        cosmetics = CosmeticFeatures.start(plugin, core, config, messages, commands, lobby, titles, reloads);
        cosmetics.onMissions(missionMenus);
        ShopCatalog shopCatalog = ShopCatalog.from(Objects.requireNonNull(config.getConfigurationSection("shop"), "Missing configuration section shop"));
        QuickBuys quickBuys = new QuickBuys(core, plugin.getLogger(), QuickBuy.of(shopCatalog.quickBuy()), shopCatalog.items()::containsKey);
        plugin.getServer().getPluginManager().registerEvents(quickBuys, plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> quickBuys.load(player.getUniqueId()));
        cosmetics.onQuickBuy(new QuickBuyEditor(shopCatalog, QuickBuyEditor.Settings.from(Objects.requireNonNull(config.getConfigurationSection("shop.quick-buy-editor"), "Missing configuration section shop.quick-buy-editor")), quickBuys,
            messages, AnvilPrompts.start(plugin), core.mainThread(), plugin.getLogger()));
        HotbarChoices hotbarChoices = new HotbarChoices(shopCatalog);
        Hotbars hotbars = new Hotbars(core, plugin.getLogger(), hotbarChoices::exists);
        plugin.getServer().getPluginManager().registerEvents(hotbars, plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> hotbars.load(player.getUniqueId()));
        HotbarMenuSettings hotbarMenus = HotbarMenuSettings.from(
            Objects.requireNonNull(config.getConfigurationSection("shop.hotbar-menus"), "Missing configuration section shop.hotbar-menus"));
        HotbarEditor hotbarEditor = new HotbarEditor(hotbarMenus, hotbarChoices, new HotbarIcons(shopCatalog, hotbarChoices, hotbarMenus.adding()), hotbars, messages);
        cosmetics.onHotbar(hotbarEditor);
        PlayerPreferences preferences = new PlayerPreferences(core, plugin.getLogger());
        plugin.getServer().getPluginManager().registerEvents(preferences, plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> preferences.load(player.getUniqueId()));
        cosmetics.onPreferences(new PreferencesMenu(PreferencesMenu.Settings.from(
            Objects.requireNonNull(config.getConfigurationSection("preferences"), "Missing configuration section preferences")), preferences));
        FavouriteColors teamColors = new FavouriteColors(core, plugin.getLogger());
        plugin.getServer().getPluginManager().registerEvents(teamColors, plugin);
        plugin.getServer().getOnlinePlayers().forEach(player -> teamColors.load(player.getUniqueId()));
        cosmetics.onTeamColor(new FavouriteColorMenu(FavouriteColorMenuSettings.from(
            Objects.requireNonNull(config.getConfigurationSection("favourite-color"), "Missing configuration section favourite-color")), teamColors, messages));
        ArenaSavedPacket.register(core.packetRegistry());
        ArenaStore arenas = new DatabaseArenaStore(GAME, core.commands(), core.queries());
        MuxeWorlds worlds = new MuxeWorlds(plugin);
        worlds.deleteLeftovers();
        if (lobby) {
            lobbyFeatures = LobbyFeatures.start(plugin, core, config, messages, schema, missionMenus, preferences);
            setup = new SetupService(plugin, schema, messages, worlds, arenas);
            setup.onEnter(player -> core.buildMode().enter(player));
            setup.onLeave(player -> core.buildMode().leave(player));
            setup.onSaved(name -> core.packets().publish(PacketTarget.group(GAME), new ArenaSavedPacket(GAME, name)).exceptionally(error -> {
                plugin.getLogger().log(Level.WARNING, "Could not tell the BedWars servers that arena " + name + " was saved", error);
                return null;
            }));
            setup.start();
            commands.register(new SetupCommand(setup));
            plugin.getLogger().info("BedWars ready in lobby mode");
            return engine;
        }

        MatchWorld matchWorld = new MatchWorld(plugin, worlds, GAME);
        ArenaCatalog catalog = new ArenaCatalog(arenas, matchWorld::fits, plugin.getLogger());
        CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Loaded " + catalog.loadAll() + " ready arenas");
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Could not load the arenas", exception);
            }
        }, core.async());
        core.packets().subscribe(ArenaSavedPacket.TYPE, context -> {
            if (GAME.equals(context.packet().game())) {
                CompletableFuture.runAsync(() -> catalog.reload(context.packet().name()).ifPresent(loaded ->
                    plugin.getLogger().info("Arena " + loaded.name() + " updated")), core.async());
            }
        });

        rooms = new RoomService(plugin, core, settings, messages, schema, catalog, matchWorld, LevelBadges.from(config.getConfigurationSection("levels")));
        rooms.missions(missions, MissionRules.from(config.getConfigurationSection("missions")));
        rooms.preferences(preferences);
        rooms.teamColors(teamColors);
        rooms.killMessages(cosmetics.messageSets(), cosmetics.killMessages());
        rooms.start();
        engine.track(rooms::activeMatches, () -> catalog.all().size());
        rooms.onBedBreak(cosmetics.bedBreaks());
        rooms.onVictory(cosmetics::victory);
        rooms.onFinalKill(cosmetics::finalKill);
        rooms.endTitles(titles);
        ShopService shop = new ShopService(
            shopCatalog,
            UpgradeCatalog.from(Objects.requireNonNull(config.getConfigurationSection("upgrades"), "Missing configuration section upgrades")),
            messages,
            settings.sounds(),
            quickBuys,
            settings.items(),
            new HotbarDelivery(hotbars, hotbarChoices),
            hotbarEditor,
            cosmetics.woods()
        );
        rooms.onNpcClick((player, click) -> shop.open(player, click.runtime(), click.upgrades()));
        publisher = new RoomPublisher(plugin, core, rooms, List.of(settings.lobbyGroup(), GAME));
        LobbyMenus playAgain = PlayAgain.menus(plugin, core, config, messages, schema, rooms, publisher);
        publisher.start();
        bedbugs = new Bedbugs(plugin, rooms, core.clock());
        bedbugs.start();
        TntTimers tntTimers = TntTimers.start(plugin, TntTimerSettings.from(
            Objects.requireNonNull(config.getConfigurationSection("tnt-timer"), "Missing configuration section tnt-timer")), preferences);
        plugin.getServer().getPluginManager().registerEvents(new MatchListener(plugin, core, rooms, settings, messages, cosmetics::openMenu, bedbugs,
            preferences, tntTimers, playAgain::openMode), plugin);

        InvisiblePlayers.start(plugin, rooms);
        auras = AuraTask.start(plugin, rooms, cosmetics.auras());
        trails = TrailTask.start(plugin, rooms, cosmetics.trails());
        commands.register(new BedWarsCommand(rooms, messages));
        commands.register(new GlobalChatCommand(rooms));

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
        if (auras != null) {
            auras.stop();
        }
        if (trails != null) {
            trails.stop();
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
