package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.core.bukkit.text.MessageCatalog;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

final class RoomAnnouncements {

    private RoomAnnouncements() {
    }

    static String joined(MessageCatalog messages, String player, int players, int maxPlayers) {
        return messages.get("room-joined", "player", player, "players", String.valueOf(players), "max", String.valueOf(maxPlayers));
    }

    static Set<UUID> joinAudience(Collection<UUID> inRoom, UUID joiner) {
        Set<UUID> audience = new LinkedHashSet<>(inRoom);
        audience.add(joiner);
        return audience;
    }

    static String left(MessageCatalog messages, String player) {
        return messages.get("room-left", "player", player);
    }
}
