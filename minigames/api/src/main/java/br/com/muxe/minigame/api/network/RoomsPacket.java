package br.com.muxe.minigame.api.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.Packet;
import java.util.List;

public record RoomsPacket(String serverId, List<RoomInfo> rooms, List<ArenaInfo> arenas) implements Packet {

    public static final PacketCodec<RoomsPacket> CODEC = PacketCodec.of(
        (packet, out) -> out
            .writeString(packet.serverId())
            .writeList(packet.rooms(), RoomInfo::write)
            .writeList(packet.arenas(), ArenaInfo::write),
        in -> new RoomsPacket(in.readString(), in.readList(RoomInfo::read), in.readList(ArenaInfo::read))
    );

    public RoomsPacket {
        rooms = List.copyOf(rooms);
        arenas = List.copyOf(arenas);
    }
}
