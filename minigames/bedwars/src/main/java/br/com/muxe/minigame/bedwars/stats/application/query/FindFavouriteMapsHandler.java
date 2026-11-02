package br.com.muxe.minigame.bedwars.stats.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import java.util.Set;

public final class FindFavouriteMapsHandler implements QueryHandler<FindFavouriteMapsQuery, Set<String>> {

    private final BedWarsStatsRepository stats;
    private final Transactions transactions;

    public FindFavouriteMapsHandler(BedWarsStatsRepository stats, Transactions transactions) {
        this.stats = stats;
        this.transactions = transactions;
    }

    @Override
    public Set<String> handle(FindFavouriteMapsQuery query) {
        return transactions.readOnly("bedwars.favourites.find", () -> stats.favouriteMaps(query.playerId()));
    }
}
