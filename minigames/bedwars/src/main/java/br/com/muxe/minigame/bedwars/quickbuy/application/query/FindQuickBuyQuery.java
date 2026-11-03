package br.com.muxe.minigame.bedwars.quickbuy.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record FindQuickBuyQuery(UUID playerId) implements Query<Optional<QuickBuy>> {

    public FindQuickBuyQuery {
        Objects.requireNonNull(playerId, "playerId");
    }
}
