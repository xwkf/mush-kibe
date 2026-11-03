package br.com.muxe.minigame.bedwars.quickbuy.application.command;

import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuyRepository;
import java.time.Clock;

public final class SaveQuickBuyHandler implements CommandHandler<SaveQuickBuyCommand, Result<Void>> {

    private final QuickBuyRepository quickBuys;
    private final Transactions transactions;
    private final Clock clock;

    public SaveQuickBuyHandler(QuickBuyRepository quickBuys, Transactions transactions, Clock clock) {
        this.quickBuys = quickBuys;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public Result<Void> handle(SaveQuickBuyCommand command) {
        return transactions.inTransaction("bedwars.quick_buy.save", () -> {
            quickBuys.save(command.playerId(), command.quickBuy(), clock.instant());
            return Result.success();
        });
    }
}
