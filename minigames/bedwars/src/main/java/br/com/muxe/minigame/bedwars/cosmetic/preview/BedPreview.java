package br.com.muxe.minigame.bedwars.cosmetic.preview;

import br.com.muxe.core.bukkit.buildmode.BuildMode;
import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer;
import br.com.muxe.core.bukkit.cosmetic.stage.AnimationPlayer.Playback;
import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.nms.BlockMask;
import br.com.muxe.core.bukkit.scoreboard.Sidebar;
import br.com.muxe.core.bukkit.text.ActionBar;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.animation.BedAnimations;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.engine.world.server.VoidGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
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

public final class BedPreview implements PreviewGate, Listener {

    private static final long TICKS_PER_SECOND = 20;
    private static final long CHUNKS_ARRIVED = 10;

    private final Plugin plugin;
    private final BuildMode buildMode;
    private final Optional<BlockMask> blocks;
    private final PreviewSettings settings;
    private final BedAnimations animations;
    private final AnimationPlayer animator;
    private final MessageCatalog messages;
    private final Map<UUID, Session> sessions = new HashMap<>();
    private final BedBreakers breakers = new BedBreakers();
    private final List<BedHalf> halves = new ArrayList<>();
    private World world;

    public BedPreview(Plugin plugin, BuildMode buildMode, Optional<BlockMask> blocks, PreviewSettings settings, BedAnimations animations, MessageCatalog messages) {
        this.plugin = plugin;
        this.buildMode = buildMode;
        this.blocks = blocks;
        this.settings = settings;
        this.animations = animations;
        this.animator = new AnimationPlayer(plugin);
        this.messages = messages;
    }

    @SuppressWarnings("deprecation")
    public boolean start() {
        if (!plugin.getServer().getWorldContainer().toPath().resolve(settings.world()).toFile().isDirectory()) {
            plugin.getLogger().warning("The bed preview is off: there is no world folder named " + settings.world());
            return false;
        }
        world = new WorldCreator(settings.world()).generator(new VoidGenerator()).generateStructures(false).createWorld();
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(true);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setTime(6000);
        world.setStorm(false);
        world.setThundering(false);
        for (Vec half : List.of(settings.bed().foot(), settings.bed().head())) {
            Block block = blockAt(half);
            if (block.getType() == Material.BED_BLOCK) {
                halves.add(new BedHalf(block.getLocation(), block.getData()));
            }
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return true;
    }

    public void stop() {
        List.copyOf(sessions.keySet()).forEach(id -> Optional.ofNullable(plugin.getServer().getPlayer(id)).ifPresent(viewer -> finish(viewer, false)));
    }

    public boolean previewing(Player viewer) {
        return sessions.containsKey(viewer.getUniqueId());
    }

    @Override
    public void preview(Player viewer, BedCosmetic cosmetic, Consumer<Player> back) {
        Optional<BedAnimation> animation = animations.find(cosmetic.id());
        if (world == null || animation.isEmpty()) {
            viewer.sendMessage(messages.get(world == null ? "preview-only-lobby" : "cosmetic-no-preview"));
            return;
        }
        if (previewing(viewer)) {
            viewer.sendMessage(messages.get("preview-busy"));
            return;
        }
        Session session = new Session(viewer, animation.get(), back);
        sessions.put(viewer.getUniqueId(), session);
        viewer.getInventory().clear();
        viewer.getInventory().setArmorContents(new ItemStack[4]);
        viewer.getInventory().setItem(settings.back().slot(), backItem());
        buildMode.releaseGameMode(viewer);
        viewer.setGameMode(GameMode.SURVIVAL);
        viewer.setAllowFlight(false);
        viewer.teleport(new Location(world, settings.x(), settings.y(), settings.z(), settings.yaw(), settings.pitch()));
        if (breakers.broken()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (previewing(viewer) && breakers.broken() && !breakers.breaker(viewer.getUniqueId())) {
                    showBed(viewer);
                }
            }, CHUNKS_ARRIVED);
        }
        for (Session other : sessions.values()) {
            if (other != session) {
                session.hide(other.viewer);
                other.hide(viewer);
            }
        }
        Sidebar sidebar = Sidebar.create(viewer);
        sidebar.title(settings.title());
        sidebar.lines(settings.lines(cosmetic.name()));
        String bar = Colors.translate(settings.actionBar());
        session.actionBar = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> ActionBar.send(viewer, bar), 0L, TICKS_PER_SECOND);
        session.timeout = plugin.getServer().getScheduler().runTaskLater(plugin, () -> finish(viewer, true), settings.timeout().toSeconds() * TICKS_PER_SECOND);
    }

    private ItemStack backItem() {
        Material material = Material.matchMaterial(settings.back().material());
        return ItemBuilder.of(material == null ? Material.BED : material).name(settings.back().name()).build();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }
        event.setCancelled(true);
        Action action = event.getAction();
        boolean rightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (settings.goesBack(rightClick, event.getPlayer().getInventory().getHeldItemSlot())) {
            finish(event.getPlayer(), true);
            return;
        }
        Block block = event.getClickedBlock();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || block == null || session.playback != null
            || !settings.isBed(block.getX(), block.getY(), block.getZ())) {
            return;
        }
        play(session);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }
        event.setCancelled(true);
        if (session.playback != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> hideBed(session.viewer));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player clicker && previewing(clicker)) {
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

    private void play(Session session) {
        Player viewer = session.viewer;
        session.actionBar.cancel();
        ActionBar.send(viewer, "");
        session.timeout.cancel();
        if (breakers.broke(viewer.getUniqueId())) {
            takeBed();
        }
        hideBed(viewer);
        plugin.getServer().getScheduler().runTask(plugin, () -> hideBed(viewer));
        Location at = viewer.getLocation();
        Vec breaker = new Vec(at.getX(), at.getY(), at.getZ());
        session.playback = animator.play(session.animation, world, settings.bed(), breaker, () -> List.of(viewer), () -> {
            hideBed(viewer);
            session.timeout = plugin.getServer().getScheduler().runTaskLater(plugin, () -> finish(viewer, true), Math.max(1, settings.returnDelay().toMillis() / 50));
        });
    }

    @SuppressWarnings("deprecation")
    private void hideBed(Player viewer) {
        if (!previewing(viewer)) {
            return;
        }
        for (Vec half : List.of(settings.bed().foot(), settings.bed().head())) {
            Location at = new Location(world, half.x(), half.y(), half.z());
            blocks.ifPresentOrElse(mask -> mask.hide(viewer, at, Material.AIR, (byte) 0), () -> viewer.sendBlockChange(at, Material.AIR, (byte) 0));
        }
    }

    private void finish(Player viewer, boolean reopen) {
        Session session = sessions.remove(viewer.getUniqueId());
        if (session == null) {
            return;
        }
        session.cancel();
        boolean lastBreaker = breakers.left(viewer.getUniqueId());
        for (Vec half : List.of(settings.bed().foot(), settings.bed().head())) {
            Block block = blockAt(half);
            blocks.ifPresentOrElse(mask -> mask.reveal(viewer, block.getLocation()), () -> sendBlock(viewer, block));
        }
        viewer.teleport(session.from);
        if (lastBreaker) {
            putBed();
        }
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

    @SuppressWarnings("deprecation")
    private static void sendBlock(Player viewer, Block block) {
        viewer.sendBlockChange(block.getLocation(), block.getType(), block.getData());
    }

    private Block blockAt(Vec half) {
        return world.getBlockAt((int) Math.floor(half.x()), (int) Math.floor(half.y()), (int) Math.floor(half.z()));
    }

    @SuppressWarnings("deprecation")
    private void takeBed() {
        List<Player> others = world.getPlayers().stream().filter(player -> !breakers.breaker(player.getUniqueId())).toList();
        if (blocks.isPresent()) {
            others.forEach(this::showBed);
        }
        for (BedHalf half : halves) {
            half.at().getBlock().setTypeIdAndData(Material.AIR.getId(), (byte) 0, false);
        }
        if (blocks.isEmpty()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> others.stream().filter(Player::isOnline).forEach(this::showBed));
        }
    }

    @SuppressWarnings("deprecation")
    private void showBed(Player viewer) {
        for (BedHalf half : halves) {
            blocks.ifPresentOrElse(mask -> mask.hide(viewer, half.at(), Material.BED_BLOCK, half.data()),
                () -> viewer.sendBlockChange(half.at(), Material.BED_BLOCK, half.data()));
        }
    }

    @SuppressWarnings("deprecation")
    private void putBed() {
        for (BedHalf half : halves) {
            half.at().getBlock().setTypeIdAndData(Material.BED_BLOCK.getId(), half.data(), false);
        }
        for (Player player : world.getPlayers()) {
            for (BedHalf half : halves) {
                blocks.ifPresentOrElse(mask -> mask.reveal(player, half.at()), () -> sendBlock(player, half.at().getBlock()));
            }
        }
    }

    private record BedHalf(Location at, byte data) {
    }

    private static final class Session {

        private final Player viewer;
        private final BedAnimation animation;
        private final Consumer<Player> back;
        private final Location from;
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final GameMode mode;
        private final boolean allowFlight;
        private final Scoreboard scoreboard;
        private final List<Player> hidden = new ArrayList<>();
        private BukkitTask actionBar;
        private BukkitTask timeout;
        private Playback playback;

        Session(Player viewer, BedAnimation animation, Consumer<Player> back) {
            this.viewer = viewer;
            this.animation = animation;
            this.back = back;
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
            if (actionBar != null) {
                actionBar.cancel();
            }
            if (timeout != null) {
                timeout.cancel();
            }
            if (playback != null) {
                playback.cancel();
            }
        }
    }
}
