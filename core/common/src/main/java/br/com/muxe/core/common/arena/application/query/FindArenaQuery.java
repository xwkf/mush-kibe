package br.com.muxe.core.common.arena.application.query;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.cqrs.query.Query;
import java.util.Objects;
import java.util.Optional;

public record FindArenaQuery(String game, String name) implements Query<Optional<ArenaRecord>> {

    public FindArenaQuery {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(name, "name");
    }
}
