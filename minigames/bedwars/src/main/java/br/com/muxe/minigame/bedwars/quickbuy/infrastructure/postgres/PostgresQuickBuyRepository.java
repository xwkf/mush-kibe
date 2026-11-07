package br.com.muxe.minigame.bedwars.quickbuy.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuyRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresQuickBuyRepository implements QuickBuyRepository {

    private static final Table QUICK_BUY = Table.of("bedwars", "quick_buy");
    private static final Column<UUID> PLAYER_ID = QUICK_BUY.column("player_id", SqlType.UUID);
    private static final Column<List<String>> SLOTS = QUICK_BUY.column("slots", SqlType.TEXT_ARRAY);
    private static final Column<Instant> UPDATED_AT = QUICK_BUY.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Select<QuickBuy> FIND = Sql.select(SLOTS).from(QUICK_BUY)
        .where(PLAYER_ID.eq())
        .map(row -> QuickBuy.of(List.copyOf(row.get(SLOTS))))
        .named("bedwars.quick_buy.find");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(QUICK_BUY)
        .value(PLAYER_ID)
        .value(SLOTS)
        .value(UPDATED_AT)
        .onConflict(PLAYER_ID).doUpdate(SLOTS, UPDATED_AT)
        .named("bedwars.quick_buy.save");

    @Override
    public Optional<QuickBuy> find(UUID playerId) {
        return FIND.first(PLAYER_ID.is(playerId));
    }

    @Override
    public void save(UUID playerId, QuickBuy quickBuy, Instant at) {
        SAVE.execute(PLAYER_ID.is(playerId), SLOTS.is(quickBuy.slots()), UPDATED_AT.is(at));
    }
}
