package br.com.muxe.minigame.bedwars.teamcolor.domain;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteColorRepository {

    Optional<TeamColor> find(UUID playerId);

    void save(UUID playerId, TeamColor color, Instant at);

    void clear(UUID playerId);
}
