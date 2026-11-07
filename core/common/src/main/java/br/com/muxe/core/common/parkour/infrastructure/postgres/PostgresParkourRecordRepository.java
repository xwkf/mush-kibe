package br.com.muxe.core.common.parkour.infrastructure.postgres;

import br.com.muxe.core.common.parkour.domain.ParkourRecordRepository;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresParkourRecordRepository implements ParkourRecordRepository {

    private static final Table RECORDS = Table.of("core", "parkour_records");
    private static final Column<String> GROUP = RECORDS.column("server_group", SqlType.TEXT);
    private static final Column<UUID> PLAYER_ID = RECORDS.column("player_id", SqlType.UUID);
    private static final Column<Duration> BEST = RECORDS.column("best_millis", SqlType.BIGINT.map(Duration::ofMillis, Duration::toMillis));
    private static final Column<Instant> ACHIEVED_AT = RECORDS.column("achieved_at", SqlType.TIMESTAMPTZ);

    private static final Column<String> LOCK_KEY = Sql.parameter("lock_key", SqlType.TEXT);

    private static final Select<Duration> FIND = Sql.select(BEST).from(RECORDS)
        .where(GROUP.eq().and(PLAYER_ID.eq()))
        .map(row -> row.get(BEST))
        .named("parkour.find_record");

    private static final Write<Void> LOCK = Sql.<Void>statement("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))")
        .parameter(LOCK_KEY)
        .named("parkour.lock_record");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(RECORDS)
        .value(GROUP)
        .value(PLAYER_ID)
        .value(BEST)
        .value(ACHIEVED_AT)
        .onConflict(GROUP, PLAYER_ID).doUpdate(BEST, ACHIEVED_AT)
        .where("core.parkour_records.best_millis > EXCLUDED.best_millis")
        .named("parkour.save_record");

    @Override
    public Optional<Duration> best(String group, UUID playerId) {
        return FIND.first(GROUP.is(group), PLAYER_ID.is(playerId));
    }

    @Override
    public Optional<Duration> bestForUpdate(String group, UUID playerId) {
        LOCK.execute(LOCK_KEY.is("core.parkour_records/" + group + "/" + playerId));
        return best(group, playerId);
    }

    @Override
    public void save(String group, UUID playerId, Duration best, Instant achievedAt) {
        SAVE.execute(GROUP.is(group), PLAYER_ID.is(playerId), BEST.is(best), ACHIEVED_AT.is(achievedAt));
    }
}
