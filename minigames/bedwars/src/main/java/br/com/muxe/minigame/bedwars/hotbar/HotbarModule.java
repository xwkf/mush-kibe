package br.com.muxe.minigame.bedwars.hotbar;

import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.hotbar.application.command.SaveHotbarCommand;
import br.com.muxe.minigame.bedwars.hotbar.application.command.SaveHotbarHandler;
import br.com.muxe.minigame.bedwars.hotbar.application.query.FindHotbarHandler;
import br.com.muxe.minigame.bedwars.hotbar.application.query.FindHotbarQuery;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarRepository;
import br.com.muxe.minigame.bedwars.hotbar.infrastructure.postgres.PostgresHotbarRepository;
import java.time.Clock;

public final class HotbarModule {

    private HotbarModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, Transactions transactions, Clock clock) {
        register(commands, queries, new PostgresHotbarRepository(), transactions, clock);
    }

    public static void register(CommandBus commands, QueryBus queries, HotbarRepository hotbars, Transactions transactions, Clock clock) {
        commands.register(SaveHotbarCommand.class, new SaveHotbarHandler(hotbars, transactions, clock));
        queries.register(FindHotbarQuery.class, new FindHotbarHandler(hotbars, transactions));
    }
}
