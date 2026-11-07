package br.com.muxe.minigame.api.cosmetic.aura;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class AuraAssignments {

    private final Map<UUID, Drawn> drawn = new HashMap<>();

    public Optional<Aura> of(UUID player, String room, Supplier<Optional<Aura>> draw) {
        Drawn existing = drawn.get(player);
        if (existing != null && existing.room().equals(room)) {
            return existing.aura();
        }
        Optional<Aura> aura = draw.get();
        drawn.put(player, new Drawn(room, aura));
        return aura;
    }

    public void retain(Set<UUID> players) {
        drawn.keySet().retainAll(players);
    }

    private record Drawn(String room, Optional<Aura> aura) {
    }
}
