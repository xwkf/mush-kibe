package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.DeliveryPolicy;
import br.com.muxe.core.common.messaging.packet.Packet;
import br.com.muxe.core.common.messaging.packet.PacketType;
import java.time.Duration;

public record JoinRoomReply(boolean accepted, String roomCode) implements Packet {

    public static final PacketType<JoinRoomReply> TYPE = PacketType.of(
        "bedwars.join_reply",
        JoinRoomReply.class,
        PacketCodec.of(
            (packet, out) -> out.writeBoolean(packet.accepted()).writeString(packet.roomCode()),
            in -> new JoinRoomReply(in.readBoolean(), in.readString())
        ),
        DeliveryPolicy.standard().withMaxAge(Duration.ofSeconds(5))
    );

    public static JoinRoomReply refused() {
        return new JoinRoomReply(false, "");
    }
}
