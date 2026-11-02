package br.com.muxe.minigame.bedwars.network;

import br.com.muxe.core.common.messaging.codec.PacketReader;
import br.com.muxe.core.common.messaging.codec.PacketWriter;

public record ArenaInfo(String mode, String name, String displayName) {

    static void write(PacketWriter out, ArenaInfo arena) {
        out.writeString(arena.mode()).writeString(arena.name()).writeString(arena.displayName());
    }

    static ArenaInfo read(PacketReader in) {
        return new ArenaInfo(in.readString(), in.readString(), in.readString());
    }
}
