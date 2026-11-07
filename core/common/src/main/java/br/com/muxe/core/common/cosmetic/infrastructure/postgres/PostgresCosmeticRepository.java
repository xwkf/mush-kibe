package br.com.muxe.core.common.cosmetic.infrastructure.postgres;

import br.com.muxe.core.common.cosmetic.domain.CosmeticRepository;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PostgresCosmeticRepository implements CosmeticRepository {

    private static final Table OWNED = Table.of("core", "owned_cosmetics");
    private static final Column<UUID> OWNER = OWNED.column("player_id", SqlType.UUID);
    private static final Column<String> OWNED_CATEGORY = OWNED.column("category", SqlType.TEXT);
    private static final Column<String> OWNED_COSMETIC = OWNED.column("cosmetic_id", SqlType.TEXT);
    private static final Column<String> SOURCE = OWNED.column("source", SqlType.TEXT);
    private static final Column<Instant> ACQUIRED_AT = OWNED.column("acquired_at", SqlType.TIMESTAMPTZ);

    private static final Table SELECTED = Table.of("core", "selected_cosmetics");
    private static final Column<UUID> SELECTOR = SELECTED.column("player_id", SqlType.UUID);
    private static final Column<String> SELECTED_CATEGORY = SELECTED.column("category", SqlType.TEXT);
    private static final Column<String> SELECTED_COSMETIC = SELECTED.column("cosmetic_id", SqlType.TEXT);
    private static final Column<Instant> UPDATED_AT = SELECTED.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<String> FIND_OWNED = Sql.select(OWNED_COSMETIC).from(OWNED)
        .where(OWNER.eq().and(OWNED_CATEGORY.eq()))
        .orderBy(ACQUIRED_AT.asc())
        .map(row -> row.get(OWNED_COSMETIC))
        .named("cosmetics.owned");

    private static final Write<Void> OWN = Sql.<Void>insertInto(OWNED)
        .value(OWNER)
        .value(OWNED_CATEGORY)
        .value(OWNED_COSMETIC)
        .value(SOURCE)
        .value(ACQUIRED_AT)
        .onConflict().doNothing()
        .named("cosmetics.own");

    private static final Select<String> FIND_SELECTED = Sql.select(SELECTED_COSMETIC).from(SELECTED)
        .where(SELECTOR.eq().and(SELECTED_CATEGORY.eq()))
        .map(row -> row.get(SELECTED_COSMETIC))
        .named("cosmetics.selected");

    private static final Write<Void> SELECT = Sql.<Void>insertInto(SELECTED)
        .value(SELECTOR)
        .value(SELECTED_CATEGORY)
        .value(SELECTED_COSMETIC)
        .value(UPDATED_AT)
        .onConflict(SELECTOR, SELECTED_CATEGORY).doUpdate(SELECTED_COSMETIC, UPDATED_AT)
        .named("cosmetics.select");

    @Override
    public Set<String> owned(UUID playerId, String category) {
        return new LinkedHashSet<>(FIND_OWNED.list(OWNER.is(playerId), OWNED_CATEGORY.is(category)));
    }

    @Override
    public boolean own(UUID playerId, String category, String cosmeticId, String source, Instant at) {
        return OWN.execute(OWNER.is(playerId), OWNED_CATEGORY.is(category), OWNED_COSMETIC.is(cosmeticId), SOURCE.is(source), ACQUIRED_AT.is(at)) > 0;
    }

    @Override
    public Optional<String> selected(UUID playerId, String category) {
        return FIND_SELECTED.first(SELECTOR.is(playerId), SELECTED_CATEGORY.is(category));
    }

    @Override
    public void select(UUID playerId, String category, String cosmeticId, Instant at) {
        SELECT.execute(SELECTOR.is(playerId), SELECTED_CATEGORY.is(category), SELECTED_COSMETIC.is(cosmeticId), UPDATED_AT.is(at));
    }
}
