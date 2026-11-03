package br.com.muxe.minigame.bedwars.hotbar.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record FindHotbarQuery(UUID playerId) implements Query<Optional<HotbarLayout>> {

    public FindHotbarQuery {
        Objects.requireNonNull(playerId, "playerId");
    }
}
