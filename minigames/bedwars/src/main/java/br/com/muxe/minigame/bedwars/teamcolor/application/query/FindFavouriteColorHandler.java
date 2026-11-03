package br.com.muxe.minigame.bedwars.teamcolor.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.teamcolor.domain.FavouriteColorRepository;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Optional;

public final class FindFavouriteColorHandler implements QueryHandler<FindFavouriteColorQuery, Optional<TeamColor>> {

    private final FavouriteColorRepository colors;
    private final Transactions transactions;

    public FindFavouriteColorHandler(FavouriteColorRepository colors, Transactions transactions) {
        this.colors = colors;
        this.transactions = transactions;
    }

    @Override
    public Optional<TeamColor> handle(FindFavouriteColorQuery query) {
        return transactions.readOnly("bedwars.team-color.find", () -> colors.find(query.playerId()));
    }
}
