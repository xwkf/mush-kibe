package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.packet.PacketRegistry;

public final class BedWarsPackets {

    private BedWarsPackets() {
    }

    public static void registerAll(PacketRegistry registry) {
        registry.register(RoomsPacket.TYPE).register(JoinRoomRequest.TYPE).register(JoinRoomReply.TYPE);
    }
}
