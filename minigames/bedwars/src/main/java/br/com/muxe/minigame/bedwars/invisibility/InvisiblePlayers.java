package br.com.muxe.minigame.bedwars.invisibility;

import br.com.muxe.core.bukkit.nms.ArmorVisibility;
import br.com.muxe.minigame.bedwars.room.Room;
import br.com.muxe.minigame.bedwars.room.RoomPlayer;
import br.com.muxe.minigame.bedwars.room.RoomRuntime;
import br.com.muxe.minigame.bedwars.room.RoomService;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public final class InvisiblePlayers {

    public static final long PERIOD_TICKS = 2L;
    private static final double FOOTSTEP_HEIGHT = 0.01;
    private static final int FOOTSTEP_RADIUS = 32;
    private static final double GROUND_PROBE = 0.05;

    private final RoomService rooms;
    private final Map<UUID, Hidden> hidden = new HashMap<>();

    private InvisiblePlayers(RoomService rooms) {
        this.rooms = rooms;
    }

    public static InvisiblePlayers start(Plugin plugin, RoomService rooms) {
        InvisiblePlayers started = new InvisiblePlayers(rooms);
        plugin.getServer().getScheduler().runTaskTimer(plugin, started::update, PERIOD_TICKS, PERIOD_TICKS);
        return started;
    }

    private void update() {
        Set<UUID> invisible = new HashSet<>();
        for (RoomRuntime runtime : rooms.runtimes()) {
            if (runtime.room().phase() != Room.Phase.PLAYING) {
                continue;
            }
            List<Player> online = runtime.online();
            for (Player target : online) {
                Optional<RoomPlayer> player = runtime.room().player(target.getUniqueId()).filter(RoomPlayer::isAlive);
                if (player.isEmpty() || runtime.isSpectator(target) || !target.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    continue;
                }
                invisible.add(target.getUniqueId());
                hide(runtime, online, target, player.get().team());
            }
        }
        hidden.entrySet().removeIf(entry -> {
            if (invisible.contains(entry.getKey())) {
                return false;
            }
            Player target = Bukkit.getPlayer(entry.getKey());
            if (target != null) {
                entry.getValue().viewers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(viewer -> ArmorVisibility.show(viewer, target));
            }
            return true;
        });
    }

    private void hide(RoomRuntime runtime, List<Player> online, Player target, TeamColor team) {
        Hidden state = hidden.computeIfAbsent(target.getUniqueId(), ignored -> new Hidden(target.getLocation()));
        for (Player viewer : online) {
            Optional<TeamColor> viewerTeam = runtime.room().player(viewer.getUniqueId()).filter(RoomPlayer::isAlive).map(RoomPlayer::team);
            if (Invisibility.hidesArmor(viewer.getUniqueId(), viewerTeam, target.getUniqueId(), team)) {
                ArmorVisibility.hide(viewer, target);
                state.viewers.add(viewer.getUniqueId());
            }
        }
        Location at = target.getLocation();
        double moved = at.getWorld().equals(state.lastStep.getWorld()) ? at.distanceSquared(state.lastStep) : Double.MAX_VALUE;
        if (Invisibility.leavesFootstep(moved, at.clone().subtract(0, GROUND_PROBE, 0).getBlock().getType().isSolid())) {
            state.lastStep = at;
            target.getWorld().spigot().playEffect(at.clone().add(0, FOOTSTEP_HEIGHT, 0), Effect.FOOTSTEP, 0, 0, 0f, 0f, 0f, 0f, 1, FOOTSTEP_RADIUS);
        }
    }

    private static final class Hidden {

        private final Set<UUID> viewers = new HashSet<>();
        private Location lastStep;

        private Hidden(Location lastStep) {
            this.lastStep = lastStep;
        }
    }
}
