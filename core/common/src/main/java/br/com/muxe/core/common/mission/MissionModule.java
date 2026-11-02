package br.com.muxe.core.common.mission;

import br.com.muxe.core.common.credit.domain.CreditRepository;
import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.mission.application.command.RecordMissionProgressCommand;
import br.com.muxe.core.common.mission.application.command.RecordMissionProgressHandler;
import br.com.muxe.core.common.mission.application.query.FindMissionsHandler;
import br.com.muxe.core.common.mission.application.query.FindMissionsQuery;
import br.com.muxe.core.common.mission.domain.MissionGames;
import br.com.muxe.core.common.mission.domain.MissionRepository;
import br.com.muxe.core.common.persistence.transaction.Transactions;
import java.time.Clock;
import java.time.ZoneId;

public final class MissionModule {

    public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private MissionModule() {
    }

    public static void register(CommandBus commands, QueryBus queries, MissionGames games, MissionRepository missions, CreditRepository credits,
                                Transactions transactions, Clock clock, ZoneId zone) {
        commands.register(RecordMissionProgressCommand.class, new RecordMissionProgressHandler(games, missions, credits, transactions, clock, zone));
        queries.register(FindMissionsQuery.class, new FindMissionsHandler(games, missions, transactions, clock, zone));
    }
}
