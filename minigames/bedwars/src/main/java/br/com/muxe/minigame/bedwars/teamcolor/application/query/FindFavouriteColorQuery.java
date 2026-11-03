package br.com.muxe.minigame.bedwars.teamcolor.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record FindFavouriteColorQuery(UUID playerId) implements Query<Optional<TeamColor>> {

    public FindFavouriteColorQuery {
        Objects.requireNonNull(playerId, "playerId");
    }
}
