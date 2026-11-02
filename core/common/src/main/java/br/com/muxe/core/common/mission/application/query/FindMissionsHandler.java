package br.com.muxe.core.common.mission.application.query;

import br.com.muxe.core.common.cqrs.query.QueryHandler;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.mission.domain.MissionBoard;
import br.com.muxe.core.common.mission.domain.MissionCatalog;
import br.com.muxe.core.common.mission.domain.MissionGames;
import br.com.muxe.core.common.mission.domain.MissionRepository;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public final class FindMissionsHandler implements QueryHandler<FindMissionsQuery, MissionBoard> {

    private final MissionGames games;
    private final MissionRepository missions;
    private final Transactions transactions;
    private final Clock clock;
    private final ZoneId zone;

    public FindMissionsHandler(MissionGames games, MissionRepository missions, Transactions transactions, Clock clock, ZoneId zone) {
        this.games = games;
        this.missions = missions;
        this.transactions = transactions;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public MissionBoard handle(FindMissionsQuery query) {
        Instant at = clock.instant();
        MissionCatalog catalog = games.find(query.game()).map(MissionGames.Game::catalog).orElse(MissionCatalog.empty());
        List<Mission> active = catalog.active(query.playerId(), at, zone);
        if (active.isEmpty()) {
            return new MissionBoard(List.of());
        }
        Map<String, LocalDate> starts = MissionBoard.starts(active, at, zone);
        return transactions.readOnly("missions.find", () -> MissionBoard.of(active, missions.find(query.game(), query.playerId(), starts), starts));
    }
}
