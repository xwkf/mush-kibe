package br.com.muxe.minigame.bedwars.quickbuy;

import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.quickbuy.application.command.SaveQuickBuyCommand;
import br.com.muxe.minigame.bedwars.quickbuy.application.command.SaveQuickBuyHandler;
import br.com.muxe.minigame.bedwars.quickbuy.application.query.FindQuickBuyHandler;
import br.com.muxe.minigame.bedwars.quickbuy.application.query.FindQuickBuyQuery;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuyRepository;
import br.com.muxe.minigame.bedwars.quickbuy.infrastructure.postgres.PostgresQuickBuyRepository;
import java.time.Clock;

public final class QuickBuyModule {

    private QuickBuyModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, Transactions transactions, Clock clock) {
        register(commands, queries, new PostgresQuickBuyRepository(), transactions, clock);
    }

    public static void register(CommandBus commands, QueryBus queries, QuickBuyRepository quickBuys, Transactions transactions, Clock clock) {
        commands.register(SaveQuickBuyCommand.class, new SaveQuickBuyHandler(quickBuys, transactions, clock));
        queries.register(FindQuickBuyQuery.class, new FindQuickBuyHandler(quickBuys, transactions));
    }
}
