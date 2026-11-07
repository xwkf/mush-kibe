package br.com.muxe.core.common.visibility.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.visibility.domain.Visibility;
import br.com.muxe.core.common.visibility.domain.VisibilityRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresVisibilityRepository implements VisibilityRepository {

    private static final Table PLAYER_VISIBILITY = Table.of("core", "player_visibility");
    private static final Column<UUID> PLAYER_ID = PLAYER_VISIBILITY.column("player_id", SqlType.UUID);
    private static final Column<Visibility> VISIBILITY = PLAYER_VISIBILITY.column("visibility", SqlType.TEXT.map(Visibility::parse, Visibility::name));
    private static final Column<Instant> UPDATED_AT = PLAYER_VISIBILITY.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<Visibility> FIND = Sql.select(VISIBILITY).from(PLAYER_VISIBILITY)
        .where(PLAYER_ID.eq())
        .map(row -> row.get(VISIBILITY))
        .named("visibility.find");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(PLAYER_VISIBILITY)
        .value(PLAYER_ID)
        .value(VISIBILITY)
        .value(UPDATED_AT)
        .onConflict(PLAYER_ID).doUpdate(VISIBILITY, UPDATED_AT)
        .named("visibility.save");

    @Override
    public Optional<Visibility> find(UUID playerId) {
        return FIND.first(PLAYER_ID.is(playerId));
    }

    @Override
    public void save(UUID playerId, Visibility visibility, Instant updatedAt) {
        SAVE.execute(PLAYER_ID.is(playerId), VISIBILITY.is(visibility), UPDATED_AT.is(updatedAt));
    }
}
