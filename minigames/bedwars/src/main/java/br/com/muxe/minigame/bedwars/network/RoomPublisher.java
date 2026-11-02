package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.core.common.messaging.channel.PacketTarget;
import br.com.muxe.minigame.bedwars.room.RoomService;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class RoomPublisher {

    public static final long PERIOD_TICKS = 40L;

    private final Plugin plugin;
    private final MuxeCore core;
    private final RoomService rooms;
    private final String lobbyGroup;
    private BukkitTask task;

    public RoomPublisher(Plugin plugin, MuxeCore core, RoomService rooms, String lobbyGroup) {
        this.plugin = plugin;
        this.core = core;
        this.rooms = rooms;
        this.lobbyGroup = lobbyGroup;
    }

    public void start() {
        BedWarsPackets.registerAll(core.packetRegistry());
        core.packets().subscribe(JoinRoomRequest.TYPE, context -> {
            JoinRoomRequest request = context.packet();
            CompletableFuture.supplyAsync(() -> rooms.reserve(request.playerId(), request.mode(), request.wantedArena(), request.wantedRoom()), core.mainThread())
                .thenAccept(code -> context.reply(code.map(found -> new JoinRoomReply(true, found)).orElse(JoinRoomReply.refused())))
                .exceptionally(error -> {
                    plugin.getLogger().log(Level.WARNING, "Could not reserve a room for " + request.playerId(), error);
                    context.reply(JoinRoomReply.refused());
                    return null;
                });
        });
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::publish, PERIOD_TICKS, PERIOD_TICKS);
    }

    public void stop() {
        Optional.ofNullable(task).ifPresent(BukkitTask::cancel);
    }

    private void publish() {
        RoomsPacket packet = new RoomsPacket(core.identity().id(), rooms.summaries(), rooms.arenas());
        core.packets().publish(PacketTarget.group(lobbyGroup), packet).exceptionally(error -> {
            plugin.getLogger().log(Level.FINE, "Could not publish the rooms", error);
            return null;
        });
    }
}
