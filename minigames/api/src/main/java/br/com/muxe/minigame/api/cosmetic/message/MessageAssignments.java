package br.com.muxe.minigame.api.cosmetic.message;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class MessageAssignments {

    private final Map<UUID, Drawn> drawn = new HashMap<>();

    public MessageSet of(UUID player, String room, Supplier<MessageSet> draw) {
        Drawn existing = drawn.get(player);
        if (existing != null && existing.room().equals(room)) {
            return existing.set();
        }
        MessageSet set = draw.get();
        drawn.put(player, new Drawn(room, set));
        return set;
    }

    public void forget(UUID player) {
        drawn.remove(player);
    }

    private record Drawn(String room, MessageSet set) {
    }
}
