package br.com.muxe.minigame.bedwars.cosmetic.trail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class TrailAssignments {

    private final Map<UUID, Drawn> drawn = new HashMap<>();

    public Optional<ProjectileTrail> of(UUID player, String room, Supplier<Optional<ProjectileTrail>> draw) {
        Drawn existing = drawn.get(player);
        if (existing != null && existing.room().equals(room)) {
            return existing.trail();
        }
        Optional<ProjectileTrail> trail = draw.get();
        drawn.put(player, new Drawn(room, trail));
        return trail;
    }

    public void retain(Set<UUID> players) {
        drawn.keySet().retainAll(players);
    }

    public void forget(UUID player) {
        drawn.remove(player);
    }

    private record Drawn(String room, Optional<ProjectileTrail> trail) {
    }
}
