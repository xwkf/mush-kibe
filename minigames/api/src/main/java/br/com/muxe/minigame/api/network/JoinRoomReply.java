package br.com.muxe.minigame.api.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.Packet;

public record JoinRoomReply(boolean accepted, String roomCode, boolean created) implements Packet {

    public static final PacketCodec<JoinRoomReply> CODEC = PacketCodec.of(
        (packet, out) -> out.writeBoolean(packet.accepted()).writeString(packet.roomCode()).writeBoolean(packet.created()),
        in -> new JoinRoomReply(in.readBoolean(), in.readString(), in.readBoolean())
    );

    public static JoinRoomReply refused() {
        return new JoinRoomReply(false, "", false);
    }

    public static JoinRoomReply reserved(String roomCode, boolean created) {
        return new JoinRoomReply(true, roomCode, created);
    }
}
