package br.com.muxe.core.common.arena.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import java.util.List;
import java.util.Objects;

public record ListArenasQuery(String game) implements Query<List<String>> {

    public ListArenasQuery {
        Objects.requireNonNull(game, "game");
    }
}
