package br.com.muxe.minigame.bedwars.bootstrap;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.command.framework.Commands;
import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.core.bukkit.mission.menu.MissionMenus;
import br.com.muxe.core.bukkit.nms.BlockMask;
import br.com.muxe.core.bukkit.permission.command.Replies;
import br.com.muxe.core.bukkit.permission.command.TargetLookup;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.config.live.LiveValue;
import br.com.muxe.minigame.api.cosmetic.GameCosmetics;
import br.com.muxe.minigame.api.cosmetic.aura.AuraCosmetics;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticCategory;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenus;
import br.com.muxe.minigame.api.cosmetic.menu.MenuSlot;
import br.com.muxe.minigame.api.cosmetic.message.KillMessages;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticProfileListener;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.api.cosmetic.trail.TrailCosmetics;
import br.com.muxe.minigame.api.runtime.MatchEvents;
import br.com.muxe.minigame.api.settings.MatchSounds;
import br.com.muxe.minigame.api.view.EndTitles;
import br.com.muxe.minigame.bedwars.cosmetic.BedBreakCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.BedWarsCosmetics;
import br.com.muxe.minigame.bedwars.cosmetic.animation.BedAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.command.CosmeticCommand;
import br.com.muxe.minigame.bedwars.cosmetic.menu.BedWarsMenu;
import br.com.muxe.minigame.bedwars.cosmetic.menu.BedWarsMenuItem;
import br.com.muxe.minigame.bedwars.cosmetic.menu.BedWarsMenuSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.BedPreview;
import br.com.muxe.minigame.bedwars.cosmetic.preview.PreviewSettings;
import br.com.muxe.minigame.bedwars.cosmetic.preview.config.PreviewKind;
import br.com.muxe.minigame.bedwars.cosmetic.wood.WoodTypes;
import br.com.muxe.minigame.bedwars.hotbar.menu.HotbarEditor;
import br.com.muxe.minigame.bedwars.preferences.menu.PreferencesMenu;
import br.com.muxe.minigame.bedwars.room.BedWarsMatch;
import br.com.muxe.minigame.bedwars.shop.QuickBuyEditor;
import br.com.muxe.minigame.bedwars.teamcolor.menu.FavouriteColorMenu;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

final class CosmeticFeatures implements MatchEvents {

    private final GameCosmetics shared;
    private final BedWarsMenu menu;
    private final BedBreakCosmetics bedBreaks;
    private final WoodTypes woods;
    private final BedPreview preview;

    private CosmeticFeatures(GameCosmetics shared, BedWarsMenu menu, BedBreakCosmetics bedBreaks, WoodTypes woods, BedPreview preview) {
        this.shared = shared;
        this.menu = menu;
        this.bedBreaks = bedBreaks;
        this.woods = woods;
        this.preview = preview;
    }

    static CosmeticFeatures start(JavaPlugin plugin, MuxeCore core, FileConfiguration config, MessageCatalog messages, Commands commands, boolean lobby, EndTitles titles,
                                  ConfigReloads reloads) {
        ConfigurationSection section = section(config, "cosmetics");
        ConfigurationSection menus = section(section, "menus");
        CosmeticMenuSettings menuSettings = BedWarsCosmetics.menuSettings(menus);
        BedAnimations animations = new BedAnimations();
        CosmeticCatalog catalog = BedBreakCosmetics.catalog(section, id -> animations.find(id).isPresent());
        CosmeticProfiles profiles = GameCosmetics.profiles(plugin, core, catalog);
        CosmeticCatalog woodCatalog = CosmeticCatalog.from(section(section, BedWarsCosmetics.WOOD_TYPE), "cosmetics", BedWarsCosmetics.WOOD_CATEGORY, id -> true);
        CosmeticProfiles woodProfiles = GameCosmetics.profiles(plugin, core, woodCatalog);

        BedPreview preview = null;
        PreviewGate bedGate = (player, cosmetic, back) -> player.sendMessage(messages.get("preview-only-lobby"));
        if (lobby) {
            LiveValue<PreviewSettings> bedSettings = new LiveValue<>(PreviewSettings.from(section(config, "preview")));
            BedPreview started = new BedPreview(plugin, core.buildMode(), BlockMask.start(plugin), bedSettings, animations, messages);
            started.start();
            preview = started;
            bedGate = started;
            reloads.register("preview", PreviewKind.BED.section(), root -> {
                bedSettings.reload(() -> PreviewSettings.from(section(root, PreviewKind.BED.section())));
                if (!started.refresh()) {
                    throw new IllegalStateException("there is no world folder named " + bedSettings.get().world());
                }
            });
        }
        BedPreview bedPreview = preview;
        GameCosmetics shared = GameCosmetics.start(new GameCosmetics.Setup(plugin, core, BedWarsMatch.GAME, BedWarsCosmetics.SHARED, config, messages, lobby, titles,
            reloads, MatchSounds.from(config.getConfigurationSection("sounds")).countdown(), player -> bedPreview != null && bedPreview.previewing(player)));

        List<CosmeticCategory> categories = new ArrayList<>();
        categories.add(CosmeticCategory.of(menuSettings.category(BedWarsCosmetics.BED_DESTRUCTION), catalog, profiles, bedGate));
        shared.category(CosmeticKind.FINAL_KILL, menuSettings).ifPresent(categories::add);
        shared.category(CosmeticKind.VICTORY_EFFECT, menuSettings).ifPresent(categories::add);
        shared.category(CosmeticKind.AURA, menuSettings).ifPresent(categories::add);
        shared.category(CosmeticKind.PROJECTILE_TRAIL, menuSettings).ifPresent(categories::add);
        if (menuSettings.categories().containsKey(BedWarsCosmetics.WOOD_TYPE)) {
            categories.add(CosmeticCategory.of(menuSettings.category(BedWarsCosmetics.WOOD_TYPE), woodCatalog, woodProfiles, (player, cosmetic, back) -> {
            }));
        }
        shared.category(CosmeticKind.KILL_MESSAGE, menuSettings).ifPresent(categories::add);
        CosmeticMenus cosmeticMenus = new CosmeticMenus(menuSettings, categories, core.commands(), core.mainThread(), messages, plugin.getLogger(),
            player -> Colors.translate(core.permissions().display(player.getUniqueId()).color()) + player.getName());
        BedWarsMenu menu = new BedWarsMenu(BedWarsMenuSettings.from(menus), cosmeticMenus, messages);
        if (lobby) {
            BedWarsMenuItem item = new BedWarsMenuItem(plugin, MenuSlot.from(section(config, "lobby.menu-item")), menu);
            plugin.getServer().getPluginManager().registerEvents(item, plugin);
            core.buildMode().addHook(item);
            plugin.getServer().getOnlinePlayers().forEach(item::give);
        }
        TargetLookup targets = new TargetLookup(name -> Optional.ofNullable(plugin.getServer().getPlayerExact(name)), core.queries());
        commands.register(new CosmeticCommand(catalog, cosmeticMenus, core.commands(), targets, new Replies(core.mainThread(), messages, plugin.getLogger()),
            new CosmeticProfileListener(profiles, plugin.getLogger())));
        return new CosmeticFeatures(shared, menu, new BedBreakCosmetics(new AnimationPlayer(plugin), animations, catalog, profiles),
            new WoodTypes(woodCatalog, woodProfiles::of), preview);
    }

    BedBreakCosmetics bedBreaks() {
        return bedBreaks;
    }

    MessageSets messageSets() {
        return shared.messageSets().orElseThrow();
    }

    KillMessages killMessages() {
        return shared.killMessages().orElseThrow();
    }

    WoodTypes woods() {
        return woods;
    }

    AuraCosmetics auras() {
        return shared.auras().orElseThrow();
    }

    TrailCosmetics trails() {
        return shared.trails().orElseThrow();
    }

    @Override
    public void victory(Player winner, List<Player> viewers, Area area) {
        shared.victory(winner, viewers, area);
    }

    @Override
    public void finalKill(Player killer, Player victim, TeamColor victimColor, List<Player> viewers) {
        shared.finalKill(killer, victim, victimColor, viewers);
    }

    void openMenu(Player player) {
        menu.open(player);
    }

    void onMissions(MissionMenus missions) {
        menu.onMissions(missions::open);
    }

    void onQuickBuy(QuickBuyEditor editor) {
        menu.onQuickBuy(editor::open);
    }

    void onHotbar(HotbarEditor editor) {
        menu.onHotbar(editor::openFromBedWarsMenu);
    }

    void onTeamColor(FavouriteColorMenu teamColor) {
        menu.cosmetics().onTeamColor(teamColor::open);
    }

    void onPreferences(PreferencesMenu preferences) {
        menu.onPreferences(preferences::open);
    }

    boolean previewing(Player player) {
        return preview != null && preview.previewing(player) || shared.previewing(player);
    }

    void stop() {
        if (preview != null) {
            preview.stop();
        }
        shared.stop();
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + path);
    }
}
