package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.core.bukkit.text.MessageCatalog;

final class RoomAnnouncements {

    private RoomAnnouncements() {
    }

    static String joined(MessageCatalog messages, String player, int players, int maxPlayers) {
        return messages.get("room-joined", "player", player, "players", String.valueOf(players), "max", String.valueOf(maxPlayers));
    }

    static String left(MessageCatalog messages, String player) {
        return messages.get("room-left", "player", player);
    }
}
