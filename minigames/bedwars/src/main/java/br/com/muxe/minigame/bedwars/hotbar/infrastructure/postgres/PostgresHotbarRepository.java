package br.com.muxe.minigame.bedwars.hotbar.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresHotbarRepository implements HotbarRepository {

    private static final Table LAYOUTS = Table.of("bedwars", "hotbar_layout");
    private static final Column<UUID> PLAYER_ID = LAYOUTS.column("player_id", SqlType.UUID);
    private static final Column<List<String>> SLOTS = LAYOUTS.column("slots", SqlType.TEXT_ARRAY);
    private static final Column<Instant> UPDATED_AT = LAYOUTS.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<HotbarLayout> FIND = Sql.select(SLOTS).from(LAYOUTS)
        .where(PLAYER_ID.eq())
        .map(row -> HotbarLayout.decode(List.copyOf(row.get(SLOTS))))
        .named("bedwars.hotbar.find");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(LAYOUTS)
        .value(PLAYER_ID)
        .value(SLOTS)
        .value(UPDATED_AT)
        .onConflict(PLAYER_ID).doUpdate(SLOTS, UPDATED_AT)
        .named("bedwars.hotbar.save");

    @Override
    public Optional<HotbarLayout> find(UUID playerId) {
        return FIND.first(PLAYER_ID.is(playerId));
    }

    @Override
    public void save(UUID playerId, HotbarLayout layout, Instant at) {
        SAVE.execute(PLAYER_ID.is(playerId), SLOTS.is(layout.encode()), UPDATED_AT.is(at));
    }
}
