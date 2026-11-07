package br.com.muxe.minigame.api.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.Packet;
import java.util.Optional;
import java.util.UUID;

public record JoinRoomRequest(UUID playerId, String mode, String arena, String roomCode) implements Packet {

    public static final PacketCodec<JoinRoomRequest> CODEC = PacketCodec.of(
        (packet, out) -> out.writeUuid(packet.playerId()).writeString(packet.mode()).writeString(packet.arena()).writeString(packet.roomCode()),
        in -> new JoinRoomRequest(in.readUuid(), in.readString(), in.readString(), in.readString())
    );

    public Optional<String> wantedArena() {
        return arena.isBlank() ? Optional.empty() : Optional.of(arena);
    }

    public Optional<String> wantedRoom() {
        return roomCode.isBlank() ? Optional.empty() : Optional.of(roomCode);
    }
}
