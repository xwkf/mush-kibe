package br.com.muxe.minigame.bedwars.preferences;

import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.preferences.application.command.SavePreferencesCommand;
import br.com.muxe.minigame.bedwars.preferences.application.command.SavePreferencesHandler;
import br.com.muxe.minigame.bedwars.preferences.application.query.FindPreferencesHandler;
import br.com.muxe.minigame.bedwars.preferences.application.query.FindPreferencesQuery;
import br.com.muxe.minigame.bedwars.preferences.domain.PreferencesRepository;
import br.com.muxe.minigame.bedwars.preferences.infrastructure.postgres.PostgresPreferencesRepository;
import java.time.Clock;

public final class PreferencesModule {

    private PreferencesModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, Transactions transactions, Clock clock) {
        register(commands, queries, new PostgresPreferencesRepository(), transactions, clock);
    }

    public static void register(CommandBus commands, QueryBus queries, PreferencesRepository preferences, Transactions transactions, Clock clock) {
        commands.register(SavePreferencesCommand.class, new SavePreferencesHandler(preferences, transactions, clock));
        queries.register(FindPreferencesQuery.class, new FindPreferencesHandler(preferences, transactions));
    }
}
