package br.com.muxe.minigame.bedwars.teamcolor;

import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.teamcolor.application.command.SaveFavouriteColorCommand;
import br.com.muxe.minigame.bedwars.teamcolor.application.command.SaveFavouriteColorHandler;
import br.com.muxe.minigame.bedwars.teamcolor.application.query.FindFavouriteColorHandler;
import br.com.muxe.minigame.bedwars.teamcolor.application.query.FindFavouriteColorQuery;
import br.com.muxe.minigame.bedwars.teamcolor.domain.FavouriteColorRepository;
import br.com.muxe.minigame.bedwars.teamcolor.infrastructure.postgres.PostgresFavouriteColorRepository;
import java.time.Clock;

public final class TeamColorModule {

    private TeamColorModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, Transactions transactions, Clock clock) {
        register(commands, queries, new PostgresFavouriteColorRepository(), transactions, clock);
    }

    public static void register(CommandBus commands, QueryBus queries, FavouriteColorRepository colors, Transactions transactions, Clock clock) {
        commands.register(SaveFavouriteColorCommand.class, new SaveFavouriteColorHandler(colors, transactions, clock));
        queries.register(FindFavouriteColorQuery.class, new FindFavouriteColorHandler(colors, transactions));
    }
}
