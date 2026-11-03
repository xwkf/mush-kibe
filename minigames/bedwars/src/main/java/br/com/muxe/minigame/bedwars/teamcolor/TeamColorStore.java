package br.com.muxe.minigame.bedwars.teamcolor;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TeamColorStore {

    Optional<TeamColor> of(UUID playerId);

    void change(UUID playerId, Optional<TeamColor> color);

    default Map<UUID, TeamColor> preferred(Collection<UUID> playerIds) {
        Map<UUID, TeamColor> preferred = new LinkedHashMap<>();
        playerIds.forEach(playerId -> of(playerId).ifPresent(color -> preferred.put(playerId, color)));
        return preferred;
    }
}
