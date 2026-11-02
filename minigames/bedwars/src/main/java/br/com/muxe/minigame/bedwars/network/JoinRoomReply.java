package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.DeliveryPolicy;
import br.com.muxe.core.common.messaging.packet.Packet;
import br.com.muxe.core.common.messaging.packet.PacketType;
import java.time.Duration;

public record JoinRoomReply(boolean accepted, String roomCode, boolean created) implements Packet {

    public static final PacketType<JoinRoomReply> TYPE = PacketType.of(
        "bedwars.join_reply",
        JoinRoomReply.class,
        PacketCodec.of(
            (packet, out) -> out.writeBoolean(packet.accepted()).writeString(packet.roomCode()).writeBoolean(packet.created()),
            in -> new JoinRoomReply(in.readBoolean(), in.readString(), in.readBoolean())
        ),
        DeliveryPolicy.standard().withMaxAge(Duration.ofSeconds(5))
    );

    public static JoinRoomReply refused() {
        return new JoinRoomReply(false, "", false);
    }

    public static JoinRoomReply reserved(String roomCode, boolean created) {
        return new JoinRoomReply(true, roomCode, created);
    }
}
