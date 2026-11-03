package br.com.muxe.minigame.bedwars.hotbar.infrastructure.memory;

import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryHotbarRepository implements HotbarRepository {

    private final Map<UUID, HotbarLayout> saved = new HashMap<>();
    private final Map<UUID, Instant> updatedAt = new HashMap<>();

    @Override
    public Optional<HotbarLayout> find(UUID playerId) {
        return Optional.ofNullable(saved.get(playerId));
    }

    @Override
    public void save(UUID playerId, HotbarLayout layout, Instant at) {
        saved.put(playerId, layout);
        updatedAt.put(playerId, at);
    }

    public Optional<Instant> updatedAt(UUID playerId) {
        return Optional.ofNullable(updatedAt.get(playerId));
    }
}
