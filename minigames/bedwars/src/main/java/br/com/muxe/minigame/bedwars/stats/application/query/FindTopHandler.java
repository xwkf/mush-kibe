package br.com.muxe.minigame.bedwars.stats.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public final class FindTopHandler implements QueryHandler<FindTopQuery, List<TopEntry>> {

    private final BedWarsStatsRepository stats;
    private final Transactions transactions;
    private final Clock clock;
    private final ZoneId zone;

    public FindTopHandler(BedWarsStatsRepository stats, Transactions transactions, Clock clock, ZoneId zone) {
        this.stats = stats;
        this.transactions = transactions;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public List<TopEntry> handle(FindTopQuery query) {
        LocalDate start = query.period().start(clock.instant(), zone);
        return transactions.readOnly("bedwars.stats.top",
            () -> stats.top(query.category(), query.mode(), query.period(), start, query.limit()));
    }
}
