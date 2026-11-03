package br.com.muxe.minigame.bedwars.preferences.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import br.com.muxe.minigame.bedwars.preferences.domain.PreferencesRepository;
import java.util.Optional;

public final class FindPreferencesHandler implements QueryHandler<FindPreferencesQuery, Optional<Preferences>> {

    private final PreferencesRepository preferences;
    private final Transactions transactions;

    public FindPreferencesHandler(PreferencesRepository preferences, Transactions transactions) {
        this.preferences = preferences;
        this.transactions = transactions;
    }

    @Override
    public Optional<Preferences> handle(FindPreferencesQuery query) {
        return transactions.readOnly("bedwars.preferences.find", () -> preferences.find(query.playerId()));
    }
}
