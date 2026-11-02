package br.com.muxe.minigame.bedwars.stats.application.command;

import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public final class RecordMatchHandler implements CommandHandler<RecordMatchCommand, Result<Void>> {

    private final BedWarsStatsRepository stats;
    private final Transactions transactions;
    private final Clock clock;
    private final ZoneId zone;

    public RecordMatchHandler(BedWarsStatsRepository stats, Transactions transactions, Clock clock, ZoneId zone) {
        this.stats = stats;
        this.transactions = transactions;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public Result<Void> handle(RecordMatchCommand command) {
        Instant at = clock.instant();
        return transactions.inTransaction("bedwars.stats.record", () -> {
            if (!stats.claimMatch(command.matchId(), at)) {
                return Result.success();
            }
            for (MatchOutcome outcome : command.outcomes()) {
                stats.record(outcome, at, zone);
            }
            return Result.success();
        });
    }
}
