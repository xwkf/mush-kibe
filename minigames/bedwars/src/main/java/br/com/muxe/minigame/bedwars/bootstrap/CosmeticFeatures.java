package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.core.bukkit.effect.SoundCue;
import br.com.muxe.core.bukkit.mission.menu.MissionMenus;
import br.com.muxe.core.bukkit.nms.BlockMask;
import br.com.muxe.core.bukkit.nms.PlayerSkins;
import br.com.muxe.core.bukkit.permission.command.Replies;
import br.com.muxe.core.bukkit.permission.command.TargetLookup;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.config.live.LiveValue;
import br.com.muxe.minigame.bedwars.cosmetic.BedBreakCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.SceneCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.animation.BedAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.aura.AuraCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.aura.Auras;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.command.CosmeticCommand;
import br.com.muxe.minigame.bedwars.cosmetic.menu.BedWarsMenuItem;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticCategory;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus;
import br.com.muxe.minigame.bedwars.cosmetic.menu.MenuSlot;
import br.com.muxe.minigame.bedwars.cosmetic.message.KillMessages;
import br.com.muxe.minigame.bedwars.cosmetic.message.MessageDescriptions;
import br.com.muxe.minigame.bedwars.cosmetic.message.MessagePreview;
import br.com.muxe.minigame.bedwars.cosmetic.message.MessageSets;
import br.com.muxe.minigame.bedwars.cosmetic.preview.BedPreview;
import br.com.muxe.minigame.bedwars.cosmetic.preview.PreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.ScenePreview;
import br.com.muxe.minigame.bedwars.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewKind;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfileListener;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.bedwars.cosmetic.wood.WoodTypes;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarEditor;
import br.com.muxe.minigame.bedwars.preferences.menu.PreferencesMenu;
import br.com.muxe.minigame.bedwars.teamcolor.menu.FavouriteColorMenu;
import br.com.muxe.minigame.bedwars.settings.MatchSounds;
import br.com.muxe.minigame.bedwars.shop.QuickBuyEditor;
import br.com.muxe.minigame.bedwars.title.EndTitles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

final class CosmeticFeatures {

    private static final String VICTORY_CATEGORY = "bedwars-vitoria";
    private static final String FINAL_KILL_CATEGORY = "bedwars-kill-final";
    private static final String AURA_CATEGORY = "bedwars-aura";
    private static final String WOOD_CATEGORY = "bedwars-madeira";
    private static final String KILL_MESSAGE_CATEGORY = "bedwars-mensagem-kill";

    private final CosmeticMenus menus;
    private final BedBreakCosmetics bedBreaks;
    private final SceneCosmetics victories;
    private final SceneCosmetics finalKills;
    private final AuraCosmetics auras;
    private final MessageSets messageSets;
    private final KillMessages killMessages;
    private final WoodTypes woods;
    private final BedPreview preview;
    private final List<ScenePreview> scenePreviews;

    private CosmeticFeatures(CosmeticMenus menus, BedBreakCosmetics bedBreaks, SceneCosmetics victories, SceneCosmetics finalKills, AuraCosmetics auras, MessageSets messageSets,
                             KillMessages killMessages, WoodTypes woods, BedPreview preview,
                             List<ScenePreview> scenePreviews) {
        this.menus = menus;
        this.bedBreaks = bedBreaks;
        this.victories = victories;
        this.finalKills = finalKills;
        this.auras = auras;
        this.messageSets = messageSets;
        this.killMessages = killMessages;
        this.woods = woods;
        this.preview = preview;
        this.scenePreviews = List.copyOf(scenePreviews);
    }

    static CosmeticFeatures start(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, Commands commands, boolean lobby, EndTitles titles,
                                  ConfigReloads reloads) {
        ConfigurationSection section = section(config, "cosmetics");
        CosmeticMenuSettings menuSettings = CosmeticMenuSettings.from(section(section, "menus"));
        AnimationPlayer animator = new AnimationPlayer(plugin);

        BedAnimations animations = new BedAnimations();
        CosmeticCatalog catalog = CosmeticCatalog.from(section, id -> animations.find(id).isPresent());
        CosmeticProfiles profiles = profiles(plugin, core, catalog);
        SceneAnimations victoryAnimations = SceneAnimations.victories();
        CosmeticCatalog victoryCatalog = CosmeticCatalog.from(section(section, CosmeticMenuSettings.VICTORY_EFFECT), "cosmetics", VICTORY_CATEGORY,
            id -> victoryAnimations.find(id).isPresent());
        CosmeticProfiles victoryProfiles = profiles(plugin, core, victoryCatalog);
        SceneAnimations killAnimations = SceneAnimations.finalKills();
        CosmeticCatalog killCatalog = CosmeticCatalog.from(section(section, CosmeticMenuSettings.FINAL_KILL), "cosmetics", FINAL_KILL_CATEGORY,
            id -> killAnimations.find(id).isPresent());
        CosmeticProfiles killProfiles = profiles(plugin, core, killCatalog);
        SceneAnimations auraAnimations = SceneAnimations.auras();
        CosmeticCatalog auraCatalog = CosmeticCatalog.from(section(section, CosmeticMenuSettings.AURA), "cosmetics", AURA_CATEGORY,
            id -> Auras.find(id).isPresent());
        CosmeticProfiles auraProfiles = profiles(plugin, core, auraCatalog);
        MessageSets messageSets = MessageSets.from(section(config, "kill-messages"));
        CosmeticCatalog killMessageCatalog = CosmeticCatalog.from(section(section, CosmeticMenuSettings.KILL_MESSAGE), "cosmetics", KILL_MESSAGE_CATEGORY,
            id -> messageSets.find(id).isPresent());
        CosmeticProfiles killMessageProfiles = profiles(plugin, core, killMessageCatalog);
        CosmeticCatalog woodCatalog = CosmeticCatalog.from(section(section, CosmeticMenuSettings.WOOD_TYPE), "cosmetics", WOOD_CATEGORY, id -> true);
        CosmeticProfiles woodProfiles = profiles(plugin, core, woodCatalog);

        BedPreview preview = null;
        List<ScenePreview> scenes = new ArrayList<>();
        PreviewGate onlyLobby = (player, cosmetic, back) -> player.sendMessage(messages.get("preview-only-lobby"));
        PreviewGate bedGate = onlyLobby;
        PreviewGate victoryGate = onlyLobby;
        PreviewGate killGate = onlyLobby;
        PreviewGate auraGate = onlyLobby;
        PreviewGate withoutPreview = (player, cosmetic, back) -> {
        };
        if (lobby) {
            LiveValue<PreviewSettings> bedSettings = new LiveValue<>(PreviewSettings.from(section(config, "preview")));
            BedPreview started = new BedPreview(plugin, core.buildMode(), BlockMask.start(plugin), bedSettings, animations, messages);
            started.start();
            preview = started;
            bedGate = started;
            reloads.register("preview", PreviewKind.BED.section(), root -> {
                bedSettings.reload(() -> PreviewSettings.from(section(root, PreviewKind.BED.section())));
                requireOpen(started.refresh(), bedSettings.get().world());
            });
            Predicate<Player> busy = player -> started.previewing(player) || scenes.stream().anyMatch(scene -> scene.previewing(player));
            victoryGate = scenePreview(plugin, core, config, reloads, PreviewKind.VICTORY, victoryAnimations, titles::victory,
                MatchSounds.from(config.getConfigurationSection("sounds")).countdown(), busy, messages, scenes);
            killGate = scenePreview(plugin, core, config, reloads, PreviewKind.FINAL_KILL, killAnimations, player -> {
            }, Optional.empty(), busy, messages, scenes);
            auraGate = scenePreview(plugin, core, config, reloads, PreviewKind.AURA, auraAnimations, player -> {
            }, Optional.empty(), busy, messages, scenes);
        }
        List<CosmeticCategory> categories = new ArrayList<>();
        categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.BED_DESTRUCTION), catalog, profiles, bedGate));
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.FINAL_KILL)) {
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.FINAL_KILL), killCatalog, killProfiles, killGate));
        }
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.VICTORY_EFFECT)) {
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.VICTORY_EFFECT), victoryCatalog, victoryProfiles, victoryGate));
        }
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.AURA)) {
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.AURA), auraCatalog, auraProfiles, auraGate));
        }
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.WOOD_TYPE)) {
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.WOOD_TYPE), woodCatalog, woodProfiles, withoutPreview));
        }
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.KILL_MESSAGE)) {
            MessageDescriptions descriptions = new MessageDescriptions(messageSets, MessagePreview.from(section(section(config, "kill-messages"), "preview")));
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.KILL_MESSAGE), killMessageCatalog, killMessageProfiles, withoutPreview, descriptions));
        }
        CosmeticMenus menus = new CosmeticMenus(menuSettings, categories, core.commands(), core.mainThread(), messages, plugin.getLogger(),
            player -> Colors.translate(core.permissions().display(player.getUniqueId()).color()) + player.getName());
        if (lobby) {
            BedWarsMenuItem item = new BedWarsMenuItem(plugin, MenuSlot.from(section(config, "lobby.menu-item")), menus);
            plugin.getServer().getPluginManager().registerEvents(item, plugin);
            core.buildMode().addHook(item);
            plugin.getServer().getOnlinePlayers().forEach(item::give);
        }
        TargetLookup targets = new TargetLookup(name -> Optional.ofNullable(plugin.getServer().getPlayerExact(name)), core.queries());
        commands.register(new CosmeticCommand(catalog, menus, core.commands(), targets, new Replies(core.mainThread(), messages, plugin.getLogger()),
            new CosmeticProfileListener(profiles, plugin.getLogger())));
        return new CosmeticFeatures(menus, new BedBreakCosmetics(animator, animations, catalog, profiles),
            new SceneCosmetics(animator, victoryAnimations, victoryCatalog, victoryProfiles), new SceneCosmetics(animator, killAnimations, killCatalog, killProfiles),
            new AuraCosmetics(auraCatalog, auraProfiles::of), messageSets, new KillMessages(messageSets, killMessageCatalog, killMessageProfiles::of),
            new WoodTypes(woodCatalog, woodProfiles::of), preview, scenes);
    }

    private static ScenePreview scenePreview(JavaPlugin plugin, MuxeCore core, FileConfiguration config, ConfigReloads reloads, PreviewKind kind, SceneAnimations animations,
                                             Consumer<Player> onScene, Optional<SoundCue> countdownSound, Predicate<Player> busy, MessageCatalog messages,
                                             List<ScenePreview> scenes) {
        LiveValue<ScenePreviewSettings> settings = new LiveValue<>(ScenePreviewSettings.from(section(config, kind.section())));
        ScenePreview scene = new ScenePreview(plugin, core.buildMode(), settings, animations, onScene, countdownSound, busy, messages);
        scene.start();
        scenes.add(scene);
        reloads.register(kind.section(), kind.section(), root -> {
            settings.reload(() -> ScenePreviewSettings.from(section(root, kind.section())));
            requireOpen(scene.refresh(), settings.get().world());
        });
        return scene;
    }

    private static void requireOpen(boolean opened, String world) {
        if (!opened) {
            throw new IllegalStateException("there is no world folder named " + world);
        }
    }

    private static CosmeticProfiles profiles(
JavaPlugin plugin, MuxeCore core, CosmeticCatalog catalog) {
        CosmeticProfiles profiles = new CosmeticProfiles(core.queries(), core.mainThread(), catalog, id -> plugin.getServer().getPlayer(id) != null);
        CosmeticProfileListener listener = new CosmeticProfileListener(profiles, plugin.getLogger());
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getServer().getOnlinePlayers().forEach(listener::load);
        return profiles;
    }

    BedBreakCosmetics bedBreaks() {
        return bedBreaks;
    }

    MessageSets messageSets() {
        return messageSets;
    }

    KillMessages killMessages() {
        return killMessages;
    }

    WoodTypes woods() {
        return woods;
    }

    AuraCosmetics auras() {
        return auras;
    }

    void victory(Player winner, List<Player> viewers, Area area) {
        victories.play(winner, winner.getLocation(), SceneCosmetics.facing(winner.getLocation()), Optional.empty(), SceneCosmetics.riders(winner, viewers), viewers, Optional.of(area));
    }

    void finalKill(Player killer, Player victim, List<Player> viewers) {
        Location at = victim.getLocation();
        Optional<SignedSkin> skin = PlayerSkins.of(victim).map(found -> new SignedSkin(found.value(), found.signature()));
        finalKills.playOnVictim(killer, at, SceneCosmetics.away(killer.getLocation(), at), skin, viewers);
    }

    void openMenu(Player player) {
        menus.openBedWars(player);
    }

    void onMissions(MissionMenus missions) {
        menus.onMissions(missions::open);
    }

    void onQuickBuy(QuickBuyEditor editor) {
        menus.onQuickBuy(editor::open);
    }

    void onHotbar(HotbarEditor editor) {
        menus.onHotbar(editor::openFromBedWarsMenu);
    }

    void onTeamColor(FavouriteColorMenu teamColor) {
        menus.onTeamColor(teamColor::open);
    }

    void onPreferences(PreferencesMenu preferences) {
        menus.onPreferences(preferences::open);
    }

    boolean previewing(Player player) {
        return preview != null && preview.previewing(player) || scenePreviews.stream().anyMatch(scene -> scene.previewing(player));
    }

    void stop() {
        if (preview != null) {
            preview.stop();
        }
        scenePreviews.forEach(ScenePreview::stop);
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + path);
    }
}
