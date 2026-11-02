package br.com.muxe.core.common.arena.application.command;

import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;

public final class DeleteArenaHandler implements CommandHandler<DeleteArenaCommand, Result<Void>> {

    public static final String NOT_FOUND = "arena.not_found";

    private final ArenaRepository arenas;
    private final Transactions transactions;

    public DeleteArenaHandler(ArenaRepository arenas, Transactions transactions) {
        this.arenas = arenas;
        this.transactions = transactions;
    }

    @Override
    public Result<Void> handle(DeleteArenaCommand command) {
        return transactions.inTransaction("arenas.delete", () -> arenas.delete(command.game(), command.name())
            ? Result.<Void>success()
            : Result.<Void>failure(NOT_FOUND, "Arena " + command.game() + "/" + command.name() + " does not exist"));
    }
}
