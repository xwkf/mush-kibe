package br.com.muxe.minigame.bedwars.stats.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import java.time.Clock;
import java.time.ZoneId;

public final class FindPlayerStatsHandler implements QueryHandler<FindPlayerStatsQuery, PlayerStats> {

    private final BedWarsStatsRepository stats;
    private final Transactions transactions;
    private final Clock clock;
    private final ZoneId zone;

    public FindPlayerStatsHandler(BedWarsStatsRepository stats, Transactions transactions, Clock clock, ZoneId zone) {
        this.stats = stats;
        this.transactions = transactions;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public PlayerStats handle(FindPlayerStatsQuery query) {
        if (query.period() == StatsPeriod.TOTAL) {
            return transactions.readOnly("bedwars.stats.totals", () -> stats.totals(query.playerId()));
        }
        return transactions.readOnly("bedwars.stats.period",
            () -> stats.period(query.playerId(), query.period(), clock.instant(), zone));
    }
}
