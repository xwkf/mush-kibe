package br.com.muxe.minigame.bedwars.teamcolor.infrastructure.memory;

import br.com.muxe.minigame.bedwars.teamcolor.domain.FavouriteColorRepository;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryFavouriteColorRepository implements FavouriteColorRepository {

    private final Map<UUID, TeamColor> saved = new HashMap<>();
    private final Map<UUID, Instant> updatedAt = new HashMap<>();

    @Override
    public Optional<TeamColor> find(UUID playerId) {
        return Optional.ofNullable(saved.get(playerId));
    }

    @Override
    public void save(UUID playerId, TeamColor color, Instant at) {
        saved.put(playerId, color);
        updatedAt.put(playerId, at);
    }

    @Override
    public void clear(UUID playerId) {
        saved.remove(playerId);
        updatedAt.remove(playerId);
    }

    public Optional<Instant> updatedAt(UUID playerId) {
        return Optional.ofNullable(updatedAt.get(playerId));
    }
}
