package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.core.bukkit.mission.menu.MissionMenus;
import br.com.muxe.core.bukkit.nms.BlockMask;
import br.com.muxe.core.bukkit.nms.PlayerSkins;
import br.com.muxe.core.bukkit.npc.NpcSkin;
import br.com.muxe.core.bukkit.permission.command.Replies;
import br.com.muxe.core.bukkit.permission.command.TargetLookup;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.BedBreakCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.SceneCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.animation.BedAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.command.CosmeticCommand;
import br.com.muxe.minigame.bedwars.cosmetic.menu.BedWarsMenuItem;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticCategory;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.bedwars.cosmetic.menu.MenuSlot;
import br.com.muxe.minigame.bedwars.cosmetic.preview.BedPreview;
import br.com.muxe.minigame.bedwars.cosmetic.preview.PreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.ScenePreview;
import br.com.muxe.minigame.bedwars.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfileListener;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.bedwars.preferences.menu.PreferencesMenu;
import br.com.muxe.minigame.bedwars.settings.MatchSounds;
import br.com.muxe.minigame.bedwars.shop.QuickBuyEditor;
import br.com.muxe.minigame.bedwars.title.EndTitles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

final class CosmeticFeatures {

    private static final String VICTORY_CATEGORY = "bedwars-vitoria";
    private static final String FINAL_KILL_CATEGORY = "bedwars-kill-final";

    private final CosmeticMenus menus;
    private final BedBreakCosmetics bedBreaks;
    private final SceneCosmetics victories;
    private final SceneCosmetics finalKills;
    private final BedPreview preview;
    private final List<ScenePreview> scenePreviews;

    private CosmeticFeatures(CosmeticMenus menus, BedBreakCosmetics bedBreaks, SceneCosmetics victories, SceneCosmetics finalKills, BedPreview preview,
                             List<ScenePreview> scenePreviews) {
        this.menus = menus;
        this.bedBreaks = bedBreaks;
        this.victories = victories;
        this.finalKills = finalKills;
        this.preview = preview;
        this.scenePreviews = List.copyOf(scenePreviews);
    }

    static CosmeticFeatures start(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, Commands commands, boolean lobby, EndTitles titles) {
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

        BedPreview preview = null;
        List<ScenePreview> scenes = new ArrayList<>();
        PreviewGate onlyLobby = (player, cosmetic, back) -> player.sendMessage(messages.get("preview-only-lobby"));
        PreviewGate bedGate = onlyLobby;
        PreviewGate victoryGate = onlyLobby;
        PreviewGate killGate = onlyLobby;
        if (lobby) {
            BedPreview started = new BedPreview(plugin, core.buildMode(), BlockMask.start(plugin), PreviewSettings.from(section(config, "preview")), animations, messages);
            if (started.start()) {
                preview = started;
                bedGate = started;
            }
            BedPreview bedPreview = preview;
            Predicate<Player> busy = player -> bedPreview != null && bedPreview.previewing(player) || scenes.stream().anyMatch(scene -> scene.previewing(player));
            ScenePreview victoryScene = new ScenePreview(plugin, core.buildMode(), ScenePreviewSettings.from(section(config, "victory-preview")), victoryAnimations,
                titles::victory, MatchSounds.from(config.getConfigurationSection("sounds")).countdown(), busy, messages);
            if (victoryScene.start()) {
                scenes.add(victoryScene);
                victoryGate = victoryScene;
            }
            ScenePreview killScene = new ScenePreview(plugin, core.buildMode(), ScenePreviewSettings.from(section(config, "final-kill-preview")), killAnimations,
                player -> {
                }, Optional.empty(), busy, messages);
            if (killScene.start()) {
                scenes.add(killScene);
                killGate = killScene;
            }
        }
        List<CosmeticCategory> categories = new ArrayList<>();
        categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.BED_DESTRUCTION), catalog, profiles, bedGate));
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.FINAL_KILL)) {
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.FINAL_KILL), killCatalog, killProfiles, killGate));
        }
        if (menuSettings.categories().containsKey(CosmeticMenuSettings.VICTORY_EFFECT)) {
            categories.add(CosmeticCategory.of(menuSettings.category(CosmeticMenuSettings.VICTORY_EFFECT), victoryCatalog, victoryProfiles, victoryGate));
        }
        CosmeticMenus menus = new CosmeticMenus(menuSettings, categories, core.commands(), core.mainThread(), messages, plugin.getLogger());
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
            preview, scenes);
    }

    private static CosmeticProfiles profiles(JavaPlugin plugin, MuxeCore core, CosmeticCatalog catalog) {
        CosmeticProfiles profiles = new CosmeticProfiles(core.queries(), core.mainThread(), catalog, id -> plugin.getServer().getPlayer(id) != null);
        CosmeticProfileListener listener = new CosmeticProfileListener(profiles, plugin.getLogger());
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getServer().getOnlinePlayers().forEach(listener::load);
        return profiles;
    }

    BedBreakCosmetics bedBreaks() {
        return bedBreaks;
    }

    void victory(Player winner, List<Player> viewers) {
        victories.play(winner, winner.getLocation(), SceneCosmetics.facing(winner.getLocation()), Optional.empty(), viewers);
    }

    void finalKill(Player killer, Player victim, List<Player> viewers) {
        Location at = victim.getLocation();
        finalKills.play(killer, at, SceneCosmetics.away(killer.getLocation(), at), PlayerSkins.of(victim).map(NpcSkin::value), viewers);
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
