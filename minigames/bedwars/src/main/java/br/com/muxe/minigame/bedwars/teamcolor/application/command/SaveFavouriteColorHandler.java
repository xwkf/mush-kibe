package br.com.muxe.minigame.bedwars.teamcolor.application.command;

import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.teamcolor.domain.FavouriteColorRepository;
import java.time.Clock;

public final class SaveFavouriteColorHandler implements CommandHandler<SaveFavouriteColorCommand, Result<Void>> {

    private final FavouriteColorRepository colors;
    private final Transactions transactions;
    private final Clock clock;

    public SaveFavouriteColorHandler(FavouriteColorRepository colors, Transactions transactions, Clock clock) {
        this.colors = colors;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public Result<Void> handle(SaveFavouriteColorCommand command) {
        return transactions.inTransaction("bedwars.team-color.save", () -> {
            command.color().ifPresentOrElse(
                color -> colors.save(command.playerId(), color, clock.instant()),
                () -> colors.clear(command.playerId()));
            return Result.success();
        });
    }
}
