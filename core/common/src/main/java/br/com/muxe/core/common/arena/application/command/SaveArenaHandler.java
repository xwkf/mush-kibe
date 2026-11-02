package br.com.muxe.core.common.arena.application.command;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import java.time.Clock;

public final class SaveArenaHandler implements CommandHandler<SaveArenaCommand, Result<Void>> {

    private final ArenaRepository arenas;
    private final Transactions transactions;
    private final Clock clock;

    public SaveArenaHandler(ArenaRepository arenas, Transactions transactions, Clock clock) {
        this.arenas = arenas;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public Result<Void> handle(SaveArenaCommand command) {
        ArenaRecord arena = new ArenaRecord(command.game(), command.name(), command.mode(), command.ready(), command.definition(), command.map(), clock.instant());
        return transactions.inTransaction("arenas.save", () -> {
            arenas.save(arena, command.keepMap());
            return Result.success();
        });
    }
}
