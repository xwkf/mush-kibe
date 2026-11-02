package br.com.muxe.core.common.arena.application.query;

import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import java.util.List;

public final class ListArenasHandler implements QueryHandler<ListArenasQuery, List<String>> {

    private final ArenaRepository arenas;
    private final Transactions transactions;

    public ListArenasHandler(ArenaRepository arenas, Transactions transactions) {
        this.arenas = arenas;
        this.transactions = transactions;
    }

    @Override
    public List<String> handle(ListArenasQuery query) {
        return transactions.readOnly("arenas.list", () -> arenas.names(query.game()));
    }
}
