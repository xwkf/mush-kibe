package br.com.muxe.minigame.bedwars.stats;

import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.stats.application.command.RecordMatchCommand;
import br.com.muxe.minigame.bedwars.stats.application.command.RecordMatchHandler;
import br.com.muxe.minigame.bedwars.stats.application.command.ToggleFavouriteMapCommand;
import br.com.muxe.minigame.bedwars.stats.application.command.ToggleFavouriteMapHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindFavouriteMapsHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindFavouriteMapsQuery;
import br.com.muxe.minigame.bedwars.stats.application.query.FindPlayerStatsHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindPlayerStatsQuery;
import br.com.muxe.minigame.bedwars.stats.application.query.FindTopHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindTopQuery;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.infrastructure.postgres.PostgresBedWarsStatsRepository;
import java.time.Clock;
import java.time.ZoneId;

public final class StatsModule {

    private StatsModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, Transactions transactions, Clock clock, ZoneId zone) {
        register(commands, queries, new PostgresBedWarsStatsRepository(), transactions, clock, zone);
    }

    public static void register(
        CommandBus commands,
        QueryBus queries,
        BedWarsStatsRepository stats,
        Transactions transactions,
        Clock clock,
        ZoneId zone
    ) {
        commands.register(RecordMatchCommand.class, new RecordMatchHandler(stats, transactions, clock, zone));
        commands.register(ToggleFavouriteMapCommand.class, new ToggleFavouriteMapHandler(stats, transactions, clock));
        queries.register(FindPlayerStatsQuery.class, new FindPlayerStatsHandler(stats, transactions, clock, zone));
        queries.register(FindTopQuery.class, new FindTopHandler(stats, transactions, clock, zone));
        queries.register(FindFavouriteMapsQuery.class, new FindFavouriteMapsHandler(stats, transactions));
    }
}
