package br.com.muxe.minigame.bedwars.hotbar.application.command;

import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarRepository;
import java.time.Clock;

public final class SaveHotbarHandler implements CommandHandler<SaveHotbarCommand, Result<Void>> {

    private final HotbarRepository hotbars;
    private final Transactions transactions;
    private final Clock clock;

    public SaveHotbarHandler(HotbarRepository hotbars, Transactions transactions, Clock clock) {
        this.hotbars = hotbars;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public Result<Void> handle(SaveHotbarCommand command) {
        return transactions.inTransaction("bedwars.hotbar.save", () -> {
            hotbars.save(command.playerId(), command.layout(), clock.instant());
            return Result.success();
        });
    }
}
