package br.com.muxe.minigame.bedwars.quickbuy.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuyRepository;
import java.util.Optional;

public final class FindQuickBuyHandler implements QueryHandler<FindQuickBuyQuery, Optional<QuickBuy>> {

    private final QuickBuyRepository quickBuys;
    private final Transactions transactions;

    public FindQuickBuyHandler(QuickBuyRepository quickBuys, Transactions transactions) {
        this.quickBuys = quickBuys;
        this.transactions = transactions;
    }

    @Override
    public Optional<QuickBuy> handle(FindQuickBuyQuery query) {
        return transactions.readOnly("bedwars.quick_buy.find", () -> quickBuys.find(query.playerId()));
    }
}
