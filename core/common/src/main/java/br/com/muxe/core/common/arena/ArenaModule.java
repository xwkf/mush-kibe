package br.com.muxe.core.common.arena;

import br.com.muxe.core.common.arena.application.command.SaveArenaCommand;
import br.com.muxe.core.common.arena.application.command.SaveArenaHandler;
import br.com.muxe.core.common.arena.application.query.FindArenaHandler;
import br.com.muxe.core.common.arena.application.query.FindArenaQuery;
import br.com.muxe.core.common.arena.application.query.ListArenasHandler;
import br.com.muxe.core.common.arena.application.query.ListArenasQuery;
import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import java.time.Clock;

public final class ArenaModule {

    private ArenaModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, ArenaRepository arenas, Transactions transactions, Clock clock) {
        commands.register(SaveArenaCommand.class, new SaveArenaHandler(arenas, transactions, clock));
        queries.register(FindArenaQuery.class, new FindArenaHandler(arenas, transactions));
        queries.register(ListArenasQuery.class, new ListArenasHandler(arenas, transactions));
    }
}
