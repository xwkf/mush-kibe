package br.com.muxe.core.common.mission.application.command;

import br.com.muxe.core.common.credit.domain.CreditRepository;
import br.com.muxe.core.common.cqrs.command.CommandHandler;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.mission.domain.MissionBoard;
import br.com.muxe.core.common.mission.domain.MissionGames;
import br.com.muxe.core.common.mission.domain.MissionProgress;
import br.com.muxe.core.common.mission.domain.MissionRepository;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import br.com.muxe.core.common.result.Result;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RecordMissionProgressHandler implements CommandHandler<RecordMissionProgressCommand, Result<List<Mission>>> {

    public static final String UNKNOWN_GAME = "mission.unknown_game";
    public static final String CREDIT_REASON = "missions:";

    private final MissionGames games;
    private final MissionRepository missions;
    private final CreditRepository credits;
    private final Transactions transactions;
    private final Clock clock;
    private final ZoneId zone;

    public RecordMissionProgressHandler(MissionGames games, MissionRepository missions, CreditRepository credits, Transactions transactions, Clock clock, ZoneId zone) {
        this.games = games;
        this.missions = missions;
        this.credits = credits;
        this.transactions = transactions;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public Result<List<Mission>> handle(RecordMissionProgressCommand command) {
        Optional<MissionGames.Game> game = games.find(command.game());
        if (game.isEmpty()) {
            return Result.failure(UNKNOWN_GAME, "No missions are registered for " + command.game());
        }
        Instant at = clock.instant();
        List<Mission> active = game.get().catalog().active(command.playerId(), at, zone);
        if (active.isEmpty()) {
            return Result.success(List.of());
        }
        Map<String, LocalDate> starts = MissionBoard.starts(active, at, zone);
        return transactions.inTransaction("missions.record", () -> {
            MissionBoard board = MissionBoard.of(active, missions.find(command.game(), command.playerId(), starts), starts);
            MissionBoard.Update update = board.apply(command.amounts());
            for (MissionProgress progress : update.changed()) {
                missions.save(command.game(), command.playerId(), progress);
            }
            if (!update.completed().isEmpty()) {
                long earned = update.completed().stream().mapToLong(Mission::credits).sum();
                if (earned > 0) {
                    credits.add(command.playerId(), earned, CREDIT_REASON + command.game(), at);
                }
                game.get().rewards().grant(command.playerId(), update.completed(), at);
            }
            return Result.success(update.completed());
        });
    }
}
