package br.com.muxe.minigame.bedwars.lobby;

import br.com.muxe.core.bukkit.nms.PlayerSkins;
import br.com.muxe.core.bukkit.npc.Npc;
import br.com.muxe.core.bukkit.npc.NpcLabelLayout;
import br.com.muxe.core.bukkit.npc.NpcService;
import br.com.muxe.minigame.bedwars.lobby.format.LevelBadges;
import br.com.muxe.minigame.bedwars.lobby.settings.LobbyNpc;
import br.com.muxe.minigame.bedwars.lobby.settings.StatsNpcSettings;
import br.com.muxe.minigame.bedwars.matchmaking.RoomDirectory;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class LobbyNpcs implements Listener {

    public static final String STATS_PREFIX = "estatisticas:";
    public static final String STATS_ACTION = "estatisticas";
    public static final long BLINK_MILLIS = 500;

    private final Plugin plugin;
    private final Map<String, LobbyNpc> npcs;
    private final List<String> blinkColors;
    private final StatsNpcSettings statsNpc;
    private final NpcLabelLayout labels;
    private final RoomDirectory directory;
    private final StatsCache stats;
    private final LevelBadges badges;
    private final BiConsumer<Player, String> actions;
    private NpcService service;

    public LobbyNpcs(Plugin plugin, List<LobbyNpc> npcs, NpcLabelLayout labels, List<String> blinkColors, StatsNpcSettings statsNpc, RoomDirectory directory,
                     StatsCache stats, LevelBadges badges, BiConsumer<Player, String> actions) {
        this.labels = labels;
        this.plugin = plugin;
        this.npcs = npcs.stream().collect(Collectors.toMap(LobbyNpc::id, npc -> npc, (first, second) -> first, LinkedHashMap::new));
        this.blinkColors = List.copyOf(blinkColors);
        this.statsNpc = statsNpc;
        this.directory = directory;
        this.stats = stats;
        this.badges = badges;
        this.actions = actions;
    }

    public void start() {
        service = NpcService.start(plugin, npc -> npc.id().startsWith(STATS_PREFIX) ? statsNpc.labels() : labels, this::lines);
        service.visibleTo((viewer, npc) -> !npc.id().startsWith(STATS_PREFIX) || npc.id().equals(STATS_PREFIX + viewer.getUniqueId()));
        service.onClick(this::clicked);
        for (LobbyNpc npc : npcs.values()) {
            service.put(new Npc(npc.id(), npc.spot().toLocation(plugin.getServer()), npc.skin()));
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getOnlinePlayers().forEach(this::addStatsNpc);
    }

    public void stop() {
        if (service != null) {
            service.stop();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        addStatsNpc(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.remove(STATS_PREFIX + event.getPlayer().getUniqueId());
    }

    private void addStatsNpc(Player player) {
        service.put(new Npc(STATS_PREFIX + player.getUniqueId(), statsNpc.spot().toLocation(plugin.getServer()), PlayerSkins.of(player)));
    }

    private List<String> lines(Npc npc) {
        if (npc.id().startsWith(STATS_PREFIX)) {
            UUID owner = UUID.fromString(npc.id().substring(STATS_PREFIX.length()));
            return LobbyLines.stats(statsNpc.lines(), stats.of(owner).general(), badges, statsNpc.barSegments());
        }
        LobbyNpc lobbyNpc = npcs.get(npc.id());
        if (lobbyNpc == null) {
            return List.of();
        }
        int players = directory.playersIn(Set.copyOf(lobbyNpc.players()));
        return LobbyLines.npc(lobbyNpc.lines(), players, LobbyLines.blink(blinkColors, System.currentTimeMillis(), BLINK_MILLIS));
    }

    private void clicked(Player player, Npc npc) {
        if (npc.id().startsWith(STATS_PREFIX)) {
            actions.accept(player, STATS_ACTION);
            return;
        }
        Optional.ofNullable(npcs.get(npc.id())).ifPresent(lobbyNpc -> actions.accept(player, lobbyNpc.action()));
    }
}
