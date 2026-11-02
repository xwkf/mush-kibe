package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.DeliveryPolicy;
import br.com.muxe.core.common.messaging.packet.Packet;
import br.com.muxe.core.common.messaging.packet.PacketType;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public record JoinRoomRequest(UUID playerId, String mode, String arena, String roomCode) implements Packet {

    public static final PacketType<JoinRoomRequest> TYPE = PacketType.of(
        "bedwars.join_request",
        JoinRoomRequest.class,
        PacketCodec.of(
            (packet, out) -> out.writeUuid(packet.playerId()).writeString(packet.mode()).writeString(packet.arena()).writeString(packet.roomCode()),
            in -> new JoinRoomRequest(in.readUuid(), in.readString(), in.readString(), in.readString())
        ),
        DeliveryPolicy.standard().withMaxAge(Duration.ofSeconds(5))
    );

    public Optional<String> wantedArena() {
        return arena.isBlank() ? Optional.empty() : Optional.of(arena);
    }

    public Optional<String> wantedRoom() {
        return roomCode.isBlank() ? Optional.empty() : Optional.of(roomCode);
    }
}
