package br.com.muxe.core.common.mission.infrastructure.postgres;

import br.com.muxe.core.common.mission.domain.MissionProgress;
import br.com.muxe.core.common.mission.domain.MissionRepository;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public final class PostgresMissionRepository implements MissionRepository {

    private static final Table PROGRESS = Table.of("core", "mission_progress");
    private static final Column<UUID> PLAYER_ID = PROGRESS.column("player_id", SqlType.UUID);
    private static final Column<String> GAME = PROGRESS.column("game", SqlType.TEXT);
    private static final Column<String> MISSION = PROGRESS.column("mission", SqlType.TEXT);
    private static final Column<LocalDate> PERIOD_START = PROGRESS.column("period_start", SqlType.DATE);
    private static final Column<Long> AMOUNT = PROGRESS.column("progress", SqlType.BIGINT);
    private static final Column<Boolean> COMPLETED = PROGRESS.column("completed", SqlType.BOOLEAN);

    private static final Select<MissionProgress> FIND = Sql.select(MISSION, PERIOD_START, AMOUNT, COMPLETED).from(PROGRESS)
        .where(PLAYER_ID.eq().and(GAME.eq()).and(MISSION.in()).and(PERIOD_START.in()))
        .map(row -> new MissionProgress(row.get(MISSION), row.get(PERIOD_START), row.get(AMOUNT), row.get(COMPLETED)))
        .named("missions.find");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(PROGRESS)
        .value(PLAYER_ID)
        .value(GAME)
        .value(MISSION)
        .value(PERIOD_START)
        .value(AMOUNT)
        .value(COMPLETED)
        .onConflict(PLAYER_ID, GAME, MISSION, PERIOD_START).doUpdate(AMOUNT, COMPLETED)
        .named("missions.save");

    @Override
    public Map<String, MissionProgress> find(String game, UUID playerId, Map<String, LocalDate> missions) {
        Map<String, MissionProgress> found = new HashMap<>();
        if (missions.isEmpty()) {
            return found;
        }
        for (MissionProgress progress : FIND.list(PLAYER_ID.is(playerId), GAME.is(game), MISSION.in(missions.keySet()), PERIOD_START.in(new HashSet<>(missions.values())))) {
            if (progress.start().equals(missions.get(progress.mission()))) {
                found.put(progress.mission(), progress);
            }
        }
        return found;
    }

    @Override
    public void save(String game, UUID playerId, MissionProgress progress) {
        SAVE.execute(PLAYER_ID.is(playerId), GAME.is(game), MISSION.is(progress.mission()), PERIOD_START.is(progress.start()),
            AMOUNT.is(progress.progress()), COMPLETED.is(progress.completed()));
    }
}
