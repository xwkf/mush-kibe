package br.com.muxe.minigame.engine.arena;

import br.com.muxe.core.common.messaging.codec.PacketCodec;
import br.com.muxe.core.common.messaging.packet.DeliveryPolicy;
import br.com.muxe.core.common.messaging.packet.Packet;
import br.com.muxe.core.common.messaging.packet.PacketRegistry;
import br.com.muxe.core.common.messaging.packet.PacketType;
import java.util.Objects;

public record ArenaSavedPacket(String game, String name) implements Packet {

    public static final PacketType<ArenaSavedPacket> TYPE = PacketType.of(
        "minigame.arena_saved",
        ArenaSavedPacket.class,
        PacketCodec.of(
            (packet, out) -> out.writeString(packet.game()).writeString(packet.name()),
            in -> new ArenaSavedPacket(in.readString(), in.readString())
        ),
        DeliveryPolicy.standard()
    );

    public ArenaSavedPacket {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(name, "name");
    }

    public static PacketRegistry register(PacketRegistry registry) {
        if (registry.find(TYPE.id(), 1).isEmpty()) {
            registry.register(TYPE);
        }
        return registry;
    }
}
