package br.com.muxe.minigame.bedwars.hotbar.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarRepository;
import java.util.Optional;

public final class FindHotbarHandler implements QueryHandler<FindHotbarQuery, Optional<HotbarLayout>> {

    private final HotbarRepository hotbars;
    private final Transactions transactions;

    public FindHotbarHandler(HotbarRepository hotbars, Transactions transactions) {
        this.hotbars = hotbars;
        this.transactions = transactions;
    }

    @Override
    public Optional<HotbarLayout> handle(FindHotbarQuery query) {
        return transactions.readOnly("bedwars.hotbar.find", () -> hotbars.find(query.playerId()));
    }
}
