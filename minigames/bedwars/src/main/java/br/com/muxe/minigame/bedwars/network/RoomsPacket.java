package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.DeliveryPolicy;
import br.com.muxe.core.common.messaging.packet.Packet;
import br.com.muxe.core.common.messaging.packet.PacketType;
import java.time.Duration;
import java.util.List;

public record RoomsPacket(String serverId, List<RoomInfo> rooms, List<ArenaInfo> arenas) implements Packet {

    public static final PacketType<RoomsPacket> TYPE = PacketType.of(
        "bedwars.rooms",
        RoomsPacket.class,
        PacketCodec.of(
            (packet, out) -> out
                .writeString(packet.serverId())
                .writeList(packet.rooms(), RoomInfo::write)
                .writeList(packet.arenas(), ArenaInfo::write),
            in -> new RoomsPacket(in.readString(), in.readList(RoomInfo::read), in.readList(ArenaInfo::read))
        ),
        DeliveryPolicy.volatileState(Duration.ofSeconds(10))
    );

    public RoomsPacket {
        rooms = List.copyOf(rooms);
        arenas = List.copyOf(arenas);
    }
}
