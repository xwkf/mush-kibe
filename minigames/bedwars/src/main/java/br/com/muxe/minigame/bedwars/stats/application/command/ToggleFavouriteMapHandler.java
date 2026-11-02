package br.com.muxe.minigame.bedwars.stats.application.command;

import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import java.time.Clock;

public final class ToggleFavouriteMapHandler implements CommandHandler<ToggleFavouriteMapCommand, Result<Boolean>> {

    private final BedWarsStatsRepository stats;
    private final Transactions transactions;
    private final Clock clock;

    public ToggleFavouriteMapHandler(BedWarsStatsRepository stats, Transactions transactions, Clock clock) {
        this.stats = stats;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public Result<Boolean> handle(ToggleFavouriteMapCommand command) {
        return transactions.inTransaction("bedwars.favourites.toggle",
            () -> Result.success(stats.toggleFavouriteMap(command.playerId(), command.arena(), clock.instant())));
    }
}
