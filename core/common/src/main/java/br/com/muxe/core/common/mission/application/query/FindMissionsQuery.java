package br.com.muxe.core.common.mission.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.core.common.mission.domain.MissionBoard;
import java.util.Objects;
import java.util.UUID;

public record FindMissionsQuery(String game, UUID playerId) implements Query<MissionBoard> {

    public FindMissionsQuery {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(playerId, "playerId");
    }
}
