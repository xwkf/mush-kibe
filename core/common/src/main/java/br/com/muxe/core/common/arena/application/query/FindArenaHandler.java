package br.com.muxe.core.common.arena.application.query;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import java.util.Optional;

public final class FindArenaHandler implements QueryHandler<FindArenaQuery, Optional<ArenaRecord>> {

    private final ArenaRepository arenas;
    private final Transactions transactions;

    public FindArenaHandler(ArenaRepository arenas, Transactions transactions) {
        this.arenas = arenas;
        this.transactions = transactions;
    }

    @Override
    public Optional<ArenaRecord> handle(FindArenaQuery query) {
        return transactions.readOnly("arenas.find", () -> arenas.find(query.game(), query.name()));
    }
}
