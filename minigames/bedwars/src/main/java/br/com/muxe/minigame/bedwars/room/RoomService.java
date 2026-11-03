package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.mission.config.MissionConfig;
import br.com.muxe.core.bukkit.npc.Npc;
import br.com.muxe.core.bukkit.display.DisplayService;
import br.com.muxe.core.bukkit.npc.NpcService;
import br.com.muxe.core.bukkit.permission.display.NameTag;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.minigame.bedwars.arena.BedWarsArena;
import br.com.muxe.minigame.bedwars.arena.BedWarsSchema;
import br.com.muxe.minigame.bedwars.lobby.LevelChat;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.preferences.PlayerPreferences;
import br.com.muxe.minigame.bedwars.matchmaking.RoomChoice;
import br.com.muxe.minigame.bedwars.mission.MissionRules;
import br.com.muxe.minigame.bedwars.network.ArenaInfo;
import br.com.muxe.minigame.bedwars.network.RoomInfo;
import br.com.muxe.minigame.bedwars.settings.BedWarsSettings;
import br.com.muxe.minigame.engine.arena.ArenaCatalog;
import br.com.muxe.minigame.engine.arena.LoadedArena;
import br.com.muxe.minigame.engine.level.LevelUps;
import br.com.muxe.minigame.engine.match.HealthDisplay;
import br.com.muxe.minigame.engine.match.StartCountdown;
import br.com.muxe.minigame.engine.room.RoomCodes;
import br.com.muxe.minigame.engine.team.TeamColor;
import br.com.muxe.minigame.engine.world.cell.Cell;
import br.com.muxe.minigame.engine.world.server.MatchWorld;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class RoomService {

    private static final Duration RESERVATION = Duration.ofSeconds(30);

    private final JavaPlugin plugin;
    private final MuxeCore core;
    private final BedWarsSettings settings;
    private final MessageCatalog messages;
    private final BedWarsSchema schema;
    private final ArenaCatalog catalog;
    private final MatchWorld matchWorld;
    private final RoomCodes codes = new RoomCodes(ThreadLocalRandom.current());
    private final Map<String, RoomRuntime> rooms = new LinkedHashMap<>();
    private final Map<UUID, String> byPlayer = new ConcurrentHashMap<>();
    private final RoomReservations reservations = new RoomReservations(RESERVATION);
    private final Map<String, List<String>> npcLines = new ConcurrentHashMap<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private BiConsumer<Player, NpcClick> npcClicks = (player, click) -> {
    };
    private BedBreakHandler bedBreaks = (breaker, world, bed, viewers) -> {
    };
    private DisplayService displays;
    private NpcService npcs;
    private MissionConfig missions = MissionConfig.empty();
    private MissionRules missionRules = MissionRules.DEFAULT;
    private volatile Map<UUID, ChatView> chat = Map.of();
    private final Set<UUID> shouting = ConcurrentHashMap.newKeySet();
    private final HealthDisplay healthDisplay;
    private final LevelUps levelUps;
    private final LevelBadges levelBadges;
    private PlayerPreferences preferences;
    private volatile int activeMatches;

    public RoomService(JavaPlugin plugin, MuxeCore core, BedWarsSettings settings, MessageCatalog messages, BedWarsSchema schema, ArenaCatalog catalog, MatchWorld matchWorld,
                       LevelBadges levelBadges) {
        this.plugin = plugin;
        this.core = core;
        this.settings = settings;
        this.messages = messages;
        this.healthDisplay = new HealthDisplay(messages.get("health-below-name"));
        this.levelUps = new LevelUps(messages.get("level-up"), levelBadges::of);
        this.levelBadges = levelBadges;
        this.schema = schema;
        this.catalog = catalog;
        this.matchWorld = matchWorld;
    }

    public void start() {
        matchWorld.start();
        npcs = NpcService.start(plugin, npc -> npcLines.getOrDefault(npc.id(), List.of()));
        npcs.onClick(this::clicked);
        displays = DisplayService.start(plugin);
        tasks.add(plugin.getServer().getScheduler().runTaskTimer(plugin, this::second, 20L, 20L));
        tasks.add(plugin.getServer().getScheduler().runTaskTimer(plugin, this::forgeCycle, 5L, 5L));
        core.nameTags().override(this::nameTag);
    }

    public void stop() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        for (RoomRuntime runtime : List.copyOf(rooms.values())) {
            runtime.close();
        }
        core.nameTags().clearOverride();
        if (npcs != null) {
            npcs.stop();
        }
        if (displays != null) {
            displays.stop();
        }
        matchWorld.stop();
    }

    public void onNpcClick(BiConsumer<Player, NpcClick> handler) {
        this.npcClicks = handler;
    }

    public void onBedBreak(BedBreakHandler handler) {
        this.bedBreaks = handler;
    }

    BedBreakHandler bedBreaks() {
        return bedBreaks;
    }

    public void arrive(Player player) {
        Instant now = core.clock().instant();
        for (RoomRuntime runtime : rooms.values()) {
            if (runtime.canReturn(player.getUniqueId(), now)) {
                byPlayer.put(player.getUniqueId(), runtime.room().code());
                runtime.rejoin(player);
                refreshChat();
                return;
            }
        }
        Optional<RoomReservations.Reservation> reserved = reservations.take(player.getUniqueId(), now);
        if (reserved.isPresent()) {
            join(player, reserved.get().mode(), reserved.get().arena(), Optional.of(reserved.get().code()));
            return;
        }
        join(player, settings.defaultMode());
    }

    public Optional<Reserved> reserve(UUID playerId, String mode, Optional<String> arena, Optional<String> code) {
        if (!schema.modes().contains(mode)) {
            return Optional.empty();
        }
        Instant now = core.clock().instant();
        reservations.take(playerId, now);
        Optional<Reserved> reserved = pick(mode, arena, code).map(found -> new Reserved(found.room().code(), false))
            .or(() -> create(mode, arena).map(created -> new Reserved(created.room().code(), true)));
        reserved.ifPresent(found -> reservations.hold(playerId, found.code(), mode, arena, now));
        return reserved;
    }

    public List<RoomInfo> summaries() {
        Instant now = core.clock().instant();
        return rooms.values().stream().map(runtime -> new RoomInfo(
            runtime.room().code(),
            runtime.room().mode().id(),
            runtime.room().arena().name(),
            runtime.room().acceptsPlayers(),
            runtime.room().players().size() + reservations.pending(runtime.room().code(), now),
            runtime.room().mode().maxPlayers()
        )).toList();
    }

    public List<ArenaInfo> arenas() {
        return catalog.all().stream().map(loaded -> new ArenaInfo(loaded.mode(), loaded.name(), loaded.displayName())).toList();
    }

    public void join(Player player, String mode) {
        join(player, mode, Optional.empty(), Optional.empty());
    }

    private void join(Player player, String mode, Optional<String> arena, Optional<String> code) {
        RoomRuntime runtime = pick(mode, arena, code).or(() -> create(mode, arena)).or(() -> pick(mode, Optional.empty(), Optional.empty())).orElse(null);
        if (runtime == null) {
            player.sendMessage(messages.get("no-arena", "mode", schema.modes().contains(mode) ? schema.mode(mode).displayName() : mode));
            return;
        }
        byPlayer.put(player.getUniqueId(), runtime.room().code());
        runtime.addWaiting(player);
        refreshChat();
    }

    public boolean forceStart(Player player) {
        return roomOf(player.getUniqueId()).map(runtime -> runtime.room().forceStart(StartCountdown.WHEN_FULL)).orElse(false);
    }

    public boolean forceEnd(Player player) {
        return roomOf(player.getUniqueId()).map(RoomRuntime::forceEnd).orElse(false);
    }

    public List<String> modes() {
        return schema.modes();
    }

    public void switchTo(Player player, String mode) {
        roomOf(player.getUniqueId()).ifPresent(runtime -> {
            if (runtime.room().phase() == Room.Phase.WAITING || runtime.room().phase() == Room.Phase.STARTING) {
                leave(player);
            }
        });
        if (roomOf(player.getUniqueId()).isEmpty()) {
            join(player, mode);
        }
    }

    public int activeMatches() {
        return activeMatches;
    }

    public void shout(Player player, String message) {
        String text = message.strip();
        if (text.isEmpty()) {
            return;
        }
        shouting.add(player.getUniqueId());
        try {
            player.chat(text.startsWith("/") ? " " + text : text);
        } finally {
            shouting.remove(player.getUniqueId());
        }
    }

    public boolean isShouting(UUID playerId) {
        return shouting.contains(playerId);
    }

    public Optional<ChatView> chatView(UUID playerId) {
        return Optional.ofNullable(chat.get(playerId));
    }

    void refreshChat() {
        Map<UUID, ChatView> views = new HashMap<>();
        for (RoomRuntime runtime : rooms.values()) {
            Set<UUID> online = runtime.online().stream().map(Player::getUniqueId).collect(Collectors.toSet());
            views.putAll(ChatView.of(runtime.room(), online));
        }
        chat = Map.copyOf(views);
    }

    public void leave(Player player) {
        levelUps.forget(player.getUniqueId());
        String code = byPlayer.get(player.getUniqueId());
        RoomRuntime runtime = code == null ? null : rooms.get(code);
        if (runtime == null) {
            byPlayer.remove(player.getUniqueId());
            return;
        }
        runtime.quit(player);
        boolean keep = runtime.room().player(player.getUniqueId()).map(RoomPlayer::state).filter(state -> state == RoomPlayer.State.DISCONNECTED).isPresent();
        if (!keep) {
            byPlayer.remove(player.getUniqueId());
        }
        refreshChat();
        core.nameTags().refresh(player);
    }

    public Optional<RoomRuntime> roomOf(UUID player) {
        String code = byPlayer.get(player);
        return code == null ? Optional.empty() : Optional.ofNullable(rooms.get(code));
    }

    public Optional<RoomRuntime> roomAt(Location location) {
        Optional<Cell> cell = matchWorld.cellAt(location);
        if (cell.isEmpty()) {
            return Optional.empty();
        }
        for (RoomRuntime runtime : rooms.values()) {
            if (runtime.room().cell().index() == cell.get().index()) {
                return Optional.of(runtime);
            }
        }
        return Optional.empty();
    }

    public boolean isMatchWorld(Location location) {
        return matchWorld.contains(location.getWorld());
    }

    public void missions(MissionConfig config, MissionRules rules) {
        this.missions = config;
        this.missionRules = rules;
    }

    MissionConfig missions() {
        return missions;
    }

    MissionRules missionRules() {
        return missionRules;
    }

    MuxeCore core() {
        return core;
    }

    BedWarsSettings settings() {
        return settings;
    }

    MessageCatalog messages() {
        return messages;
    }

    JavaPlugin plugin() {
        return plugin;
    }

    DisplayService displays() {
        return displays;
    }

    NpcService npcs() {
        return npcs;
    }

    Map<String, List<String>> npcLines() {
        return npcLines;
    }

    void closed(RoomRuntime runtime) {
        rooms.remove(runtime.room().code());
        byPlayer.values().removeIf(code -> code.equals(runtime.room().code()));
        try {
            matchWorld.release(runtime.room().cell());
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not release the cell of room " + runtime.room().code(), exception);
        }
    }

    private Optional<RoomRuntime> pick(String mode, Optional<String> arena, Optional<String> code) {
        return RoomChoice.pick(summaries(), mode, arena, code).map(info -> rooms.get(info.code()));
    }

    private Optional<RoomRuntime> create(String mode, Optional<String> wanted) {
        if (!schema.modes().contains(mode)) {
            return Optional.empty();
        }
        List<LoadedArena> arenas = catalog.ofMode(mode).stream().filter(loaded -> wanted.map(loaded.name()::equals).orElse(true)).toList();
        if (arenas.isEmpty()) {
            return Optional.empty();
        }
        LoadedArena loaded = arenas.get(ThreadLocalRandom.current().nextInt(arenas.size()));
        BedWarsArena arena;
        try {
            arena = BedWarsArena.from(loaded);
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Arena " + loaded.name() + " cannot be played", exception);
            return Optional.empty();
        }
        Cell cell = matchWorld.allocate(loaded.map());
        String code = codes.next(rooms.keySet());
        Room room = new Room(code, schema.mode(mode), arena, cell);
        RoomRuntime runtime = new RoomRuntime(this, room, matchWorld.world());
        rooms.put(code, runtime);
        return Optional.of(runtime);
    }

    private void second() {
        Instant now = core.clock().instant();
        reservations.purge(now);
        for (RoomRuntime runtime : List.copyOf(rooms.values())) {
            try {
                if (runtime.room().idle(reservations.pending(runtime.room().code(), now) > 0, settings.timings().emptyRoomSeconds())) {
                    runtime.close();
                    continue;
                }
                runtime.second();
            } catch (RuntimeException exception) {
                plugin.getLogger().log(Level.SEVERE, "Room " + runtime.room().code() + " failed and is being closed", exception);
                runtime.close();
            }
        }
        refreshChat();
        activeMatches = (int) rooms.values().stream()
            .filter(runtime -> runtime.room().phase() == Room.Phase.PLAYING || runtime.room().phase() == Room.Phase.ENDING)
            .count();
    }

    private void forgeCycle() {
        for (RoomRuntime runtime : rooms.values()) {
            runtime.forgeCycle();
            runtime.showHealth();
        }
    }

    HealthDisplay healthDisplay() {
        return healthDisplay;
    }

    LevelUps levelUps() {
        return levelUps;
    }

    public void preferences(PlayerPreferences preferences) {
        this.preferences = preferences;
    }

    PlayerPreferences preferences() {
        return preferences;
    }

    public String chatBadge(UUID playerId) {
        int level = roomOf(playerId).map(runtime -> runtime.chatLevel(playerId)).orElse(0);
        return LevelChat.badge(levelBadges, level, preferences.of(playerId).showLevel());
    }

    private void clicked(Player player, Npc npc) {
        roomOf(player.getUniqueId()).ifPresent(runtime -> {
            if (runtime.npcRoom(npc.id()).isEmpty() || runtime.room().phase() != Room.Phase.PLAYING) {
                return;
            }
            boolean upgrades = npc.id().contains("-melhorias");
            npcClicks.accept(player, new NpcClick(runtime, upgrades));
        });
    }

    private Optional<NameTag> nameTag(UUID id) {
        return roomOf(id)
            .filter(runtime -> runtime.room().phase() == Room.Phase.PLAYING || runtime.room().phase() == Room.Phase.ENDING)
            .flatMap(runtime -> runtime.room().player(id))
            .map(RoomPlayer::team)
            .map(RoomService::tag);
    }

    static NameTag tag(TeamColor team) {
        String name = String.format("mxbw%02d%s", team.ordinal(), team.id());
        return new NameTag(name.length() > NameTag.MAX_TEAM ? name.substring(0, NameTag.MAX_TEAM) : name,
            Colors.translate(team.color() + team.letter() + " " + team.color()));
    }

    public record NpcClick(RoomRuntime runtime, boolean upgrades) {
    }

    public record Reserved(String code, boolean created) {
    }


    @FunctionalInterface
    public interface BedBreakHandler {

        void broken(Player breaker, World world, BedFrame bed, List<Player> viewers);
    }
}
