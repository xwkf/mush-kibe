package br.com.muxe.minigame.bedwars.cosmetic.preview;

import br.com.muxe.core.bukkit.buildmode.BuildMode;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer.Playback;
import br.com.muxe.core.bukkit.effect.SoundCue;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.nms.PlayerSkins;
import br.com.muxe.core.bukkit.npc.Npc;
import br.com.muxe.core.bukkit.npc.NpcService;
import br.com.muxe.core.bukkit.npc.NpcSkin;
import br.com.muxe.core.bukkit.scoreboard.Sidebar;
import br.com.muxe.core.bukkit.text.ActionBar;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.bukkit.text.Title;
import br.com.muxe.minigame.bedwars.cosmetic.animation.SceneAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.engine.world.server.VoidGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

public final class ScenePreview implements PreviewGate, Listener {

    private static final long TICKS_PER_SECOND = 20;
    private static final int COUNTDOWN_STAY = 40;
    private static final String SUBTITLE = " ";
    private static final String TARGET_PREFIX = "previa-alvo:";

    private final Plugin plugin;
    private final BuildMode buildMode;
    private final ScenePreviewSettings settings;
    private final SceneAnimations animations;
    private final Consumer<Player> onScene;
    private final Optional<SoundCue> countdownSound;
    private final Predicate<Player> busyElsewhere;
    private final AnimationPlayer animator;
    private final MessageCatalog messages;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private World world;
    private NpcService targets;
    private final Map<String, String> targetNames = new HashMap<>();

    public ScenePreview(Plugin plugin, BuildMode buildMode, ScenePreviewSettings settings, SceneAnimations animations, Consumer<Player> onScene,
                        Optional<SoundCue> countdownSound, Predicate<Player> busyElsewhere, MessageCatalog messages) {
        this.plugin = plugin;
        this.buildMode = buildMode;
        this.settings = settings;
        this.animations = animations;
        this.onScene = onScene;
        this.countdownSound = countdownSound;
        this.busyElsewhere = busyElsewhere;
        this.animator = new AnimationPlayer(plugin);
        this.messages = messages;
    }

    public boolean start() {
        if (!plugin.getServer().getWorldContainer().toPath().resolve(settings.world()).toFile().isDirectory()) {
            plugin.getLogger().warning("The preview is off: there is no world folder named " + settings.world());
            return false;
        }
        world = new WorldCreator(settings.world()).generator(new VoidGenerator()).generateStructures(false).createWorld();
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(true);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doFireTick", "false");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        if (settings.target().isPresent()) {
            targets = NpcService.start(plugin, npc -> Optional.ofNullable(targetNames.get(npc.id())).map(List::of).orElse(List.of()));
            targets.visibleTo((viewer, npc) -> npc.id().equals(TARGET_PREFIX + viewer.getUniqueId()));
            targets.onClick(this::hit);
        }
        return true;
    }

    public void stop() {
        List.copyOf(sessions.keySet()).forEach(id -> Optional.ofNullable(plugin.getServer().getPlayer(id)).ifPresent(viewer -> finish(viewer, false)));
        if (targets != null) {
            targets.stop();
        }
    }

    public boolean previewing(Player viewer) {
        return sessions.containsKey(viewer.getUniqueId());
    }

    @Override
    public void preview(Player viewer, BedCosmetic cosmetic, Consumer<Player> back) {
        Optional<SceneAnimation> animation = animations.find(cosmetic.id());
        if (world == null || animation.isEmpty()) {
            viewer.sendMessage(messages.get(world == null ? "preview-only-lobby" : "cosmetic-no-preview"));
            return;
        }
        if (previewing(viewer) || busyElsewhere.test(viewer)) {
            viewer.sendMessage(messages.get("preview-busy"));
            return;
        }
        Session session = new Session(viewer, back, animation.get());
        sessions.put(viewer.getUniqueId(), session);
        viewer.getInventory().clear();
        viewer.getInventory().setArmorContents(new ItemStack[4]);
        viewer.getInventory().setItem(settings.back().slot(), backItem());
        buildMode.releaseGameMode(viewer);
        viewer.setGameMode(GameMode.ADVENTURE);
        viewer.setAllowFlight(false);
        viewer.teleport(new Location(world, settings.x(), settings.y(), settings.z(), settings.yaw(), settings.pitch()));
        for (Session other : sessions.values()) {
            if (other != session) {
                session.hide(other.viewer);
                other.hide(viewer);
            }
        }
        Sidebar sidebar = Sidebar.create(viewer);
        sidebar.title(settings.title());
        sidebar.lines(settings.lines(cosmetic.name()));
        session.timeout = plugin.getServer().getScheduler().runTaskLater(plugin, () -> finish(viewer, true), settings.timeout().toSeconds() * TICKS_PER_SECOND);
        if (settings.target().isPresent() && targets != null) {
            placeTarget(session, settings.target().get());
            return;
        }
        for (int index = 0; index < settings.countdown().size(); index++) {
            String text = Colors.translate(settings.countdown().get(index));
            session.tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Title.builder().title(text).subtitle(SUBTITLE).timings(0, COUNTDOWN_STAY, 0).build().send(viewer);
                countdownSound.ifPresent(cue -> cue.play(viewer));
            }, (long) index * settings.countdownTicks()));
        }
        session.tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> play(session, settings.origin()), Math.max(1, settings.sceneStart())));
    }

    private void placeTarget(Session session, ScenePreviewSettings.Target target) {
        Vec at = settings.targetAt();
        Location spot = new Location(world, at.x(), at.y(), at.z(), settings.yaw() + 180, 0);
        session.target = TARGET_PREFIX + session.viewer.getUniqueId();
        targetNames.put(session.target, target.name(ThreadLocalRandom.current().nextInt()));
        targets.put(new Npc(session.target, spot, skin(session.viewer)));
        Material weapon = Material.matchMaterial(target.weapon());
        if (weapon != null) {
            session.viewer.getInventory().setItem(0, new ItemStack(weapon));
            session.viewer.getInventory().setHeldItemSlot(0);
        }
        String bar = Colors.translate(target.actionBar());
        session.tasks.add(plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (session.target != null) {
                ActionBar.send(session.viewer, bar);
            }
        }, 0L, TICKS_PER_SECOND));
    }

    private Optional<NpcSkin> skin(Player viewer) {
        List<Player> others = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        others.remove(viewer);
        Collections.shuffle(others);
        for (Player other : others) {
            Optional<NpcSkin> skin = PlayerSkins.of(other);
            if (skin.isPresent()) {
                return skin;
            }
        }
        return PlayerSkins.of(viewer);
    }

    private void hit(Player player, Npc npc) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null || session.playback != null || !npc.id().equals(session.target)) {
            return;
        }
        targets.remove(session.target);
        targetNames.remove(session.target);
        session.target = null;
        ActionBar.send(player, "");
        player.playSound(npc.location(), Sound.HURT_FLESH, 1, 1);
        play(session, settings.targetAt());
    }

    private void play(Session session, Vec origin) {
        Player viewer = session.viewer;
        if (!previewing(viewer)) {
            return;
        }
        onScene.accept(viewer);
        session.playback = animator.play(session.animation, world, origin, settings.facing(), () -> List.of(viewer), () ->
            session.tasks.add(plugin.getServer().getScheduler().runTaskLater(plugin, () -> finish(viewer, true), Math.max(1, settings.returnDelay().toMillis() / 50))));
    }

    private ItemStack backItem() {
        Material material = Material.matchMaterial(settings.back().material());
        return ItemBuilder.of(material == null ? Material.BED : material).name(settings.back().name()).build();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!previewing(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        Action action = event.getAction();
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (settings.goesBack(rightClick, event.getPlayer().getInventory().getHeldItemSlot())) {
            finish(event.getPlayer(), true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        if (previewing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        if (previewing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player clicker && previewing(clicker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && previewing(victim)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player hungry && previewing(hungry)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (previewing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(PlayerPickupItemEvent event) {
        if (previewing(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (previewing(event.getPlayer())) {
            finish(event.getPlayer(), false);
        }
    }

    private void finish(Player viewer, boolean reopen) {
        Session session = sessions.remove(viewer.getUniqueId());
        if (session == null) {
            return;
        }
        session.cancel();
        if (session.target != null && targets != null) {
            targets.remove(session.target);
            targetNames.remove(session.target);
        }
        Title.clear(viewer);
        viewer.teleport(session.from);
        viewer.getInventory().setContents(session.contents);
        viewer.getInventory().setArmorContents(session.armor);
        viewer.setGameMode(session.mode);
        buildMode.lockGameMode(viewer);
        viewer.setAllowFlight(session.allowFlight);
        viewer.setScoreboard(session.scoreboard);
        viewer.updateInventory();
        for (Session other : sessions.values()) {
            other.unhide(viewer);
        }
        session.restoreHidden();
        if (reopen) {
            session.back.accept(viewer);
        }
    }

    private static final class Session {

        private final Player viewer;
        private final Consumer<Player> back;
        private final SceneAnimation animation;
        private final Location from;
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final GameMode mode;
        private final boolean allowFlight;
        private final Scoreboard scoreboard;
        private final List<Player> hidden = new ArrayList<>();
        private final List<BukkitTask> tasks = new ArrayList<>();
        private BukkitTask timeout;
        private Playback playback;
        private String target;

        Session(Player viewer, Consumer<Player> back, SceneAnimation animation) {
            this.viewer = viewer;
            this.back = back;
            this.animation = animation;
            this.from = viewer.getLocation();
            this.contents = viewer.getInventory().getContents();
            this.armor = viewer.getInventory().getArmorContents();
            this.mode = viewer.getGameMode();
            this.allowFlight = viewer.getAllowFlight();
            this.scoreboard = viewer.getScoreboard();
        }

        void hide(Player other) {
            if (viewer.canSee(other)) {
                viewer.hidePlayer(other);
                hidden.add(other);
            }
        }

        void unhide(Player other) {
            if (hidden.remove(other) && other.isOnline()) {
                viewer.showPlayer(other);
            }
        }

        void restoreHidden() {
            for (Player other : hidden) {
                if (other.isOnline()) {
                    viewer.showPlayer(other);
                }
            }
            hidden.clear();
        }

        void cancel() {
            tasks.forEach(BukkitTask::cancel);
            if (timeout != null) {
                timeout.cancel();
            }
            if (playback != null) {
                playback.cancel();
            }
        }
    }
}
