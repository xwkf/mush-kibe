package br.com.muxe.minigame.bedwars.stats.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record FindFavouriteMapsQuery(UUID playerId) implements Query<Set<String>> {

    public FindFavouriteMapsQuery {
        Objects.requireNonNull(playerId, "playerId");
    }
}
