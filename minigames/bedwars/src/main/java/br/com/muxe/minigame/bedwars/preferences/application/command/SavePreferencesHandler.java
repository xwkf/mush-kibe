package br.com.muxe.minigame.bedwars.preferences.application.command;

import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.preferences.domain.PreferencesRepository;
import java.time.Clock;

public final class SavePreferencesHandler implements CommandHandler<SavePreferencesCommand, Result<Void>> {

    private final PreferencesRepository preferences;
    private final Transactions transactions;
    private final Clock clock;

    public SavePreferencesHandler(PreferencesRepository preferences, Transactions transactions, Clock clock) {
        this.preferences = preferences;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public Result<Void> handle(SavePreferencesCommand command) {
        return transactions.inTransaction("bedwars.preferences.save", () -> {
            preferences.save(command.playerId(), command.preferences(), clock.instant());
            return Result.success();
        });
    }
}
