package br.com.muxe.minigame.bedwars.matchmaking;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.bukkit.text.MessageCatalog;
import br.com.muxe.core.common.messaging.channel.PacketTarget;
import br.com.muxe.minigame.bedwars.network.BedWarsPackets;
import br.com.muxe.minigame.bedwars.network.JoinRoomReply;
import br.com.muxe.minigame.bedwars.network.JoinRoomRequest;
import br.com.muxe.minigame.bedwars.network.RoomsPacket;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.bukkit.entity.Player;

public final class MatchConnector {

    public static final Duration DIRECTORY_TIME_TO_LIVE = Duration.ofSeconds(10);

    private final MuxeCore core;
    private final MessageCatalog messages;
    private final RoomDirectory directory;
    private final Function<String, String> modeNames;
    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();

    public MatchConnector(MuxeCore core, MessageCatalog messages, Function<String, String> modeNames) {
        this.core = core;
        this.messages = messages;
        this.modeNames = modeNames;
        this.directory = new RoomDirectory(core.clock(), DIRECTORY_TIME_TO_LIVE);
    }

    public void start() {
        BedWarsPackets.registerAll(core.packetRegistry());
        core.packets().subscribe(RoomsPacket.TYPE, context -> directory.update(context.packet()));
    }

    public RoomDirectory directory() {
        return directory;
    }

    public void join(Player player, String mode, Optional<String> arena) {
        UUID id = player.getUniqueId();
        Optional<Matchmaker.Target> target = Matchmaker.plan(directory.alive(), mode, arena);
        if (target.isEmpty()) {
            player.sendMessage(messages.get("room-unavailable", "mode", modeNames.apply(mode)));
            return;
        }
        if (!pending.add(id)) {
            return;
        }
        JoinNotices.beforeRequest(target.get()).forEach(key -> player.sendMessage(messages.get(key)));
        JoinRoomRequest request = new JoinRoomRequest(id, mode, arena.orElse(""), target.get().roomCode());
        core.packets().request(PacketTarget.server(target.get().serverId()), request, JoinRoomReply.class, core.requestTimeout())
            .whenCompleteAsync((reply, error) -> {
                pending.remove(id);
                if (!player.isOnline()) {
                    return;
                }
                if (error != null || reply == null || !reply.accepted()) {
                    player.sendMessage(messages.get("room-unavailable", "mode", modeNames.apply(mode)));
                    return;
                }
                JoinNotices.afterReply(reply).forEach(key -> player.sendMessage(messages.get(key, "room", reply.roomCode())));
                core.network().connectToServer(id, target.get().serverId()).exceptionally(failure -> {
                    core.mainThread().execute(() -> player.sendMessage(messages.get("room-unavailable", "mode", modeNames.apply(mode))));
                    return null;
                });
            }, core.mainThread());
    }
}
