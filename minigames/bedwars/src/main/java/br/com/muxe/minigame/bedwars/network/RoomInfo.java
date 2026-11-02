package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.codec.PacketReader;
import br.com.muxe.core.common.messaging.codec.PacketWriter;

public record RoomInfo(String code, String mode, String arena, boolean open, int players, int maxPlayers) {

    public boolean hasRoom() {
        return open && players < maxPlayers;
    }

    static void write(PacketWriter out, RoomInfo room) {
        out.writeString(room.code())
            .writeString(room.mode())
            .writeString(room.arena())
            .writeBoolean(room.open())
            .writeVarInt(room.players())
            .writeVarInt(room.maxPlayers());
    }

    static RoomInfo read(PacketReader in) {
        return new RoomInfo(in.readString(), in.readString(), in.readString(), in.readBoolean(), in.readVarInt(), in.readVarInt());
    }
}
