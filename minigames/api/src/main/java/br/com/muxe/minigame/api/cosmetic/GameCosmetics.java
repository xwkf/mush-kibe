package br.com.muxe.minigame.api.cosmetic;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.core.bukkit.effect.SoundCue;
import br.com.muxe.core.bukkit.nms.PlayerSkins;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.config.live.LiveValue;
import br.com.muxe.minigame.api.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.api.cosmetic.aura.AuraCosmetics;
import br.com.muxe.minigame.api.cosmetic.aura.Auras;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticDefaults;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticKind;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticCategory;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.api.cosmetic.message.KillMessages;
import br.com.muxe.minigame.api.cosmetic.message.MessageDescriptions;
import br.com.muxe.minigame.api.cosmetic.message.MessagePreview;
import br.com.muxe.minigame.api.cosmetic.message.MessageSets;
import br.com.muxe.minigame.api.cosmetic.preview.KitPlay;
import br.com.muxe.minigame.api.cosmetic.preview.ScenePreview;
import br.com.muxe.minigame.api.cosmetic.preview.ScenePreviewSettings;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticProfileListener;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrails;
import br.com.muxe.minigame.api.cosmetic.trail.TrailCosmetics;
import br.com.muxe.minigame.api.cosmetic.trail.TrailPreview;
import br.com.muxe.minigame.api.runtime.MatchEvents;
import br.com.muxe.minigame.api.view.EndTitles;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class GameCosmetics implements MatchEvents {

    private final Map<CosmeticKind, Kind> kinds;
    private final Optional<SceneCosmetics> victories;
    private final Optional<SceneCosmetics> finalKills;
    private final Optional<AuraCosmetics> auras;
    private final Optional<TrailCosmetics> trails;
    private final Optional<MessageSets> messageSets;
    private final Optional<KillMessages> killMessages;
    private final Optional<MessageDescriptions> messageDescriptions;
    private final List<ScenePreview> previews;
    private final List<Runnable> stops;

    private GameCosmetics(Map<CosmeticKind, Kind> kinds, Optional<SceneCosmetics> victories, Optional<SceneCosmetics> finalKills, Optional<AuraCosmetics> auras,
                          Optional<TrailCosmetics> trails, Optional<MessageSets> messageSets, Optional<KillMessages> killMessages,
                          Optional<MessageDescriptions> messageDescriptions, List<ScenePreview> previews, List<Runnable> stops) {
        this.kinds = Map.copyOf(kinds);
        this.victories = victories;
        this.finalKills = finalKills;
        this.auras = auras;
        this.trails = trails;
        this.messageSets = messageSets;
        this.killMessages = killMessages;
        this.messageDescriptions = messageDescriptions;
        this.previews = List.copyOf(previews);
        this.stops = List.copyOf(stops);
    }

    public static GameCosmetics start(Setup setup) {
        CosmeticDefaults defaults = CosmeticDefaults.load();
        ConfigurationSection gameCosmetics = setup.config().getConfigurationSection("cosmetics");
        AnimationPlayer animator = new AnimationPlayer(setup.plugin());
        Map<CosmeticKind, Kind> kinds = new EnumMap<>(CosmeticKind.class);
        List<ScenePreview> previews = new ArrayList<>();
        List<Runnable> stops = new ArrayList<>();
        Predicate<Player> busy = player -> setup.busyElsewhere().test(player) || previews.stream().anyMatch(scene -> scene.previewing(player));
        PreviewGate onlyLobby = (player, cosmetic, back) -> player.sendMessage(setup.messages().get("preview-only-lobby"));
        PreviewGate withoutPreview = (player, cosmetic, back) -> {
        };

        Optional<SceneCosmetics> victories = Optional.empty();
        if (setup.kinds().contains(CosmeticKind.VICTORY_EFFECT)) {
            SceneAnimations animations = SceneAnimations.victories();
            Kind kind = kind(setup, defaults, gameCosmetics, CosmeticKind.VICTORY_EFFECT, id -> animations.find(id).isPresent());
            PreviewGate gate = setup.lobby() ? scenePreview(setup, CosmeticKind.VICTORY_EFFECT, animations, setup.titles()::victory, setup.titles()::stop,
                setup.countdown(), busy, previews) : onlyLobby;
            kinds.put(CosmeticKind.VICTORY_EFFECT, kind.with(gate));
            victories = Optional.of(new SceneCosmetics(animator, animations, kind.catalog(), kind.profiles()));
        }
        Optional<SceneCosmetics> finalKills = Optional.empty();
        if (setup.kinds().contains(CosmeticKind.FINAL_KILL)) {
            SceneAnimations animations = SceneAnimations.finalKills();
            Kind kind = kind(setup, defaults, gameCosmetics, CosmeticKind.FINAL_KILL, id -> animations.find(id).isPresent());
            PreviewGate gate = setup.lobby() ? scenePreview(setup, CosmeticKind.FINAL_KILL, animations, player -> {
            }, player -> {
            }, Optional.empty(), busy, previews) : onlyLobby;
            kinds.put(CosmeticKind.FINAL_KILL, kind.with(gate));
            finalKills = Optional.of(new SceneCosmetics(animator, animations, kind.catalog(), kind.profiles()));
        }
        Optional<AuraCosmetics> auras = Optional.empty();
        if (setup.kinds().contains(CosmeticKind.AURA)) {
            SceneAnimations animations = SceneAnimations.auras();
            Kind kind = kind(setup, defaults, gameCosmetics, CosmeticKind.AURA, id -> Auras.find(id).isPresent());
            PreviewGate gate = setup.lobby() ? scenePreview(setup, CosmeticKind.AURA, animations, player -> {
            }, player -> {
            }, Optional.empty(), busy, previews) : onlyLobby;
            kinds.put(CosmeticKind.AURA, kind.with(gate));
            auras = Optional.of(new AuraCosmetics(kind.catalog(), kind.profiles()::of));
        }
        Optional<TrailCosmetics> trails = Optional.empty();
        if (setup.kinds().contains(CosmeticKind.PROJECTILE_TRAIL)) {
            Kind kind = kind(setup, defaults, gameCosmetics, CosmeticKind.PROJECTILE_TRAIL, id -> ProjectileTrails.find(id).isPresent());
            PreviewGate gate = onlyLobby;
            if (setup.lobby()) {
                TrailPreview play = TrailPreview.start(setup.plugin(), ProjectileTrails::find);
                stops.add(play::stop);
                gate = kitPreview(setup, CosmeticKind.PROJECTILE_TRAIL, play, busy, previews);
            }
            kinds.put(CosmeticKind.PROJECTILE_TRAIL, kind.with(gate));
            trails = Optional.of(new TrailCosmetics(kind.catalog(), kind.profiles()::of, ProjectileTrails::find));
        }
        Optional<MessageSets> messageSets = Optional.empty();
        Optional<KillMessages> killMessages = Optional.empty();
        Optional<MessageDescriptions> descriptions = Optional.empty();
        if (setup.kinds().contains(CosmeticKind.KILL_MESSAGE)) {
            ConfigurationSection section = defaults.killMessages(setup.config().getConfigurationSection(CosmeticDefaults.KILL_MESSAGES));
            MessageSets sets = MessageSets.from(section);
            Kind kind = kind(setup, defaults, gameCosmetics, CosmeticKind.KILL_MESSAGE, id -> sets.find(id).isPresent());
            kinds.put(CosmeticKind.KILL_MESSAGE, kind.with(withoutPreview));
            messageSets = Optional.of(sets);
            killMessages = Optional.of(new KillMessages(sets, kind.catalog(), kind.profiles()::of));
            descriptions = Optional.of(new MessageDescriptions(sets, MessagePreview.from(section(section, "preview"))));
        }
        return new GameCosmetics(kinds, victories, finalKills, auras, trails, messageSets, killMessages, descriptions, previews, stops);
    }

    public static CosmeticProfiles profiles(JavaPlugin plugin, MuxeCore core, CosmeticCatalog catalog) {
        CosmeticProfiles profiles = new CosmeticProfiles(core.queries(), core.mainThread(), catalog, id -> plugin.getServer().getPlayer(id) != null);
        CosmeticProfileListener listener = new CosmeticProfileListener(profiles, plugin.getLogger());
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getServer().getOnlinePlayers().forEach(listener::load);
        return profiles;
    }

    public Optional<CosmeticCategory> category(CosmeticKind kind, CosmeticMenuSettings menus) {
        Kind found = kinds.get(kind);
        if (found == null || !menus.categories().containsKey(kind.key())) {
            return Optional.empty();
        }
        if (kind == CosmeticKind.KILL_MESSAGE && messageDescriptions.isPresent()) {
            return Optional.of(CosmeticCategory.of(menus.category(kind.key()), found.catalog(), found.profiles(), found.gate(), messageDescriptions.get()));
        }
        return Optional.of(CosmeticCategory.of(menus.category(kind.key()), found.catalog(), found.profiles(), found.gate()));
    }

    public Optional<CosmeticCatalog> catalog(CosmeticKind kind) {
        return Optional.ofNullable(kinds.get(kind)).map(Kind::catalog);
    }

    public Optional<AuraCosmetics> auras() {
        return auras;
    }

    public Optional<TrailCosmetics> trails() {
        return trails;
    }

    public Optional<MessageSets> messageSets() {
        return messageSets;
    }

    public Optional<KillMessages> killMessages() {
        return killMessages;
    }

    @Override
    public void victory(Player winner, List<Player> viewers, Area area) {
        victories.ifPresent(scenes -> scenes.play(winner, winner.getLocation(), SceneCosmetics.facing(winner.getLocation()), Optional.empty(),
            SceneCosmetics.riders(winner, viewers), viewers, Optional.of(area)));
    }

    @Override
    public void finalKill(Player killer, Player victim, TeamColor victimColor, List<Player> viewers) {
        finalKills.ifPresent(scenes -> {
            Location at = victim.getLocation();
            Optional<SignedSkin> skin = PlayerSkins.of(victim).map(found -> new SignedSkin(found.value(), found.signature()));
            scenes.playOnVictim(killer, at, SceneCosmetics.away(killer.getLocation(), at), skin, SceneCosmetics.dye(victimColor), viewers);
        });
    }

    public boolean previewing(Player player) {
        return previews.stream().anyMatch(scene -> scene.previewing(player));
    }

    public void stop() {
        previews.forEach(ScenePreview::stop);
        stops.forEach(Runnable::run);
    }

    private static Kind kind(Setup setup, CosmeticDefaults defaults, ConfigurationSection gameCosmetics, CosmeticKind kind, Predicate<String> animated) {
        CosmeticCatalog catalog = defaults.catalog(setup.game(), kind, gameCosmetics, animated);
        return new Kind(catalog, profiles(setup.plugin(), setup.core(), catalog), (player, cosmetic, back) -> {
        });
    }

    private static ScenePreview scenePreview(Setup setup, CosmeticKind kind, SceneAnimations animations, Consumer<Player> onScene, Consumer<Player> onLeave,
                                             Optional<SoundCue> countdownSound, Predicate<Player> busy, List<ScenePreview> previews) {
        String name = kind.previewSection().orElseThrow();
        LiveValue<ScenePreviewSettings> settings = new LiveValue<>(ScenePreviewSettings.from(section(setup.config(), name)));
        ScenePreview scene = new ScenePreview(setup.plugin(), setup.core().buildMode(), settings, animations, onScene, onLeave, countdownSound, busy, setup.messages());
        return started(setup, name, settings, scene, previews);
    }

    private static ScenePreview kitPreview(Setup setup, CosmeticKind kind, KitPlay play, Predicate<Player> busy, List<ScenePreview> previews) {
        String name = kind.previewSection().orElseThrow();
        LiveValue<ScenePreviewSettings> settings = new LiveValue<>(ScenePreviewSettings.from(section(setup.config(), name)));
        ScenePreview scene = new ScenePreview(setup.plugin(), setup.core().buildMode(), settings, play, busy, setup.messages());
        return started(setup, name, settings, scene, previews);
    }

    private static ScenePreview started(Setup setup, String name, LiveValue<ScenePreviewSettings> settings, ScenePreview scene, List<ScenePreview> previews) {
        scene.start();
        previews.add(scene);
        setup.reloads().register(name, name, root -> {
            settings.reload(() -> ScenePreviewSettings.from(section(root, name)));
            if (!scene.refresh()) {
                throw new IllegalStateException("there is no world folder named " + settings.get().world());
            }
        });
        return scene;
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + path);
    }

    public record Setup(JavaPlugin plugin, MuxeCore core, String game, Set<CosmeticKind> kinds, ConfigurationSection config, MessageCatalog messages,
                        boolean lobby, EndTitles titles, ConfigReloads reloads, Optional<SoundCue> countdown, Predicate<Player> busyElsewhere) {
    }

    private record Kind(CosmeticCatalog catalog, CosmeticProfiles profiles, PreviewGate gate) {

        Kind with(PreviewGate previews) {
            return new Kind(catalog, profiles, previews);
        }
    }
}
